package org.apache.lucene.util;

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
 * Interface for Bitset-like structures.
 * @lucene.experimental
 */

public interface Bits {
  /** 
   * Returns the value of the bit with the specified <code>index</code>.
   * @param index index, should be non-negative and &lt; {@link #length()}.
   *        The result of passing negative or out of bounds values is undefined
   *        by this interface, <b>just don't do it!</b>
   * @return <code>true</code> if the bit is set, <code>false</code> otherwise.
   */
  public boolean get(int index);
  
  /** Returns the number of bits in this set */
  public int length();

  public static final Bits[] EMPTY_ARRAY = new Bits[0];
  
  /**
   * Bits impl of the specified length with all bits set. 
   */
  public static class MatchAllBits implements Bits {
    final int len;
    
    public MatchAllBits( int len ) {
      this.len = len;
    }

    @Override
    public boolean get(int index) {
      return true;
    }

    @Override
    public int length() {
      return len;
    }
  }

  /**
   * Bits impl of the specified length with no bits set. 
   */
  public static class MatchNoBits implements Bits {
    final int len;
    
    public MatchNoBits( int len ) {
      this.len = len;
    }

    @Override
    public boolean get(int index) {
      return false;
    }

    @Override
    public int length() {
      return len;
    }
  }
}
