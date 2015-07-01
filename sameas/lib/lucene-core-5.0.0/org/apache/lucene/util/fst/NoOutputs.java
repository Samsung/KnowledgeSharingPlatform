package org.apache.lucene.util.fst;

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

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;

/**
 * A null FST {@link Outputs} implementation; use this if
 * you just want to build an FSA.
 *
 * @lucene.experimental
 */

public final class NoOutputs extends Outputs<Object> {

  static final Object NO_OUTPUT = new Object() {
    // NodeHash calls hashCode for this output; we fix this
    // so we get deterministic hashing.
    @Override
    public int hashCode() {
      return 42;
    }

    @Override
    public boolean equals(Object other) {
      return other == this;
    }
  };

  private static final NoOutputs singleton = new NoOutputs();

  private NoOutputs() {
  }

  public static NoOutputs getSingleton() {
    return singleton;
  }

  @Override
  public Object common(Object output1, Object output2) {
    assert output1 == NO_OUTPUT;
    assert output2 == NO_OUTPUT;
    return NO_OUTPUT;
  }

  @Override
  public Object subtract(Object output, Object inc) {
    assert output == NO_OUTPUT;
    assert inc == NO_OUTPUT;
    return NO_OUTPUT;
  }

  @Override
  public Object add(Object prefix, Object output) {
    assert prefix == NO_OUTPUT: "got " + prefix;
    assert output == NO_OUTPUT;
    return NO_OUTPUT;
  }

  @Override
  public Object merge(Object first, Object second) {
    assert first == NO_OUTPUT;
    assert second == NO_OUTPUT;
    return NO_OUTPUT;
  }

  @Override
  public void write(Object prefix, DataOutput out) {
    //assert false;
  }

  @Override
  public Object read(DataInput in) {
    //assert false;
    //return null;
    return NO_OUTPUT;
  }

  @Override
  public Object getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(Object output) {
    return "";
  }

  @Override
  public long ramBytesUsed(Object output) {
    return 0;
  }

  @Override
  public String toString() {
    return "NoOutputs";
  }
}
