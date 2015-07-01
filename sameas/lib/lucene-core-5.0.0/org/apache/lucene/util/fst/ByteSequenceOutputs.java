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
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RamUsageEstimator;
import org.apache.lucene.util.StringHelper;

/**
 * An FST {@link Outputs} implementation where each output
 * is a sequence of bytes.
 *
 * @lucene.experimental
 */

public final class ByteSequenceOutputs extends Outputs<BytesRef> {

  private final static BytesRef NO_OUTPUT = new BytesRef();
  private final static ByteSequenceOutputs singleton = new ByteSequenceOutputs();

  private ByteSequenceOutputs() {
  }

  public static ByteSequenceOutputs getSingleton() {
    return singleton;
  }

  @Override
  public BytesRef common(BytesRef output1, BytesRef output2) {
    assert output1 != null;
    assert output2 != null;

    int pos1 = output1.offset;
    int pos2 = output2.offset;
    int stopAt1 = pos1 + Math.min(output1.length, output2.length);
    while(pos1 < stopAt1) {
      if (output1.bytes[pos1] != output2.bytes[pos2]) {
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
      return new BytesRef(output1.bytes, output1.offset, pos1-output1.offset);
    }
  }

  @Override
  public BytesRef subtract(BytesRef output, BytesRef inc) {
    assert output != null;
    assert inc != null;
    if (inc == NO_OUTPUT) {
      // no prefix removed
      return output;
    } else {
      assert StringHelper.startsWith(output, inc);
      if (inc.length == output.length) {
        // entire output removed
        return NO_OUTPUT;
      } else {
        assert inc.length < output.length: "inc.length=" + inc.length + " vs output.length=" + output.length;
        assert inc.length > 0;
        return new BytesRef(output.bytes, output.offset + inc.length, output.length-inc.length);
      }
    }
  }

  @Override
  public BytesRef add(BytesRef prefix, BytesRef output) {
    assert prefix != null;
    assert output != null;
    if (prefix == NO_OUTPUT) {
      return output;
    } else if (output == NO_OUTPUT) {
      return prefix;
    } else {
      assert prefix.length > 0;
      assert output.length > 0;
      BytesRef result = new BytesRef(prefix.length + output.length);
      System.arraycopy(prefix.bytes, prefix.offset, result.bytes, 0, prefix.length);
      System.arraycopy(output.bytes, output.offset, result.bytes, prefix.length, output.length);
      result.length = prefix.length + output.length;
      return result;
    }
  }

  @Override
  public void write(BytesRef prefix, DataOutput out) throws IOException {
    assert prefix != null;
    out.writeVInt(prefix.length);
    out.writeBytes(prefix.bytes, prefix.offset, prefix.length);
  }

  @Override
  public BytesRef read(DataInput in) throws IOException {
    final int len = in.readVInt();
    if (len == 0) {
      return NO_OUTPUT;
    } else {
      final BytesRef output = new BytesRef(len);
      in.readBytes(output.bytes, 0, len);
      output.length = len;
      return output;
    }
  }

  @Override
  public void skipOutput(DataInput in) throws IOException {
    final int len = in.readVInt();
    if (len != 0) {
      in.skipBytes(len);
    }
  }

  @Override
  public BytesRef getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(BytesRef output) {
    return output.toString();
  }

  private static final long BASE_NUM_BYTES = RamUsageEstimator.shallowSizeOf(NO_OUTPUT);

  @Override
  public long ramBytesUsed(BytesRef output) {
    return BASE_NUM_BYTES + RamUsageEstimator.sizeOf(output.bytes);
  }

  @Override
  public String toString() {
    return "ByteSequenceOutputs";
  }
}
