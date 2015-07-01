package org.apache.lucene.queryparser.flexible.precedence.processors;

/*
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

import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.*;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode.Modifier;

/**
 * <p>
 * This processor is used to apply the correct {@link ModifierQueryNode} to {@link BooleanQueryNode}s children.
 * </p>
 * <p>
 * It walks through the query node tree looking for {@link BooleanQueryNode}s. If an {@link AndQueryNode} is found,
 * every child, which is not a {@link ModifierQueryNode} or the {@link ModifierQueryNode} 
 * is {@link Modifier#MOD_NONE}, becomes a {@link Modifier#MOD_REQ}. For any other
 * {@link BooleanQueryNode} which is not an {@link OrQueryNode}, it checks the default operator is {@link Operator#AND},
 * if it is, the same operation when an {@link AndQueryNode} is found is applied to it.
 * </p>
 * 
 * @see ConfigurationKeys#DEFAULT_OPERATOR
 * @see PrecedenceQueryParser#setDefaultOperator
 */
public class BooleanModifiersQueryNodeProcessor extends QueryNodeProcessorImpl {

  private ArrayList<QueryNode> childrenBuffer = new ArrayList<>();

  private Boolean usingAnd = false;

  public BooleanModifiersQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    Operator op = getQueryConfigHandler().get(ConfigurationKeys.DEFAULT_OPERATOR);
    
    if (op == null) {
      throw new IllegalArgumentException(
          "StandardQueryConfigHandler.ConfigurationKeys.DEFAULT_OPERATOR should be set on the QueryConfigHandler");
    }

    this.usingAnd = StandardQueryConfigHandler.Operator.AND == op;

    return super.process(queryTree);

  }

  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {

    if (node instanceof AndQueryNode) {
      this.childrenBuffer.clear();
      List<QueryNode> children = node.getChildren();

      for (QueryNode child : children) {
        this.childrenBuffer.add(applyModifier(child, ModifierQueryNode.Modifier.MOD_REQ));
      }

      node.set(this.childrenBuffer);

    } else if (this.usingAnd && node instanceof BooleanQueryNode
        && !(node instanceof OrQueryNode)) {

      this.childrenBuffer.clear();
      List<QueryNode> children = node.getChildren();

      for (QueryNode child : children) {
        this.childrenBuffer.add(applyModifier(child, ModifierQueryNode.Modifier.MOD_REQ));
      }

      node.set(this.childrenBuffer);

    }

    return node;

  }

  private QueryNode applyModifier(QueryNode node, ModifierQueryNode.Modifier mod) {

    // check if modifier is not already defined and is default
    if (!(node instanceof ModifierQueryNode)) {
      return new ModifierQueryNode(node, mod);

    } else {
      ModifierQueryNode modNode = (ModifierQueryNode) node;

      if (modNode.getModifier() == ModifierQueryNode.Modifier.MOD_NONE) {
        return new ModifierQueryNode(modNode.getChild(), mod);
      }

    }

    return node;

  }

  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {

    return children;

  }

}
