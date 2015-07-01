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
 * An FST {@link Outputs} implementation, holding two other outputs.
 *
 * @lucene.experimental
 */

public class PairOutputs<A,B> extends Outputs<PairOutputs.Pair<A,B>> {

  private final Pair<A,B> NO_OUTPUT;
  private final Outputs<A> outputs1;
  private final Outputs<B> outputs2;

  /** Holds a single pair of two outputs. */
  public static class Pair<A,B> {
    public final A output1;
    public final B output2;

    // use newPair
    private Pair(A output1, B output2) {
      this.output1 = output1;
      this.output2 = output2;
    }

    @Override @SuppressWarnings("rawtypes")
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      } else if (other instanceof Pair) {
        Pair pair = (Pair) other;
        return output1.equals(pair.output1) && output2.equals(pair.output2);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return output1.hashCode() + output2.hashCode();
    }

    @Override
    public String toString() {
      return "Pair(" + output1 + "," + output2 + ")";
    }
  };

  public PairOutputs(Outputs<A> outputs1, Outputs<B> outputs2) {
    this.outputs1 = outputs1;
    this.outputs2 = outputs2;
    NO_OUTPUT = new Pair<>(outputs1.getNoOutput(), outputs2.getNoOutput());
  }

  /** Create a new Pair */
  public Pair<A,B> newPair(A a, B b) {
    if (a.equals(outputs1.getNoOutput())) {
      a = outputs1.getNoOutput();
    }
    if (b.equals(outputs2.getNoOutput())) {
      b = outputs2.getNoOutput();
    }

    if (a == outputs1.getNoOutput() && b == outputs2.getNoOutput()) {
      return NO_OUTPUT;
    } else {
      final Pair<A,B> p = new Pair<>(a, b);
      assert valid(p);
      return p;
    }
  }

  // for assert
  private boolean valid(Pair<A,B> pair) {
    final boolean noOutput1 = pair.output1.equals(outputs1.getNoOutput());
    final boolean noOutput2 = pair.output2.equals(outputs2.getNoOutput());

    if (noOutput1 && pair.output1 != outputs1.getNoOutput()) {
      return false;
    }

    if (noOutput2 && pair.output2 != outputs2.getNoOutput()) {
      return false;
    }

    if (noOutput1 && noOutput2) {
      if (pair != NO_OUTPUT) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }
  
  @Override
  public Pair<A,B> common(Pair<A,B> pair1, Pair<A,B> pair2) {
    assert valid(pair1);
    assert valid(pair2);
    return newPair(outputs1.common(pair1.output1, pair2.output1),
                   outputs2.common(pair1.output2, pair2.output2));
  }

  @Override
  public Pair<A,B> subtract(Pair<A,B> output, Pair<A,B> inc) {
    assert valid(output);
    assert valid(inc);
    return newPair(outputs1.subtract(output.output1, inc.output1),
                    outputs2.subtract(output.output2, inc.output2));
  }

  @Override
  public Pair<A,B> add(Pair<A,B> prefix, Pair<A,B> output) {
    assert valid(prefix);
    assert valid(output);
    return newPair(outputs1.add(prefix.output1, output.output1),
                    outputs2.add(prefix.output2, output.output2));
  }

  @Override
  public void write(Pair<A,B> output, DataOutput writer) throws IOException {
    assert valid(output);
    outputs1.write(output.output1, writer);
    outputs2.write(output.output2, writer);
  }

  @Override
  public Pair<A,B> read(DataInput in) throws IOException {
    A output1 = outputs1.read(in);
    B output2 = outputs2.read(in);
    return newPair(output1, output2);
  }
  
  @Override
  public void skipOutput(DataInput in) throws IOException {
    outputs1.skipOutput(in);
    outputs2.skipOutput(in);
  }

  @Override
  public Pair<A,B> getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(Pair<A,B> output) {
    assert valid(output);
    return "<pair:" + outputs1.outputToString(output.output1) + "," + outputs2.outputToString(output.output2) + ">";
  }

  @Override
  public String toString() {
    return "PairOutputs<" + outputs1 + "," + outputs2 + ">";
  }

  private static final long BASE_NUM_BYTES = RamUsageEstimator.shallowSizeOf(new Pair<Object,Object>(null, null));

  @Override
  public long ramBytesUsed(Pair<A,B> output) {
    long ramBytesUsed = BASE_NUM_BYTES;
    if (output.output1 != null) {
      ramBytesUsed += outputs1.ramBytesUsed(output.output1);
    }
    if (output.output2 != null) {
      ramBytesUsed += outputs2.ramBytesUsed(output.output2);
    }
    return ramBytesUsed;
  }
}
