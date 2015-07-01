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

import org.apache.lucene.util.Attribute;

/** Determines how many positions this
 *  token spans.  Very few analyzer components actually
 *  produce this attribute, and indexing ignores it, but
 *  it's useful to express the graph structure naturally
 *  produced by decompounding, word splitting/joining,
 *  synonym filtering, etc.
 *
 * <p>NOTE: this is optional, and most analyzers
 *  don't change the default value (1). */

public interface PositionLengthAttribute extends Attribute {
  /**
   * Set the position length of this Token.
   * <p>
   * The default value is one. 
   * @param positionLength how many positions this token
   *  spans. 
   * @throws IllegalArgumentException if <code>positionLength</code> 
   *         is zero or negative.
   * @see #getPositionLength()
   */
  public void setPositionLength(int positionLength);

  /** Returns the position length of this Token.
   * @see #setPositionLength
   */
  public int getPositionLength();
}

