package org.apache.lucene.queryparser.flexible.core;

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

import java.util.Locale;

import org.apache.lucene.queryparser.flexible.messages.Message;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.messages.NLS;
import org.apache.lucene.queryparser.flexible.messages.NLSException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

/**
 * <p>
 * This exception should be thrown if something wrong happens when dealing with
 * {@link QueryNode}s.
 * </p>
 * <p>
 * It also supports NLS messages.
 * </p>
 * 
 * @see Message
 * @see NLS
 * @see NLSException
 * @see QueryNode
 */
public class QueryNodeException extends Exception implements NLSException {

  protected Message message = new MessageImpl(QueryParserMessages.EMPTY_MESSAGE);

  public QueryNodeException(Message message) {
    super(message.getKey());

    this.message = message;

  }

  public QueryNodeException(Throwable throwable) {
    super(throwable);
  }

  public QueryNodeException(Message message, Throwable throwable) {
    super(message.getKey(), throwable);

    this.message = message;

  }

  @Override
  public Message getMessageObject() {
    return this.message;
  }

  @Override
  public String getMessage() {
    return getLocalizedMessage();
  }

  @Override
  public String getLocalizedMessage() {
    return getLocalizedMessage(Locale.getDefault());
  }

  public String getLocalizedMessage(Locale locale) {
    return this.message.getLocalizedMessage(locale);
  }

  @Override
  public String toString() {
    return this.message.getKey() + ": " + getLocalizedMessage();
  }

}
