package org.apache.lucene.queryparser.flexible.standard;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * This class defines utility methods to (help) parse query strings into
 * {@link Query} objects.
 */
final public class QueryParserUtil {

  /**
   * Parses a query which searches on the fields specified.
   * <p>
   * If x fields are specified, this effectively constructs:
   * 
   * <pre>
   * <code>
   * (field1:query1) (field2:query2) (field3:query3)...(fieldx:queryx)
   * </code>
   * </pre>
   * 
   * @param queries
   *          Queries strings to parse
   * @param fields
   *          Fields to search on
   * @param analyzer
   *          Analyzer to use
   * @throws IllegalArgumentException
   *           if the length of the queries array differs from the length of the
   *           fields array
   */
  public static Query parse(String[] queries, String[] fields, Analyzer analyzer)
      throws QueryNodeException {
    if (queries.length != fields.length)
      throw new IllegalArgumentException("queries.length != fields.length");
    BooleanQuery bQuery = new BooleanQuery();

    StandardQueryParser qp = new StandardQueryParser();
    qp.setAnalyzer(analyzer);

    for (int i = 0; i < fields.length; i++) {
      Query q = qp.parse(queries[i], fields[i]);

      if (q != null && // q never null, just being defensive
          (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
        bQuery.add(q, BooleanClause.Occur.SHOULD);
      }
    }
    return bQuery;
  }

  /**
   * Parses a query, searching on the fields specified. Use this if you need to
   * specify certain fields as required, and others as prohibited.
   * <p>
   * 
   * Usage:
   * <pre class="prettyprint">
   * <code>
   * String[] fields = {&quot;filename&quot;, &quot;contents&quot;, &quot;description&quot;};
   * BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
   *                BooleanClause.Occur.MUST,
   *                BooleanClause.Occur.MUST_NOT};
   * MultiFieldQueryParser.parse(&quot;query&quot;, fields, flags, analyzer);
   * </code>
   * </pre>
   *<p>
   * The code above would construct a query:
   * 
   * <pre>
   * <code>
   * (filename:query) +(contents:query) -(description:query)
   * </code>
   * </pre>
   * 
   * @param query
   *          Query string to parse
   * @param fields
   *          Fields to search on
   * @param flags
   *          Flags describing the fields
   * @param analyzer
   *          Analyzer to use
   * @throws IllegalArgumentException
   *           if the length of the fields array differs from the length of the
   *           flags array
   */
  public static Query parse(String query, String[] fields,
      BooleanClause.Occur[] flags, Analyzer analyzer) throws QueryNodeException {
    if (fields.length != flags.length)
      throw new IllegalArgumentException("fields.length != flags.length");
    BooleanQuery bQuery = new BooleanQuery();

    StandardQueryParser qp = new StandardQueryParser();
    qp.setAnalyzer(analyzer);

    for (int i = 0; i < fields.length; i++) {
      Query q = qp.parse(query, fields[i]);

      if (q != null && // q never null, just being defensive
          (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
        bQuery.add(q, flags[i]);
      }
    }
    return bQuery;
  }

  /**
   * Parses a query, searching on the fields specified. Use this if you need to
   * specify certain fields as required, and others as prohibited.
   * <p>
   * 
   * Usage:
   * <pre class="prettyprint">
   * <code>
   * String[] query = {&quot;query1&quot;, &quot;query2&quot;, &quot;query3&quot;};
   * String[] fields = {&quot;filename&quot;, &quot;contents&quot;, &quot;description&quot;};
   * BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
   *                BooleanClause.Occur.MUST,
   *                BooleanClause.Occur.MUST_NOT};
   * MultiFieldQueryParser.parse(query, fields, flags, analyzer);
   * </code>
   * </pre>
   *<p>
   * The code above would construct a query:
   * 
   * <pre>
   * <code>
   * (filename:query1) +(contents:query2) -(description:query3)
   * </code>
   * </pre>
   * 
   * @param queries
   *          Queries string to parse
   * @param fields
   *          Fields to search on
   * @param flags
   *          Flags describing the fields
   * @param analyzer
   *          Analyzer to use
   * @throws IllegalArgumentException
   *           if the length of the queries, fields, and flags array differ
   */
  public static Query parse(String[] queries, String[] fields,
      BooleanClause.Occur[] flags, Analyzer analyzer) throws QueryNodeException {
    if (!(queries.length == fields.length && queries.length == flags.length))
      throw new IllegalArgumentException(
          "queries, fields, and flags array have have different length");
    BooleanQuery bQuery = new BooleanQuery();

    StandardQueryParser qp = new StandardQueryParser();
    qp.setAnalyzer(analyzer);

    for (int i = 0; i < fields.length; i++) {
      Query q = qp.parse(queries[i], fields[i]);

      if (q != null && // q never null, just being defensive
          (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
        bQuery.add(q, flags[i]);
      }
    }
    return bQuery;
  }

  /**
   * Returns a String where those characters that TextParser expects to be
   * escaped are escaped by a preceding <code>\</code>.
   */
  public static String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      // These characters are part of the query syntax and must be escaped
      if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')'
          || c == ':' || c == '^' || c == '[' || c == ']' || c == '\"'
          || c == '{' || c == '}' || c == '~' || c == '*' || c == '?'
          || c == '|' || c == '&' || c == '/') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }

}
