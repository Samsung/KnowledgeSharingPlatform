package org.apache.lucene.queryparser.flexible.core.messages;

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

import org.apache.lucene.queryparser.flexible.messages.NLS;

/**
 * Flexible Query Parser message bundle class
 */
public class QueryParserMessages extends NLS {

  private static final String BUNDLE_NAME = QueryParserMessages.class.getName();

  private QueryParserMessages() {
    // Do not instantiate
  }

  static {
    // register all string ids with NLS class and initialize static string
    // values
    NLS.initializeMessages(BUNDLE_NAME, QueryParserMessages.class);
  }

  // static string must match the strings in the property files.
  public static String INVALID_SYNTAX;
  public static String INVALID_SYNTAX_CANNOT_PARSE;
  public static String INVALID_SYNTAX_FUZZY_LIMITS;
  public static String INVALID_SYNTAX_FUZZY_EDITS;
  public static String INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION;
  public static String INVALID_SYNTAX_ESCAPE_CHARACTER;
  public static String INVALID_SYNTAX_ESCAPE_NONE_HEX_UNICODE;
  public static String NODE_ACTION_NOT_SUPPORTED;
  public static String PARAMETER_VALUE_NOT_SUPPORTED;
  public static String LUCENE_QUERY_CONVERSION_ERROR;
  public static String EMPTY_MESSAGE;
  public static String WILDCARD_NOT_SUPPORTED;
  public static String TOO_MANY_BOOLEAN_CLAUSES;
  public static String LEADING_WILDCARD_NOT_ALLOWED;
  public static String COULD_NOT_PARSE_NUMBER;
  public static String NUMBER_CLASS_NOT_SUPPORTED_BY_NUMERIC_RANGE_QUERY;
  public static String UNSUPPORTED_NUMERIC_DATA_TYPE;
  public static String NUMERIC_CANNOT_BE_EMPTY;

}
