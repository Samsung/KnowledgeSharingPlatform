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
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;

import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Query that matches wildcards
 */
public class SrndTruncQuery extends SimpleTerm {
  public SrndTruncQuery(String truncated, char unlimited, char mask) {
    super(false); /* not quoted */
    this.truncated = truncated;
    this.unlimited = unlimited;
    this.mask = mask;
    truncatedToPrefixAndPattern();
  }
  
  private final String truncated;
  private final char unlimited;
  private final char mask;
  
  private String prefix;
  private BytesRef prefixRef;
  private Pattern pattern;
  
  
  public String getTruncated() {return truncated;}
  
  @Override
  public String toStringUnquoted() {return getTruncated();}

  
  protected boolean matchingChar(char c) {
    return (c != unlimited) && (c != mask);
  }

  protected void appendRegExpForChar(char c, StringBuilder re) {
    if (c == unlimited)
      re.append(".*");
    else if (c == mask)
      re.append(".");
    else
      re.append(c);
  }
  
  protected void truncatedToPrefixAndPattern() {
    int i = 0;
    while ((i < truncated.length()) && matchingChar(truncated.charAt(i))) {
      i++;
    }
    prefix = truncated.substring(0, i);
    prefixRef = new BytesRef(prefix);
    
    StringBuilder re = new StringBuilder();
    while (i < truncated.length()) {
      appendRegExpForChar(truncated.charAt(i), re);
      i++;
    }
    pattern = Pattern.compile(re.toString());
  }
  
  @Override
  public void visitMatchingTerms(
    IndexReader reader,
    String fieldName,
    MatchingTermVisitor mtv) throws IOException
  {
    int prefixLength = prefix.length();
    Terms terms = MultiFields.getTerms(reader, fieldName);
    if (terms != null) {
      Matcher matcher = pattern.matcher("");
      try {
        TermsEnum termsEnum = terms.iterator(null);

        TermsEnum.SeekStatus status = termsEnum.seekCeil(prefixRef);
        BytesRef text;
        if (status == TermsEnum.SeekStatus.FOUND) {
          text = prefixRef;
        } else if (status == TermsEnum.SeekStatus.NOT_FOUND) {
          text = termsEnum.term();
        } else {
          text = null;
        }

        while(text != null) {
          if (text != null && StringHelper.startsWith(text, prefixRef)) {
            String textString = text.utf8ToString();
            matcher.reset(textString.substring(prefixLength));
            if (matcher.matches()) {
              mtv.visitMatchingTerm(new Term(fieldName, textString));
            }
          } else {
            break;
          }
          text = termsEnum.next();
        }
      } finally {
        matcher.reset();
      }
    }
  }
}
