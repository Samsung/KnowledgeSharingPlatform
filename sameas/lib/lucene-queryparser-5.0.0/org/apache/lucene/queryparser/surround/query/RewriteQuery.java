package org.apache.lucene.queryparser.surround.query;
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
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

abstract class RewriteQuery<SQ extends SrndQuery> extends Query {
  protected final SQ srndQuery;
  protected final String fieldName;
  protected final BasicQueryFactory qf;

  RewriteQuery(
      SQ srndQuery,
      String fieldName,
      BasicQueryFactory qf) {
    this.srndQuery = srndQuery;
    this.fieldName = fieldName;
    this.qf = qf;
  }

  @Override
  abstract public Query rewrite(IndexReader reader) throws IOException;

  @Override
  public String toString() {
    return toString(null);
  }

  @Override
  public String toString(String field) {
    return getClass().getName()
    + (field == null ? "" : "(unused: " + field + ")")
    + "(" + fieldName
    + ", " + srndQuery.toString()
    + ", " + qf.toString()
    + ")";
  }

  @Override
  public int hashCode() {
    return getClass().hashCode()
    ^ fieldName.hashCode()
    ^ qf.hashCode()
    ^ srndQuery.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (! getClass().equals(obj.getClass()))
      return false;
    RewriteQuery other = (RewriteQuery)obj;
    return fieldName.equals(other.fieldName)
  && qf.equals(other.qf)
  && srndQuery.equals(other.srndQuery);
  }

  /** 
   * Not supported by this query.
   * @throws UnsupportedOperationException always: clone is not supported. */
  @Override
  public RewriteQuery clone() {
    throw new UnsupportedOperationException();
  }
}

