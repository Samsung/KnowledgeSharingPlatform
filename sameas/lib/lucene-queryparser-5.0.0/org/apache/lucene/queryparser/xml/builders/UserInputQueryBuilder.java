package org.apache.lucene.queryparser.xml.builders;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryparser.xml.DOMUtils;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.w3c.dom.Element;

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

/**
 * UserInputQueryBuilder uses 1 of 2 strategies for thread-safe parsing:
 * 1) Synchronizing access to "parse" calls on a previously supplied QueryParser
 * or..
 * 2) creating a new QueryParser object for each parse request
 */
public class UserInputQueryBuilder implements QueryBuilder {

  private QueryParser unSafeParser;
  private Analyzer analyzer;
  private String defaultField;

  /**
   * This constructor has the disadvantage of not being able to change choice of default field name
   *
   * @param parser thread un-safe query parser
   */
  public UserInputQueryBuilder(QueryParser parser) {
    this.unSafeParser = parser;
  }

  public UserInputQueryBuilder(String defaultField, Analyzer analyzer) {
    this.analyzer = analyzer;
    this.defaultField = defaultField;
  }

  /* (non-Javadoc)
    * @see org.apache.lucene.xmlparser.QueryObjectBuilder#process(org.w3c.dom.Element)
    */

  @Override
  public Query getQuery(Element e) throws ParserException {
    String text = DOMUtils.getText(e);
    try {
      Query q = null;
      if (unSafeParser != null) {
        //synchronize on unsafe parser
        synchronized (unSafeParser) {
          q = unSafeParser.parse(text);
        }
      } else {
        String fieldName = DOMUtils.getAttribute(e, "fieldName", defaultField);
        //Create new parser
        QueryParser parser = createQueryParser(fieldName, analyzer);
        q = parser.parse(text);
      }
      q.setBoost(DOMUtils.getAttribute(e, "boost", 1.0f));
      return q;
    } catch (ParseException e1) {
      throw new ParserException(e1.getMessage());
    }
  }

  /**
   * Method to create a QueryParser - designed to be overridden
   *
   * @return QueryParser
   */
  protected QueryParser createQueryParser(String fieldName, Analyzer analyzer) {
    return new QueryParser(fieldName, analyzer);
  }

}
