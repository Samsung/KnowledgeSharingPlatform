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

import org.apache.lucene.util.RamUsageEstimator;
import org.apache.lucene.util.packed.PackedInts.Mutable;

/**
 * A {@link PagedGrowableWriter}. This class slices data into fixed-size blocks
 * which have independent numbers of bits per value and grow on-demand.
 * <p>You should use this class instead of the {@link PackedLongValues} related ones only when
 * you need random write-access. Otherwise this class will likely be slower and
 * less memory-efficient.
 * @lucene.internal
 */
public final class PagedGrowableWriter extends AbstractPagedMutable<PagedGrowableWriter> {

  final float acceptableOverheadRatio;

  /**
   * Create a new {@link PagedGrowableWriter} instance.
   *
   * @param size the number of values to store.
   * @param pageSize the number of values per page
   * @param startBitsPerValue the initial number of bits per value
   * @param acceptableOverheadRatio an acceptable overhead ratio
   */
  public PagedGrowableWriter(long size, int pageSize,
      int startBitsPerValue, float acceptableOverheadRatio) {
    this(size, pageSize, startBitsPerValue, acceptableOverheadRatio, true);
  }

  PagedGrowableWriter(long size, int pageSize,int startBitsPerValue, float acceptableOverheadRatio, boolean fillPages) {
    super(startBitsPerValue, size, pageSize);
    this.acceptableOverheadRatio = acceptableOverheadRatio;
    if (fillPages) {
      fillPages();
    }
  }

  @Override
  protected Mutable newMutable(int valueCount, int bitsPerValue) {
    return new GrowableWriter(bitsPerValue, valueCount, acceptableOverheadRatio);
  }

  @Override
  protected PagedGrowableWriter newUnfilledCopy(long newSize) {
    return new PagedGrowableWriter(newSize, pageSize(), bitsPerValue, acceptableOverheadRatio, false);
  }

  @Override
  protected long baseRamBytesUsed() {
    return super.baseRamBytesUsed() + RamUsageEstimator.NUM_BYTES_FLOAT;
  }

}
