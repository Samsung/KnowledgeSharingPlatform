/*
 * Created on 25-Jan-2006
 */
package org.apache.lucene.queryparser.xml.builders;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.xml.DOMUtils;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
 * Builder for {@link BooleanQuery}
 */
public class BooleanQueryBuilder implements QueryBuilder {

  private final QueryBuilder factory;

  public BooleanQueryBuilder(QueryBuilder factory) {
    this.factory = factory;
  }

  /* (non-Javadoc)
    * @see org.apache.lucene.xmlparser.QueryObjectBuilder#process(org.w3c.dom.Element)
    */

  @Override
  public Query getQuery(Element e) throws ParserException {
    BooleanQuery bq = new BooleanQuery(DOMUtils.getAttribute(e, "disableCoord", false));
    bq.setMinimumNumberShouldMatch(DOMUtils.getAttribute(e, "minimumNumberShouldMatch", 0));
    bq.setBoost(DOMUtils.getAttribute(e, "boost", 1.0f));

    NodeList nl = e.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node.getNodeName().equals("Clause")) {
        Element clauseElem = (Element) node;
        BooleanClause.Occur occurs = getOccursValue(clauseElem);

        Element clauseQuery = DOMUtils.getFirstChildOrFail(clauseElem);
        Query q = factory.getQuery(clauseQuery);
        bq.add(new BooleanClause(q, occurs));
      }
    }

    return bq;
  }

  static BooleanClause.Occur getOccursValue(Element clauseElem) throws ParserException {
    String occs = clauseElem.getAttribute("occurs");
    BooleanClause.Occur occurs = BooleanClause.Occur.SHOULD;
    if ("must".equalsIgnoreCase(occs)) {
      occurs = BooleanClause.Occur.MUST;
    } else {
      if ("mustNot".equalsIgnoreCase(occs)) {
        occurs = BooleanClause.Occur.MUST_NOT;
      } else {
        if (("should".equalsIgnoreCase(occs)) || ("".equals(occs))) {
          occurs = BooleanClause.Occur.SHOULD;
        } else {
          if (occs != null) {
            throw new ParserException("Invalid value for \"occurs\" attribute of clause:" + occs);
          }
        }
      }
    }
    return occurs;

  }

}
