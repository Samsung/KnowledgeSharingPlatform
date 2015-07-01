package org.apache.lucene.queryparser.flexible.standard.processors;

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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode.Modifier;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.standard.nodes.BooleanModifierNode;

/**
 * The {@link SyntaxParser}
 * generates query node trees that consider the boolean operator precedence, but
 * Lucene current syntax does not support boolean precedence, so this processor
 * remove all the precedence and apply the equivalent modifier according to the
 * boolean operation defined on an specific query node. <br/>
 * <br/>
 * If there is a {@link GroupQueryNode} in the query node tree, the query node
 * tree is not merged with the one above it.
 * 
 * Example: TODO: describe a good example to show how this processor works
 * 
 * @see org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler
 * @deprecated use {@link BooleanQuery2ModifierNodeProcessor} instead
 */
public class GroupQueryNodeProcessor implements QueryNodeProcessor {

  private ArrayList<QueryNode> queryNodeList;

  private boolean latestNodeVerified;

  private QueryConfigHandler queryConfig;

  private Boolean usingAnd = false;

  public GroupQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    Operator defaultOperator = getQueryConfigHandler().get(ConfigurationKeys.DEFAULT_OPERATOR);
    
    if (defaultOperator == null) {
      throw new IllegalArgumentException(
          "DEFAULT_OPERATOR should be set on the QueryConfigHandler");
    }

    this.usingAnd = StandardQueryConfigHandler.Operator.AND == defaultOperator;

    if (queryTree instanceof GroupQueryNode) {
      queryTree = ((GroupQueryNode) queryTree).getChild();
    }

    this.queryNodeList = new ArrayList<>();
    this.latestNodeVerified = false;
    readTree(queryTree);

    List<QueryNode> actualQueryNodeList = this.queryNodeList;

    for (int i = 0; i < actualQueryNodeList.size(); i++) {
      QueryNode node = actualQueryNodeList.get(i);

      if (node instanceof GroupQueryNode) {
        actualQueryNodeList.set(i, process(node));
      }

    }

    this.usingAnd = false;

    if (queryTree instanceof BooleanQueryNode) {
      queryTree.set(actualQueryNodeList);

      return queryTree;

    } else {
      return new BooleanQueryNode(actualQueryNodeList);
    }

  }

  /**
   */
  private QueryNode applyModifier(QueryNode node, QueryNode parent) {

    if (this.usingAnd) {

      if (parent instanceof OrQueryNode) {

        if (node instanceof ModifierQueryNode) {

          ModifierQueryNode modNode = (ModifierQueryNode) node;

          if (modNode.getModifier() == Modifier.MOD_REQ) {
            return modNode.getChild();
          }

        }

      } else {

        if (node instanceof ModifierQueryNode) {

          ModifierQueryNode modNode = (ModifierQueryNode) node;

          if (modNode.getModifier() == Modifier.MOD_NONE) {
            return new BooleanModifierNode(modNode.getChild(), Modifier.MOD_REQ);
          }

        } else {
          return new BooleanModifierNode(node, Modifier.MOD_REQ);
        }

      }

    } else {

      if (node.getParent() instanceof AndQueryNode) {

        if (node instanceof ModifierQueryNode) {

          ModifierQueryNode modNode = (ModifierQueryNode) node;

          if (modNode.getModifier() == Modifier.MOD_NONE) {
            return new BooleanModifierNode(modNode.getChild(), Modifier.MOD_REQ);
          }

        } else {
          return new BooleanModifierNode(node, Modifier.MOD_REQ);
        }

      }

    }

    return node;

  }

  private void readTree(QueryNode node) {

    if (node instanceof BooleanQueryNode) {
      List<QueryNode> children = node.getChildren();

      if (children != null && children.size() > 0) {

        for (int i = 0; i < children.size() - 1; i++) {
          readTree(children.get(i));
        }

        processNode(node);
        readTree(children.get(children.size() - 1));

      } else {
        processNode(node);
      }

    } else {
      processNode(node);
    }

  }

  private void processNode(QueryNode node) {

    if (node instanceof AndQueryNode || node instanceof OrQueryNode) {

      if (!this.latestNodeVerified && !this.queryNodeList.isEmpty()) {
        this.queryNodeList.add(applyModifier(this.queryNodeList
            .remove(this.queryNodeList.size() - 1), node));
        this.latestNodeVerified = true;

      }

    } else if (!(node instanceof BooleanQueryNode)) {
      this.queryNodeList.add(applyModifier(node, node.getParent()));
      this.latestNodeVerified = false;

    }

  }

  @Override
  public QueryConfigHandler getQueryConfigHandler() {
    return this.queryConfig;
  }

  @Override
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler) {
    this.queryConfig = queryConfigHandler;
  }

}
