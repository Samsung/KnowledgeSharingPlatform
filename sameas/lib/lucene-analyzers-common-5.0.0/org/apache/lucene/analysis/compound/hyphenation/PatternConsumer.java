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

package org.apache.lucene.analysis.compound.hyphenation;

import java.util.ArrayList;

/**
 * This interface is used to connect the XML pattern file parser to the
 * hyphenation tree.
 * 
 * This class has been taken from the Apache FOP project (http://xmlgraphics.apache.org/fop/). They have been slightly modified.
 */
public interface PatternConsumer {

  /**
   * Add a character class. A character class defines characters that are
   * considered equivalent for the purpose of hyphenation (e.g. "aA"). It
   * usually means to ignore case.
   * 
   * @param chargroup character group
   */
  void addClass(String chargroup);

  /**
   * Add a hyphenation exception. An exception replaces the result obtained by
   * the algorithm for cases for which this fails or the user wants to provide
   * his own hyphenation. A hyphenatedword is a vector of alternating String's
   * and {@link Hyphen Hyphen} instances
   */
  void addException(String word, ArrayList<Object> hyphenatedword);

  /**
   * Add hyphenation patterns.
   * 
   * @param pattern the pattern
   * @param values interletter values expressed as a string of digit characters.
   */
  void addPattern(String pattern, String values);

}
