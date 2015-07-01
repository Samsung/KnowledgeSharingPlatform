package org.apache.lucene.analysis.tokenattributes;

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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;

/**
 * This attribute can be used to mark a token as a keyword. Keyword aware
 * {@link TokenStream}s can decide to modify a token based on the return value
 * of {@link #isKeyword()} if the token is modified. Stemming filters for
 * instance can use this attribute to conditionally skip a term if
 * {@link #isKeyword()} returns <code>true</code>.
 */
public interface KeywordAttribute extends Attribute {

  /**
   * Returns <code>true</code> if the current token is a keyword, otherwise
   * <code>false</code>
   * 
   * @return <code>true</code> if the current token is a keyword, otherwise
   *         <code>false</code>
   * @see #setKeyword(boolean)
   */
  public boolean isKeyword();

  /**
   * Marks the current token as keyword if set to <code>true</code>.
   * 
   * @param isKeyword
   *          <code>true</code> if the current token is a keyword, otherwise
   *          <code>false</code>.
   * @see #isKeyword()
   */
  public void setKeyword(boolean isKeyword);
}
