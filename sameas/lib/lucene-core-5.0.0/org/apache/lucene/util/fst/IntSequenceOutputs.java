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
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * An FST {@link Outputs} implementation where each output
 * is a sequence of ints.
 *
 * @lucene.experimental
 */

public final class IntSequenceOutputs extends Outputs<IntsRef> {

  private final static IntsRef NO_OUTPUT = new IntsRef();
  private final static IntSequenceOutputs singleton = new IntSequenceOutputs();

  private IntSequenceOutputs() {
  }

  public static IntSequenceOutputs getSingleton() {
    return singleton;
  }

  @Override
  public IntsRef common(IntsRef output1, IntsRef output2) {
    assert output1 != null;
    assert output2 != null;

    int pos1 = output1.offset;
    int pos2 = output2.offset;
    int stopAt1 = pos1 + Math.min(output1.length, output2.length);
    while(pos1 < stopAt1) {
      if (output1.ints[pos1] != output2.ints[pos2]) {
        break;
      }
      pos1++;
      pos2++;
    }

    if (pos1 == output1.offset) {
      // no common prefix
      return NO_OUTPUT;
    } else if (pos1 == output1.offset + output1.length) {
      // output1 is a prefix of output2
      return output1;
    } else if (pos2 == output2.offset + output2.length) {
      // output2 is a prefix of output1
      return output2;
    } else {
      return new IntsRef(output1.ints, output1.offset, pos1-output1.offset);
    }
  }

  @Override
  public IntsRef subtract(IntsRef output, IntsRef inc) {
    assert output != null;
    assert inc != null;
    if (inc == NO_OUTPUT) {
      // no prefix removed
      return output;
    } else if (inc.length == output.length) {
      // entire output removed
      return NO_OUTPUT;
    } else {
      assert inc.length < output.length: "inc.length=" + inc.length + " vs output.length=" + output.length;
      assert inc.length > 0;
      return new IntsRef(output.ints, output.offset + inc.length, output.length-inc.length);
    }
  }

  @Override
  public IntsRef add(IntsRef prefix, IntsRef output) {
    assert prefix != null;
    assert output != null;
    if (prefix == NO_OUTPUT) {
      return output;
    } else if (output == NO_OUTPUT) {
      return prefix;
    } else {
      assert prefix.length > 0;
      assert output.length > 0;
      IntsRef result = new IntsRef(prefix.length + output.length);
      System.arraycopy(prefix.ints, prefix.offset, result.ints, 0, prefix.length);
      System.arraycopy(output.ints, output.offset, result.ints, prefix.length, output.length);
      result.length = prefix.length + output.length;
      return result;
    }
  }

  @Override
  public void write(IntsRef prefix, DataOutput out) throws IOException {
    assert prefix != null;
    out.writeVInt(prefix.length);
    for(int idx=0;idx<prefix.length;idx++) {
      out.writeVInt(prefix.ints[prefix.offset+idx]);
    }
  }

  @Override
  public IntsRef read(DataInput in) throws IOException {
    final int len = in.readVInt();
    if (len == 0) {
      return NO_OUTPUT;
    } else {
      final IntsRef output = new IntsRef(len);
      for(int idx=0;idx<len;idx++) {
        output.ints[idx] = in.readVInt();
      }
      output.length = len;
      return output;
    }
  }
  
  @Override
  public void skipOutput(DataInput in) throws IOException {
    final int len = in.readVInt();
    if (len == 0) {
      return;
    }
    for(int idx=0;idx<len;idx++) {
      in.readVInt();
    }
  }

  @Override
  public IntsRef getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(IntsRef output) {
    return output.toString();
  }

  private static final long BASE_NUM_BYTES = RamUsageEstimator.shallowSizeOf(NO_OUTPUT);

  @Override
  public long ramBytesUsed(IntsRef output) {
    return BASE_NUM_BYTES + RamUsageEstimator.sizeOf(output.ints);
  }
  
  @Override
  public String toString() {
    return "IntSequenceOutputs";
  }
}
