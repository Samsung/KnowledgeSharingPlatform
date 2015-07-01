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

/**
 * The start and end character offset of a Token. 
 */
public interface OffsetAttribute extends Attribute {
  /** 
   * Returns this Token's starting offset, the position of the first character
   * corresponding to this token in the source text.
   * <p>
   * Note that the difference between {@link #endOffset()} and <code>startOffset()</code> 
   * may not be equal to termText.length(), as the term text may have been altered by a
   * stemmer or some other filter.
   * @see #setOffset(int, int) 
   */
  public int startOffset();

  
  /** 
   * Set the starting and ending offset.
   * @throws IllegalArgumentException If <code>startOffset</code> or <code>endOffset</code>
   *         are negative, or if <code>startOffset</code> is greater than 
   *         <code>endOffset</code>
   * @see #startOffset()
   * @see #endOffset()
   */
  public void setOffset(int startOffset, int endOffset);
  

  /** 
   * Returns this Token's ending offset, one greater than the position of the
   * last character corresponding to this token in the source text. The length
   * of the token in the source text is (<code>endOffset()</code> - {@link #startOffset()}). 
   * @see #setOffset(int, int)
   */
  public int endOffset();
}
