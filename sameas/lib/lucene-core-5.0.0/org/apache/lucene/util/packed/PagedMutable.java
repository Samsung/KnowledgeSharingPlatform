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
 * A {@link PagedMutable}. This class slices data into fixed-size blocks
 * which have the same number of bits per value. It can be a useful replacement
 * for {@link PackedInts.Mutable} to store more than 2B values.
 * @lucene.internal
 */
public final class PagedMutable extends AbstractPagedMutable<PagedMutable> {

  final PackedInts.Format format;

  /**
   * Create a new {@link PagedMutable} instance.
   *
   * @param size the number of values to store.
   * @param pageSize the number of values per page
   * @param bitsPerValue the number of bits per value
   * @param acceptableOverheadRatio an acceptable overhead ratio
   */
  public PagedMutable(long size, int pageSize, int bitsPerValue, float acceptableOverheadRatio) {
    this(size, pageSize, PackedInts.fastestFormatAndBits(pageSize, bitsPerValue, acceptableOverheadRatio));
    fillPages();
  }

  PagedMutable(long size, int pageSize, PackedInts.FormatAndBits formatAndBits) {
    this(size, pageSize, formatAndBits.bitsPerValue, formatAndBits.format);
  }

  PagedMutable(long size, int pageSize, int bitsPerValue, PackedInts.Format format) {
    super(bitsPerValue, size, pageSize);
    this.format = format;
  }

  @Override
  protected Mutable newMutable(int valueCount, int bitsPerValue) {
    assert this.bitsPerValue >= bitsPerValue;
    return PackedInts.getMutable(valueCount, this.bitsPerValue, format);
  }

  @Override
  protected PagedMutable newUnfilledCopy(long newSize) {
    return new PagedMutable(newSize, pageSize(), bitsPerValue, format);
  }

  @Override
  protected long baseRamBytesUsed() {
    return super.baseRamBytesUsed() + RamUsageEstimator.NUM_BYTES_OBJECT_REF;
  }

}
