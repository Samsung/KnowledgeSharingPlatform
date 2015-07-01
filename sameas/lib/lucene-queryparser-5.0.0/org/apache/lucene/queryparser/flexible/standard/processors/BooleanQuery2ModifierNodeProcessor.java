package org.apache.lucene.queryparser.flexible.standard.processors;

/**
*
* Copyright(c) 2015, Samsung Electronics Co., Ltd.
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
  * Neither the name of the <organization> nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.
  
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/ 

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode.Modifier;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.precedence.processors.BooleanModifiersQueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.standard.nodes.BooleanModifierNode;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;

/**
 * <p>
 * This processor is used to apply the correct {@link ModifierQueryNode} to
 * {@link BooleanQueryNode}s children. This is a variant of
 * {@link BooleanModifiersQueryNodeProcessor} which ignores precedence.
 * </p>
 * <p>
 * The {@link StandardSyntaxParser} knows the rules of precedence, but lucene
 * does not. e.g. <code>(A AND B OR C AND D)</code> ist treated like
 * <code>(+A +B +C +D)</code>.
 * </p>
 * <p>
 * This processor walks through the query node tree looking for
 * {@link BooleanQueryNode}s. If an {@link AndQueryNode} is found, every child,
 * which is not a {@link ModifierQueryNode} or the {@link ModifierQueryNode} is
 * {@link Modifier#MOD_NONE}, becomes a {@link Modifier#MOD_REQ}. For default
 * {@link BooleanQueryNode}, it checks the default operator is
 * {@link Operator#AND}, if it is, the same operation when an
 * {@link AndQueryNode} is found is applied to it. Each {@link BooleanQueryNode}
 * which direct parent is also a {@link BooleanQueryNode} is removed (to ignore
 * the rules of precedence).
 * </p>
 * 
 * @see ConfigurationKeys#DEFAULT_OPERATOR
 * @see BooleanModifiersQueryNodeProcessor
 */
public class BooleanQuery2ModifierNodeProcessor implements QueryNodeProcessor {
  final static String TAG_REMOVE = "remove";
  final static String TAG_MODIFIER = "wrapWithModifier";
  final static String TAG_BOOLEAN_ROOT = "booleanRoot";
  
  QueryConfigHandler queryConfigHandler;
  
  private final ArrayList<QueryNode> childrenBuffer = new ArrayList<>();
  
  private Boolean usingAnd = false;
  
  public BooleanQuery2ModifierNodeProcessor() {
    // empty constructor
  }
  
  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    Operator op = getQueryConfigHandler().get(
        ConfigurationKeys.DEFAULT_OPERATOR);
    
    if (op == null) {
      throw new IllegalArgumentException(
          "StandardQueryConfigHandler.ConfigurationKeys.DEFAULT_OPERATOR should be set on the QueryConfigHandler");
    }
    
    this.usingAnd = StandardQueryConfigHandler.Operator.AND == op;
    
    return processIteration(queryTree);
    
  }
  
  protected void processChildren(QueryNode queryTree) throws QueryNodeException {
    List<QueryNode> children = queryTree.getChildren();
    if (children != null && children.size() > 0) {
      for (QueryNode child : children) {
        child = processIteration(child);
      }
    }
  }
  
  private QueryNode processIteration(QueryNode queryTree)
      throws QueryNodeException {
    queryTree = preProcessNode(queryTree);
    
    processChildren(queryTree);
    
    queryTree = postProcessNode(queryTree);
    
    return queryTree;
    
  }
  
  protected void fillChildrenBufferAndApplyModifiery(QueryNode parent) {
    for (QueryNode node : parent.getChildren()) {
      if (node.containsTag(TAG_REMOVE)) {
        fillChildrenBufferAndApplyModifiery(node);
      } else if (node.containsTag(TAG_MODIFIER)) {
        childrenBuffer.add(applyModifier(node,
            (Modifier) node.getTag(TAG_MODIFIER)));
      } else {
        childrenBuffer.add(node);
      }
    }
  }
  
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    if (node.containsTag(TAG_BOOLEAN_ROOT)) {
      this.childrenBuffer.clear();
      fillChildrenBufferAndApplyModifiery(node);
      node.set(childrenBuffer);
    }
    return node;
    
  }
  
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    QueryNode parent = node.getParent();
    if (node instanceof BooleanQueryNode) {
      if (parent instanceof BooleanQueryNode) {
        node.setTag(TAG_REMOVE, Boolean.TRUE); // no precedence
      } else {
        node.setTag(TAG_BOOLEAN_ROOT, Boolean.TRUE);
      }
    } else if (parent instanceof BooleanQueryNode) {
      if ((parent instanceof AndQueryNode)
          || (usingAnd && isDefaultBooleanQueryNode(parent))) {
        tagModifierButDoNotOverride(node, ModifierQueryNode.Modifier.MOD_REQ);
      }
    }
    return node;
  }
  
  protected boolean isDefaultBooleanQueryNode(QueryNode toTest) {
    return toTest != null && BooleanQueryNode.class.equals(toTest.getClass());
  }
  
  private QueryNode applyModifier(QueryNode node, Modifier mod) {
    
    // check if modifier is not already defined and is default
    if (!(node instanceof ModifierQueryNode)) {
      return new BooleanModifierNode(node, mod);
      
    } else {
      ModifierQueryNode modNode = (ModifierQueryNode) node;
      
      if (modNode.getModifier() == Modifier.MOD_NONE) {
        return new ModifierQueryNode(modNode.getChild(), mod);
      }
      
    }
    
    return node;
    
  }
  
  protected void tagModifierButDoNotOverride(QueryNode node, Modifier mod) {
    if (node instanceof ModifierQueryNode) {
      ModifierQueryNode modNode = (ModifierQueryNode) node;
      if (modNode.getModifier() == Modifier.MOD_NONE) {
        node.setTag(TAG_MODIFIER, mod);
      }
    } else {
      node.setTag(TAG_MODIFIER, ModifierQueryNode.Modifier.MOD_REQ);
    }
  }
  
  @Override
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler) {
    this.queryConfigHandler = queryConfigHandler;
    
  }
  
  @Override
  public QueryConfigHandler getQueryConfigHandler() {
    return queryConfigHandler;
  }
  
}

