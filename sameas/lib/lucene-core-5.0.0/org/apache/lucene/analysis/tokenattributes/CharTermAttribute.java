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
 * The term text of a Token.
 */
public interface CharTermAttribute extends Attribute, CharSequence, Appendable {
  
  /** Copies the contents of buffer, starting at offset for
   *  length characters, into the termBuffer array.
   *  @param buffer the buffer to copy
   *  @param offset the index in the buffer of the first character to copy
   *  @param length the number of characters to copy
   */
  public void copyBuffer(char[] buffer, int offset, int length);
  
  /** Returns the internal termBuffer character array which
   *  you can then directly alter.  If the array is too
   *  small for your token, use {@link
   *  #resizeBuffer(int)} to increase it.  After
   *  altering the buffer be sure to call {@link
   *  #setLength} to record the number of valid
   *  characters that were placed into the termBuffer. 
   *  <p>
   *  <b>NOTE</b>: The returned buffer may be larger than
   *  the valid {@link #length()}.
   */
  public char[] buffer();

  /** Grows the termBuffer to at least size newSize, preserving the
   *  existing content.
   *  @param newSize minimum size of the new termBuffer
   *  @return newly created termBuffer with {@code length >= newSize}
   */
  public char[] resizeBuffer(int newSize);

  /** Set number of valid characters (length of the term) in
   *  the termBuffer array. Use this to truncate the termBuffer
   *  or to synchronize with external manipulation of the termBuffer.
   *  Note: to grow the size of the array,
   *  use {@link #resizeBuffer(int)} first.
   *  @param length the truncated length
   */
  public CharTermAttribute setLength(int length);
  
  /** Sets the length of the termBuffer to zero.
   * Use this method before appending contents
   * using the {@link Appendable} interface.
   */
  public CharTermAttribute setEmpty();
  
  // the following methods are redefined to get rid of IOException declaration:
  @Override
  public CharTermAttribute append(CharSequence csq);
  @Override
  public CharTermAttribute append(CharSequence csq, int start, int end);
  @Override
  public CharTermAttribute append(char c);

  /** Appends the specified {@code String} to this character sequence. 
   * <p>The characters of the {@code String} argument are appended, in order, increasing the length of
   * this sequence by the length of the argument. If argument is {@code null}, then the four
   * characters {@code "null"} are appended. 
   */
  public CharTermAttribute append(String s);

  /** Appends the specified {@code StringBuilder} to this character sequence. 
   * <p>The characters of the {@code StringBuilder} argument are appended, in order, increasing the length of
   * this sequence by the length of the argument. If argument is {@code null}, then the four
   * characters {@code "null"} are appended. 
   */
  public CharTermAttribute append(StringBuilder sb);

  /** Appends the contents of the other {@code CharTermAttribute} to this character sequence. 
   * <p>The characters of the {@code CharTermAttribute} argument are appended, in order, increasing the length of
   * this sequence by the length of the argument. If argument is {@code null}, then the four
   * characters {@code "null"} are appended. 
   */
  public CharTermAttribute append(CharTermAttribute termAtt);
}
