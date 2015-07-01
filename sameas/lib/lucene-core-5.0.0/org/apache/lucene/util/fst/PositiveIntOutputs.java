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

import java.io.IOException;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * An FST {@link Outputs} implementation where each output
 * is a non-negative long value.
 *
 * @lucene.experimental
 */

public final class PositiveIntOutputs extends Outputs<Long> {
  
  private final static Long NO_OUTPUT = new Long(0);

  private final static PositiveIntOutputs singleton = new PositiveIntOutputs();

  private PositiveIntOutputs() {
  }

  public static PositiveIntOutputs getSingleton() {
    return singleton;
  }

  @Override
  public Long common(Long output1, Long output2) {
    assert valid(output1);
    assert valid(output2);
    if (output1 == NO_OUTPUT || output2 == NO_OUTPUT) {
      return NO_OUTPUT;
    } else {
      assert output1 > 0;
      assert output2 > 0;
      return Math.min(output1, output2);
    }
  }

  @Override
  public Long subtract(Long output, Long inc) {
    assert valid(output);
    assert valid(inc);
    assert output >= inc;

    if (inc == NO_OUTPUT) {
      return output;
    } else if (output.equals(inc)) {
      return NO_OUTPUT;
    } else {
      return output - inc;
    }
  }

  @Override
  public Long add(Long prefix, Long output) {
    assert valid(prefix);
    assert valid(output);
    if (prefix == NO_OUTPUT) {
      return output;
    } else if (output == NO_OUTPUT) {
      return prefix;
    } else {
      return prefix + output;
    }
  }

  @Override
  public void write(Long output, DataOutput out) throws IOException {
    assert valid(output);
    out.writeVLong(output);
  }

  @Override
  public Long read(DataInput in) throws IOException {
    long v = in.readVLong();
    if (v == 0) {
      return NO_OUTPUT;
    } else {
      return v;
    }
  }

  private boolean valid(Long o) {
    assert o != null;
    assert o == NO_OUTPUT || o > 0: "o=" + o;
    return true;
  }

  @Override
  public Long getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(Long output) {
    return output.toString();
  }

  @Override
  public String toString() {
    return "PositiveIntOutputs";
  }

  @Override
  public long ramBytesUsed(Long output) {
    return RamUsageEstimator.sizeOf(output);
  }
}
