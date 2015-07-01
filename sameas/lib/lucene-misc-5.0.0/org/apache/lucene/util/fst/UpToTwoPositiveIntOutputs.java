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
 * is one or two non-negative long values.  If it's a
 * single output, Long is returned; else, TwoLongs.  Order
 * is preserved in the TwoLongs case, ie .first is the first
 * input/output added to Builder, and .second is the
 * second.  You cannot store 0 output with this (that's
 * reserved to mean "no output")!
 *
 * <p>NOTE: the only way to create a TwoLongs output is to
 * add the same input to the FST twice in a row.  This is
 * how the FST maps a single input to two outputs (e.g. you
 * cannot pass a TwoLongs to {@link Builder#add}.  If you
 * need more than two then use {@link ListOfOutputs}, but if
 * you only have at most 2 then this implementation will
 * require fewer bytes as it steals one bit from each long
 * value.
 *
 * <p>NOTE: the resulting FST is not guaranteed to be minimal!
 * See {@link Builder}.
 *
 * @lucene.experimental
 */

public final class UpToTwoPositiveIntOutputs extends Outputs<Object> {

  /** Holds two long outputs. */
  public final static class TwoLongs {
    public final long first;
    public final long second;

    public TwoLongs(long first, long second) {
      this.first = first;
      this.second = second;
      assert first >= 0;
      assert second >= 0;
    }

    @Override
    public String toString() {
      return "TwoLongs:" + first + "," + second;
    }

    @Override
    public boolean equals(Object _other) {
      if (_other instanceof TwoLongs) {
        final TwoLongs other = (TwoLongs) _other;
        return first == other.first && second == other.second;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return (int) ((first^(first>>>32)) ^ (second^(second>>32)));
    }
  }
  
  private final static Long NO_OUTPUT = new Long(0);

  private final boolean doShare;

  private final static UpToTwoPositiveIntOutputs singletonShare = new UpToTwoPositiveIntOutputs(true);
  private final static UpToTwoPositiveIntOutputs singletonNoShare = new UpToTwoPositiveIntOutputs(false);

  private UpToTwoPositiveIntOutputs(boolean doShare) {
    this.doShare = doShare;
  }

  public static UpToTwoPositiveIntOutputs getSingleton(boolean doShare) {
    return doShare ? singletonShare : singletonNoShare;
  }

  public Long get(long v) {
    if (v == 0) {
      return NO_OUTPUT;
    } else {
      return Long.valueOf(v);
    }
  }

  public TwoLongs get(long first, long second) {
    return new TwoLongs(first, second);
  }

  @Override
  public Long common(Object _output1, Object _output2) {
    assert valid(_output1, false);
    assert valid(_output2, false);
    final Long output1 = (Long) _output1;
    final Long output2 = (Long) _output2;
    if (output1 == NO_OUTPUT || output2 == NO_OUTPUT) {
      return NO_OUTPUT;
    } else if (doShare) {
      assert output1 > 0;
      assert output2 > 0;
      return Math.min(output1, output2);
    } else if (output1.equals(output2)) {
      return output1;
    } else {
      return NO_OUTPUT;
    }
  }

  @Override
  public Long subtract(Object _output, Object _inc) {
    assert valid(_output, false);
    assert valid(_inc, false);
    final Long output = (Long) _output;
    final Long inc = (Long) _inc;
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
  public Object add(Object _prefix, Object _output) {
    assert valid(_prefix, false);
    assert valid(_output, true);
    final Long prefix = (Long) _prefix;
    if (_output instanceof Long) {
      final Long output = (Long) _output;
      if (prefix == NO_OUTPUT) {
        return output;
      } else if (output == NO_OUTPUT) {
        return prefix;
      } else {
        return prefix + output;
      }
    } else {
      final TwoLongs output = (TwoLongs) _output;
      final long v = prefix;
      return new TwoLongs(output.first + v, output.second + v);
    }
  }

  @Override
  public void write(Object _output, DataOutput out) throws IOException {
    assert valid(_output, true);
    if (_output instanceof Long) {
      final Long output = (Long) _output;
      out.writeVLong(output<<1);
    } else {
      final TwoLongs output = (TwoLongs) _output;
      out.writeVLong((output.first<<1) | 1);
      out.writeVLong(output.second);
    }
  }

  @Override
  public Object read(DataInput in) throws IOException {
    final long code = in.readVLong();
    if ((code & 1) == 0) {
      // single long
      final long v = code >>> 1;
      if (v == 0) {
        return NO_OUTPUT;
      } else {
        return Long.valueOf(v);
      }
    } else {
      // two longs
      final long first = code >>> 1;
      final long second = in.readVLong();
      return new TwoLongs(first, second);
    }
  }

  private boolean valid(Long o) {
    assert o != null;
    assert o instanceof Long;
    assert o == NO_OUTPUT || o > 0;
    return true;
  }

  // Used only by assert
  private boolean valid(Object _o, boolean allowDouble) {
    if (!allowDouble) {
      assert _o instanceof Long;
      return valid((Long) _o);
    } else if (_o instanceof TwoLongs) {
      return true;
    } else {
      return valid((Long) _o);
    }
  }

  @Override
  public Object getNoOutput() {
    return NO_OUTPUT;
  }

  @Override
  public String outputToString(Object output) {
    return output.toString();
  }

  @Override
  public Object merge(Object first, Object second) {
    assert valid(first, false);
    assert valid(second, false);
    return new TwoLongs((Long) first, (Long) second);
  }

  private static final long TWO_LONGS_NUM_BYTES = RamUsageEstimator.shallowSizeOf(new TwoLongs(0, 0));

  @Override
  public long ramBytesUsed(Object o) {
    if (o instanceof Long) {
      return RamUsageEstimator.sizeOf((Long) o);
    } else {
      assert o instanceof TwoLongs;
      return TWO_LONGS_NUM_BYTES;
    }
  }
}
