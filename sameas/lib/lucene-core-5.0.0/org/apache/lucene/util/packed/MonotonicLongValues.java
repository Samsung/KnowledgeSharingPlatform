package org.apache.lucene.util.packed;

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

import java.util.Arrays;

import static org.apache.lucene.util.packed.MonotonicBlockPackedReader.expected;

import org.apache.lucene.util.RamUsageEstimator;
import org.apache.lucene.util.packed.DeltaPackedLongValues.Builder;
import org.apache.lucene.util.packed.PackedInts.Reader;

class MonotonicLongValues extends DeltaPackedLongValues {

  private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(MonotonicLongValues.class);

  final float[] averages;

  MonotonicLongValues(int pageShift, int pageMask, Reader[] values, long[] mins, float[] averages, long size, long ramBytesUsed) {
    super(pageShift, pageMask, values, mins, size, ramBytesUsed);
    assert values.length == averages.length;
    this.averages = averages;
  }

  @Override
  long get(int block, int element) {
    return expected(mins[block], averages[block], element) + values[block].get(element);
  }

  @Override
  int decodeBlock(int block, long[] dest) {
    final int count = super.decodeBlock(block, dest);
    final float average = averages[block];
    for (int i = 0; i < count; ++i) {
      dest[i] += expected(0, average, i);
    }
    return count;
  }

  static class Builder extends DeltaPackedLongValues.Builder {

    private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(Builder.class);

    float[] averages;

    Builder(int pageSize, float acceptableOverheadRatio) {
      super(pageSize, acceptableOverheadRatio);
      averages = new float[values.length];
      ramBytesUsed += RamUsageEstimator.sizeOf(averages);
    }

    @Override
    long baseRamBytesUsed() {
      return BASE_RAM_BYTES_USED;
    }

    @Override
    public MonotonicLongValues build() {
      finish();
      pending = null;
      final PackedInts.Reader[] values = Arrays.copyOf(this.values, valuesOff);
      final long[] mins = Arrays.copyOf(this.mins, valuesOff);
      final float[] averages = Arrays.copyOf(this.averages, valuesOff);
      final long ramBytesUsed = MonotonicLongValues.BASE_RAM_BYTES_USED
          + RamUsageEstimator.sizeOf(values) + RamUsageEstimator.sizeOf(mins)
          + RamUsageEstimator.sizeOf(averages);
      return new MonotonicLongValues(pageShift, pageMask, values, mins, averages, size, ramBytesUsed);
    }

    @Override
    void pack(long[] values, int numValues, int block, float acceptableOverheadRatio) {
      final float average = numValues == 1 ? 0 : (float) (values[numValues - 1] - values[0]) / (numValues - 1);
      for (int i = 0; i < numValues; ++i) {
        values[i] -= expected(0, average, i);
      }
      super.pack(values, numValues, block, acceptableOverheadRatio);
      averages[block] = average;
    }

    @Override
    void grow(int newBlockCount) {
      super.grow(newBlockCount);
      ramBytesUsed -= RamUsageEstimator.sizeOf(averages);
      averages = Arrays.copyOf(averages, newBlockCount);
      ramBytesUsed += RamUsageEstimator.sizeOf(averages);
    }

  }

}
