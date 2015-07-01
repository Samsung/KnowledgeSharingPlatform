package org.apache.lucene.queryparser.xml.builders;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.queryparser.xml.ParserException;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
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
 * Factory for {@link SpanQueryBuilder}s
 */
public class SpanQueryBuilderFactory implements SpanQueryBuilder {

  private final Map<String, SpanQueryBuilder> builders = new HashMap<>();

  @Override
  public Query getQuery(Element e) throws ParserException {
    return getSpanQuery(e);
  }

  public void addBuilder(String nodeName, SpanQueryBuilder builder) {
    builders.put(nodeName, builder);
  }

  @Override
  public SpanQuery getSpanQuery(Element e) throws ParserException {
    SpanQueryBuilder builder = builders.get(e.getNodeName());
    if (builder == null) {
      throw new ParserException("No SpanQueryObjectBuilder defined for node " + e.getNodeName());
    }
    return builder.getSpanQuery(e);
  }

}
