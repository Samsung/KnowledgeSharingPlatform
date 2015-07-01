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

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;

/**
 * {@link MutableValue} implementation of type {@link String}.
 * When mutating instances of this object, the caller is responsible for ensuring 
 * that any instance where <code>exists</code> is set to <code>false</code> must also 
 * have a <code>value</code> with a length set to 0.
 */
public class MutableValueStr extends MutableValue {
  public BytesRefBuilder value = new BytesRefBuilder();

  @Override
  public Object toObject() {
    assert exists || 0 == value.length();
    return exists ? value.get().utf8ToString() : null;
  }

  @Override
  public void copy(MutableValue source) {
    MutableValueStr s = (MutableValueStr) source;
    exists = s.exists;
    value.copyBytes(s.value);
  }

  @Override
  public MutableValue duplicate() {
    MutableValueStr v = new MutableValueStr();
    v.value.copyBytes(value);
    v.exists = this.exists;
    return v;
  }

  @Override
  public boolean equalsSameType(Object other) {
    assert exists || 0 == value.length();
    MutableValueStr b = (MutableValueStr)other;
    return value.get().equals(b.value.get()) && exists == b.exists;
  }

  @Override
  public int compareSameType(Object other) {
    assert exists || 0 == value.length();
    MutableValueStr b = (MutableValueStr)other;
    int c = value.get().compareTo(b.value.get());
    if (c != 0) return c;
    if (exists == b.exists) return 0;
    return exists ? 1 : -1;
  }


  @Override
  public int hashCode() {
    assert exists || 0 == value.length();
    return value.get().hashCode();
  }
}