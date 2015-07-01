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

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/** Lowest level base class for surround queries */
public abstract class SrndQuery implements Cloneable {
  public SrndQuery() {}
  
  private float weight = (float) 1.0;
  private boolean weighted = false;

  public void setWeight(float w) {
    weight = w; /* as parsed from the query text */
    weighted = true;
  } 
  public boolean isWeighted() {return weighted;}
  public float getWeight() { return weight; }
  public String getWeightString() {return Float.toString(getWeight());}

  public String getWeightOperator() {return "^";}

  protected void weightToString(StringBuilder r) { /* append the weight part of a query */
    if (isWeighted()) {
      r.append(getWeightOperator());
      r.append(getWeightString());
    }
  }
  
  public Query makeLuceneQueryField(String fieldName, BasicQueryFactory qf){
    Query q = makeLuceneQueryFieldNoBoost(fieldName, qf);
    if (isWeighted()) {
      q.setBoost(getWeight() * q.getBoost()); /* weight may be at any level in a SrndQuery */
    }
    return q;
  }
  
  public abstract Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf);
  
  /** This method is used by {@link #hashCode()} and {@link #equals(Object)},
   *  see LUCENE-2945.
   */
  @Override
  public abstract String toString();
  
  public boolean isFieldsSubQueryAcceptable() {return true;}
    
  @Override
  public SrndQuery clone() {
    try {
      return (SrndQuery)super.clone();
    } catch (CloneNotSupportedException cns) {
      throw new Error(cns);
    }
  }

  /** For subclasses of {@link SrndQuery} within the package
   *  {@link org.apache.lucene.queryparser.surround.query}
   *  it is not necessary to override this method,
   *  @see #toString()
   */
  @Override
  public int hashCode() {
    return getClass().hashCode() ^ toString().hashCode();
  }

  /** For subclasses of {@link SrndQuery} within the package
   *  {@link org.apache.lucene.queryparser.surround.query}
   *  it is not necessary to override this method,
   *  @see #toString()
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (! getClass().equals(obj.getClass()))
      return false;
    return toString().equals(obj.toString());
  }

  /** An empty Lucene query */
  public final static Query theEmptyLcnQuery = new BooleanQuery() { /* no changes allowed */
    @Override
    public void setBoost(float boost) {
      throw new UnsupportedOperationException();
    }
    @Override
    public void add(BooleanClause clause) {
      throw new UnsupportedOperationException();
    }
    @Override
    public void add(Query query, BooleanClause.Occur occur) {
      throw new UnsupportedOperationException();
    }
  };
}

