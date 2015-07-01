package org.apache.lucene.analysis.util;

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
 * Some commonly-used stemming functions
 * 
 * @lucene.internal
 */
public class StemmerUtil {
  /** no instance */
  private StemmerUtil() {}

  /**
   * Returns true if the character array starts with the suffix.
   * 
   * @param s Input Buffer
   * @param len length of input buffer
   * @param prefix Prefix string to test
   * @return true if <code>s</code> starts with <code>prefix</code>
   */
  public static boolean startsWith(char s[], int len, String prefix) {
    final int prefixLen = prefix.length();
    if (prefixLen > len)
      return false;
    for (int i = 0; i < prefixLen; i++)
      if (s[i] != prefix.charAt(i)) 
        return false;
    return true;
  }
  
  /**
   * Returns true if the character array ends with the suffix.
   * 
   * @param s Input Buffer
   * @param len length of input buffer
   * @param suffix Suffix string to test
   * @return true if <code>s</code> ends with <code>suffix</code>
   */
  public static boolean endsWith(char s[], int len, String suffix) {
    final int suffixLen = suffix.length();
    if (suffixLen > len)
      return false;
    for (int i = suffixLen - 1; i >= 0; i--)
      if (s[len -(suffixLen - i)] != suffix.charAt(i))
        return false;
    
    return true;
  }
  
  /**
   * Returns true if the character array ends with the suffix.
   * 
   * @param s Input Buffer
   * @param len length of input buffer
   * @param suffix Suffix string to test
   * @return true if <code>s</code> ends with <code>suffix</code>
   */
  public static boolean endsWith(char s[], int len, char suffix[]) {
    final int suffixLen = suffix.length;
    if (suffixLen > len)
      return false;
    for (int i = suffixLen - 1; i >= 0; i--)
      if (s[len -(suffixLen - i)] != suffix[i])
        return false;
    
    return true;
  }
  
  /**
   * Delete a character in-place
   * 
   * @param s Input Buffer
   * @param pos Position of character to delete
   * @param len length of input buffer
   * @return length of input buffer after deletion
   */
  public static int delete(char s[], int pos, int len) {
    assert pos < len;
    if (pos < len - 1) { // don't arraycopy if asked to delete last character
      System.arraycopy(s, pos + 1, s, pos, len - pos - 1);
    }
    return len - 1;
  }
  
  /**
   * Delete n characters in-place
   * 
   * @param s Input Buffer
   * @param pos Position of character to delete
   * @param len Length of input buffer
   * @param nChars number of characters to delete
   * @return length of input buffer after deletion
   */
  public static int deleteN(char s[], int pos, int len, int nChars) {
    assert pos + nChars <= len;
    if (pos + nChars < len) { // don't arraycopy if asked to delete the last characters
      System.arraycopy(s, pos + nChars, s, pos, len - pos - nChars);
    }
    return len - nChars;
  }
}
