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
package org.apache.lucene.util.mutable;

/**
 * Base class for all mutable values.
 *  
 * @lucene.internal 
 */
public abstract class MutableValue implements Comparable<MutableValue> {
  public boolean exists = true;

  public abstract void copy(MutableValue source);
  public abstract MutableValue duplicate();
  public abstract boolean equalsSameType(Object other);
  public abstract int compareSameType(Object other);
  public abstract Object toObject();

  public boolean exists() {
    return exists;
  }

  @Override
  public int compareTo(MutableValue other) {
    Class<? extends MutableValue> c1 = this.getClass();
    Class<? extends MutableValue> c2 = other.getClass();
    if (c1 != c2) {
      int c = c1.hashCode() - c2.hashCode();
      if (c == 0) {
        c = c1.getCanonicalName().compareTo(c2.getCanonicalName());
      }
      return c;
    }
    return compareSameType(other);
  }

  @Override
  public boolean equals(Object other) {
    return (getClass() == other.getClass()) && this.equalsSameType(other);
  }

  @Override
  public abstract int hashCode();

  @Override
  public String toString() {
    return exists() ? toObject().toString() : "(null)";
  }
}


