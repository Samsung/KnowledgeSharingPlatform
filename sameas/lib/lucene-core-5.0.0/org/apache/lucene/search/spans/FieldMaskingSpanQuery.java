package org.apache.lucene.search.spans;

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
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;

/**
 * <p>Wrapper to allow {@link SpanQuery} objects participate in composite 
 * single-field SpanQueries by 'lying' about their search field. That is, 
 * the masked SpanQuery will function as normal, 
 * but {@link SpanQuery#getField()} simply hands back the value supplied 
 * in this class's constructor.</p>
 * 
 * <p>This can be used to support Queries like {@link SpanNearQuery} or 
 * {@link SpanOrQuery} across different fields, which is not ordinarily 
 * permitted.</p>
 * 
 * <p>This can be useful for denormalized relational data: for example, when 
 * indexing a document with conceptually many 'children': </p>
 * 
 * <pre>
 *  teacherid: 1
 *  studentfirstname: james
 *  studentsurname: jones
 *  
 *  teacherid: 2
 *  studenfirstname: james
 *  studentsurname: smith
 *  studentfirstname: sally
 *  studentsurname: jones
 * </pre>
 * 
 * <p>a SpanNearQuery with a slop of 0 can be applied across two 
 * {@link SpanTermQuery} objects as follows:
 * <pre class="prettyprint">
 *    SpanQuery q1  = new SpanTermQuery(new Term("studentfirstname", "james"));
 *    SpanQuery q2  = new SpanTermQuery(new Term("studentsurname", "jones"));
 *    SpanQuery q2m = new FieldMaskingSpanQuery(q2, "studentfirstname");
 *    Query q = new SpanNearQuery(new SpanQuery[]{q1, q2m}, -1, false);
 * </pre>
 * to search for 'studentfirstname:james studentsurname:jones' and find 
 * teacherid 1 without matching teacherid 2 (which has a 'james' in position 0 
 * and 'jones' in position 1). </p>
 * 
 * <p>Note: as {@link #getField()} returns the masked field, scoring will be 
 * done using the Similarity and collection statistics of the field name supplied,
 * but with the term statistics of the real field. This may lead to exceptions,
 * poor performance, and unexpected scoring behaviour.</p>
 */
public class FieldMaskingSpanQuery extends SpanQuery {
  private SpanQuery maskedQuery;
  private String field;
    
  public FieldMaskingSpanQuery(SpanQuery maskedQuery, String maskedField) {
    this.maskedQuery = maskedQuery;
    this.field = maskedField;
  }

  @Override
  public String getField() {
    return field;
  }

  public SpanQuery getMaskedQuery() {
    return maskedQuery;
  }

  // :NOTE: getBoost and setBoost are not proxied to the maskedQuery
  // ...this is done to be more consistent with things like SpanFirstQuery
  
  @Override
  public Spans getSpans(LeafReaderContext context, Bits acceptDocs, Map<Term,TermContext> termContexts) throws IOException {
    return maskedQuery.getSpans(context, acceptDocs, termContexts);
  }

  @Override
  public void extractTerms(Set<Term> terms) {
    maskedQuery.extractTerms(terms);
  }  

  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    return maskedQuery.createWeight(searcher);
  }

  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    FieldMaskingSpanQuery clone = null;

    SpanQuery rewritten = (SpanQuery) maskedQuery.rewrite(reader);
    if (rewritten != maskedQuery) {
      clone = (FieldMaskingSpanQuery) this.clone();
      clone.maskedQuery = rewritten;
    }

    if (clone != null) {
      return clone;
    } else {
      return this;
    }
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("mask(");
    buffer.append(maskedQuery.toString(field));
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    buffer.append(" as ");
    buffer.append(this.field);
    return buffer.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FieldMaskingSpanQuery))
      return false;
    FieldMaskingSpanQuery other = (FieldMaskingSpanQuery) o;
    return (this.getField().equals(other.getField())
            && (this.getBoost() == other.getBoost())
            && this.getMaskedQuery().equals(other.getMaskedQuery()));

  }
  
  @Override
  public int hashCode() {
    return getMaskedQuery().hashCode()
      ^ getField().hashCode()
      ^ Float.floatToRawIntBits(getBoost());
  }
}
