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

import org.apache.lucene.queryparser.flexible.messages.Message;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

/**
 * This should be thrown when an exception happens during the query parsing from
 * string to the query node tree.
 * 
 * @see QueryNodeException
 * @see SyntaxParser
 * @see QueryNode
 */
public class QueryNodeParseException extends QueryNodeException {

  private CharSequence query;

  private int beginColumn = -1;

  private int beginLine = -1;

  private String errorToken = "";

  public QueryNodeParseException(Message message) {
    super(message);
  }

  public QueryNodeParseException(Throwable throwable) {
    super(throwable);
  }

  public QueryNodeParseException(Message message, Throwable throwable) {
    super(message, throwable);
  }

  public void setQuery(CharSequence query) {
    this.query = query;
    this.message = new MessageImpl(
        QueryParserMessages.INVALID_SYNTAX_CANNOT_PARSE, query, "");
  }

  public CharSequence getQuery() {
    return this.query;
  }

  /**
   * @param errorToken
   *          the errorToken in the query
   */
  protected void setErrorToken(String errorToken) {
    this.errorToken = errorToken;
  }

  public String getErrorToken() {
    return this.errorToken;
  }

  public void setNonLocalizedMessage(Message message) {
    this.message = message;
  }

  /**
   * For EndOfLine and EndOfFile ("&lt;EOF&gt;") parsing problems the last char in the
   * string is returned For the case where the parser is not able to figure out
   * the line and column number -1 will be returned
   * 
   * @return line where the problem was found
   */
  public int getBeginLine() {
    return this.beginLine;
  }

  /**
   * For EndOfLine and EndOfFile ("&lt;EOF&gt;") parsing problems the last char in the
   * string is returned For the case where the parser is not able to figure out
   * the line and column number -1 will be returned
   * 
   * @return column of the first char where the problem was found
   */
  public int getBeginColumn() {
    return this.beginColumn;
  }

  /**
   * @param beginLine
   *          the beginLine to set
   */
  protected void setBeginLine(int beginLine) {
    this.beginLine = beginLine;
  }

  /**
   * @param beginColumn
   *          the beginColumn to set
   */
  protected void setBeginColumn(int beginColumn) {
    this.beginColumn = beginColumn;
  }
}
