package org.apache.lucene.queryparser.flexible.core.builders;

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

import java.util.HashMap;
import java.util.List;

import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldableNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;

/**
 * This class should be used when there is a builder for each type of node.
 * 
 * The type of node may be defined in 2 different ways: - by the field name,
 * when the node implements the {@link FieldableNode} interface - by its class,
 * it keeps checking the class and all the interfaces and classes this class
 * implements/extends until it finds a builder for that class/interface
 * 
 * This class always check if there is a builder for the field name before it
 * checks for the node class. So, field name builders have precedence over class
 * builders.
 * 
 * When a builder is found for a node, it's called and the node is passed to the
 * builder. If the returned built object is not <code>null</code>, it's tagged
 * on the node using the tag {@link QueryTreeBuilder#QUERY_TREE_BUILDER_TAGID}.
 * 
 * The children are usually built before the parent node. However, if a builder
 * associated to a node is an instance of {@link QueryTreeBuilder}, the node is
 * delegated to this builder and it's responsible to build the node and its
 * children.
 * 
 * @see QueryBuilder
 */
public class QueryTreeBuilder implements QueryBuilder {

  /**
   * This tag is used to tag the nodes in a query tree with the built objects
   * produced from their own associated builder.
   */
  public static final String QUERY_TREE_BUILDER_TAGID = QueryTreeBuilder.class
      .getName();

  private HashMap<Class<? extends QueryNode>, QueryBuilder> queryNodeBuilders;

  private HashMap<String, QueryBuilder> fieldNameBuilders;

  /**
   * {@link QueryTreeBuilder} constructor.
   */
  public QueryTreeBuilder() {
    // empty constructor
  }

  /**
   * Associates a field name with a builder.
   * 
   * @param fieldName the field name
   * @param builder the builder to be associated
   */
  public void setBuilder(CharSequence fieldName, QueryBuilder builder) {

    if (this.fieldNameBuilders == null) {
      this.fieldNameBuilders = new HashMap<>();
    }

    this.fieldNameBuilders.put(fieldName.toString(), builder);


  }

  /**
   * Associates a class with a builder
   * 
   * @param queryNodeClass the class
   * @param builder the builder to be associated
   */
  public void setBuilder(Class<? extends QueryNode> queryNodeClass,
      QueryBuilder builder) {

    if (this.queryNodeBuilders == null) {
      this.queryNodeBuilders = new HashMap<>();
    }

    this.queryNodeBuilders.put(queryNodeClass, builder);

  }

  private void process(QueryNode node) throws QueryNodeException {

    if (node != null) {
      QueryBuilder builder = getBuilder(node);

      if (!(builder instanceof QueryTreeBuilder)) {
        List<QueryNode> children = node.getChildren();

        if (children != null) {

          for (QueryNode child : children) {
            process(child);
          }

        }

      }

      processNode(node, builder);

    }

  }

  private QueryBuilder getBuilder(QueryNode node) {
    QueryBuilder builder = null;

    if (this.fieldNameBuilders != null && node instanceof FieldableNode) {
      CharSequence field = ((FieldableNode) node).getField();

      if (field != null) {
        field = field.toString();
      }

      builder = this.fieldNameBuilders.get(field);

    }

    if (builder == null && this.queryNodeBuilders != null) {

      Class<?> clazz = node.getClass();

      do {
        builder = getQueryBuilder(clazz);

        if (builder == null) {
          Class<?>[] classes = clazz.getInterfaces();

          for (Class<?> actualClass : classes) {
            builder = getQueryBuilder(actualClass);

            if (builder != null) {
              break;
            }

          }

        }

      } while (builder == null && (clazz = clazz.getSuperclass()) != null);

    }

    return builder;

  }

  private void processNode(QueryNode node, QueryBuilder builder)
      throws QueryNodeException {

    if (builder == null) {

      throw new QueryNodeException(new MessageImpl(
          QueryParserMessages.LUCENE_QUERY_CONVERSION_ERROR, node
              .toQueryString(new EscapeQuerySyntaxImpl()), node.getClass()
              .getName()));

    }

    Object obj = builder.build(node);

    if (obj != null) {
      node.setTag(QUERY_TREE_BUILDER_TAGID, obj);
    }

  }

  private QueryBuilder getQueryBuilder(Class<?> clazz) {

    if (QueryNode.class.isAssignableFrom(clazz)) {
      return this.queryNodeBuilders.get(clazz);
    }

    return null;

  }

  /**
   * Builds some kind of object from a query tree. Each node in the query tree
   * is built using an specific builder associated to it.
   * 
   * @param queryNode the query tree root node
   * 
   * @return the built object
   * 
   * @throws QueryNodeException if some node builder throws a
   *         {@link QueryNodeException} or if there is a node which had no
   *         builder associated to it
   */
  @Override
  public Object build(QueryNode queryNode) throws QueryNodeException {
    process(queryNode);

    return queryNode.getTag(QUERY_TREE_BUILDER_TAGID);

  }

}
