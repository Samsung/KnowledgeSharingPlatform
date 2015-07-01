package org.apache.lucene.analysis.core;

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

import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

/**
 * Removes tokens whose types appear in a set of blocked types from a token stream.
 */
public final class TypeTokenFilter extends FilteringTokenFilter {

  private final Set<String> stopTypes;
  private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
  private final boolean useWhiteList;

  /**
   * Create a new {@link TypeTokenFilter}.
   * @param input        the {@link TokenStream} to consume
   * @param stopTypes    the types to filter
   * @param useWhiteList if true, then tokens whose type is in stopTypes will
   *                     be kept, otherwise they will be filtered out
   */
  public TypeTokenFilter(TokenStream input, Set<String> stopTypes, boolean useWhiteList) {
    super(input);
    this.stopTypes = stopTypes;
    this.useWhiteList = useWhiteList;
  }

  /**
   * Create a new {@link TypeTokenFilter} that filters tokens out
   * (useWhiteList=false).
   */
  public TypeTokenFilter(TokenStream input, Set<String> stopTypes) {
    this(input, stopTypes, false);
  }

  /**
   * By default accept the token if its type is not a stop type.
   * When the useWhiteList parameter is set to true then accept the token if its type is contained in the stopTypes
   */
  @Override
  protected boolean accept() {
    return useWhiteList == stopTypes.contains(typeAttribute.type());
  }
}
