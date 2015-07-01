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

import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;

import java.io.IOException;


/**
 * Query that matches String prefixes
 */
public class SrndPrefixQuery extends SimpleTerm {
  private final BytesRef prefixRef;
  public SrndPrefixQuery(String prefix, boolean quoted, char truncator) {
    super(quoted);
    this.prefix = prefix;
    prefixRef = new BytesRef(prefix);
    this.truncator = truncator;
  }

  private final String prefix;
  public String getPrefix() {return prefix;}
  
  private final char truncator;
  public char getSuffixOperator() {return truncator;}
  
  public Term getLucenePrefixTerm(String fieldName) {
    return new Term(fieldName, getPrefix());
  }
  
  @Override
  public String toStringUnquoted() {return getPrefix();}
  
  @Override
  protected void suffixToString(StringBuilder r) {r.append(getSuffixOperator());}
  
  @Override
  public void visitMatchingTerms(
    IndexReader reader,
    String fieldName,
    MatchingTermVisitor mtv) throws IOException
  {
    /* inspired by PrefixQuery.rewrite(): */
    Terms terms = MultiFields.getTerms(reader, fieldName);
    if (terms != null) {
      TermsEnum termsEnum = terms.iterator(null);

      boolean skip = false;
      TermsEnum.SeekStatus status = termsEnum.seekCeil(new BytesRef(getPrefix()));
      if (status == TermsEnum.SeekStatus.FOUND) {
        mtv.visitMatchingTerm(getLucenePrefixTerm(fieldName));
      } else if (status == TermsEnum.SeekStatus.NOT_FOUND) {
        if (StringHelper.startsWith(termsEnum.term(), prefixRef)) {
          mtv.visitMatchingTerm(new Term(fieldName, termsEnum.term().utf8ToString()));
        } else {
          skip = true;
        }
      } else {
        // EOF
        skip = true;
      }

      if (!skip) {
        while(true) {
          BytesRef text = termsEnum.next();
          if (text != null && StringHelper.startsWith(text, prefixRef)) {
            mtv.visitMatchingTerm(new Term(fieldName, text.utf8ToString()));
          } else {
            break;
          }
        }
      }
    }
  }
}
