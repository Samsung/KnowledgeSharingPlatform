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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.lucene.search.Query;

/**
 * Forms an OR query of the provided query across multiple fields.
 */
public class FieldsQuery extends SrndQuery { /* mostly untested */
  private SrndQuery q;
  private List<String> fieldNames;
  private final char fieldOp;
  private final String OrOperatorName = "OR"; /* for expanded queries, not normally visible */
  
  public FieldsQuery(SrndQuery q, List<String> fieldNames, char fieldOp) {
    this.q = q;
    this.fieldNames = fieldNames;
    this.fieldOp = fieldOp;
  }
  
  public FieldsQuery(SrndQuery q, String fieldName, char fieldOp) {
    this.q = q;
    fieldNames = new ArrayList<>();
    fieldNames.add(fieldName);
    this.fieldOp = fieldOp;
  }
  
  @Override
  public boolean isFieldsSubQueryAcceptable() {
    return false;
  }
  
  public Query makeLuceneQueryNoBoost(BasicQueryFactory qf) {
    if (fieldNames.size() == 1) { /* single field name: no new queries needed */
      return q.makeLuceneQueryFieldNoBoost(fieldNames.get(0), qf);
    } else { /* OR query over the fields */
      List<SrndQuery> queries = new ArrayList<>();
      Iterator<String> fni = getFieldNames().listIterator();
      SrndQuery qc;
      while (fni.hasNext()) {
        qc = q.clone();
        queries.add( new FieldsQuery( qc, fni.next(), fieldOp));
      }
      OrQuery oq = new OrQuery(queries,
                              true /* infix OR for field names */,
                              OrOperatorName);
      // System.out.println(getClass().toString() + ", fields expanded: " + oq.toString()); /* needs testing */
      return oq.makeLuceneQueryField(null, qf);
    }
  }

  @Override
  public Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf) {
    return makeLuceneQueryNoBoost(qf); /* use this.fieldNames instead of fieldName */
  }

  
  public List<String> getFieldNames() {return fieldNames;}

  public char getFieldOperator() { return fieldOp;}
  
  @Override
  public String toString() {
    StringBuilder r = new StringBuilder();
    r.append("(");
    fieldNamesToString(r);
    r.append(q.toString());
    r.append(")");
    return r.toString();
  }
  
  protected void fieldNamesToString(StringBuilder r) {
    Iterator<String> fni = getFieldNames().listIterator();
    while (fni.hasNext()) {
      r.append(fni.next());
      r.append(getFieldOperator());
    }
  }
}

