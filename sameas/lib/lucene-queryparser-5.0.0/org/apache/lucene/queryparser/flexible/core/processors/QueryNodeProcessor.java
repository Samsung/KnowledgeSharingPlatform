package org.apache.lucene.queryparser.flexible.core.processors;

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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

/**
 * <p>
 * A {@link QueryNodeProcessor} is an interface for classes that process a
 * {@link QueryNode} tree.
 * <p>
 * </p>
 * The implementor of this class should perform some operation on a query node
 * tree and return the same or another query node tree.
 * <p>
 * </p>
 * It also may carry a {@link QueryConfigHandler} object that contains
 * configuration about the query represented by the query tree or the
 * collection/index where it's intended to be executed.
 * <p>
 * </p>
 * In case there is any {@link QueryConfigHandler} associated to the query tree
 * to be processed, it should be set using
 * {@link QueryNodeProcessor#setQueryConfigHandler(QueryConfigHandler)} before
 * {@link QueryNodeProcessor#process(QueryNode)} is invoked.
 * 
 * @see QueryNode
 * @see QueryNodeProcessor
 * @see QueryConfigHandler
 */
public interface QueryNodeProcessor {

  /**
   * Processes a query node tree. It may return the same or another query tree.
   * I should never return <code>null</code>.
   * 
   * @param queryTree
   *          tree root node
   * 
   * @return the processed query tree
   */
  public QueryNode process(QueryNode queryTree) throws QueryNodeException;

  /**
   * Sets the {@link QueryConfigHandler} associated to the query tree.
   */
  public void setQueryConfigHandler(QueryConfigHandler queryConfigHandler);

  /**
   * Returns the {@link QueryConfigHandler} associated to the query tree if any,
   * otherwise it returns <code>null</code>
   * 
   * @return the {@link QueryConfigHandler} associated to the query tree if any,
   *         otherwise it returns <code>null</code>
   */
  public QueryConfigHandler getQueryConfigHandler();

}
