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

package org.apache.lucene.analysis.charfilter;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.util.ArrayUtil;

import java.io.Reader;
import java.util.Arrays;

/**
 * Base utility class for implementing a {@link CharFilter}.
 * You subclass this, and then record mappings by calling
 * {@link #addOffCorrectMap}, and then invoke the correct
 * method to correct an offset.
 */
public abstract class BaseCharFilter extends CharFilter {

  private int offsets[];
  private int diffs[];
  private int size = 0;
  
  public BaseCharFilter(Reader in) {
    super(in);
  }

  /** Retrieve the corrected offset. */
  @Override
  protected int correct(int currentOff) {
    if (offsets == null || currentOff < offsets[0]) {
      return currentOff;
    }
    
    int hi = size - 1;
    if(currentOff >= offsets[hi])
      return currentOff + diffs[hi];

    int lo = 0;
    int mid = -1;
    
    while (hi >= lo) {
      mid = (lo + hi) >>> 1;
      if (currentOff < offsets[mid])
        hi = mid - 1;
      else if (currentOff > offsets[mid])
        lo = mid + 1;
      else
        return currentOff + diffs[mid];
    }

    if (currentOff < offsets[mid])
      return mid == 0 ? currentOff : currentOff + diffs[mid-1];
    else
      return currentOff + diffs[mid];
  }
  
  protected int getLastCumulativeDiff() {
    return offsets == null ?
      0 : diffs[size-1];
  }

  /**
   * <p>
   *   Adds an offset correction mapping at the given output stream offset.
   * </p>
   * <p>
   *   Assumption: the offset given with each successive call to this method
   *   will not be smaller than the offset given at the previous invocation.
   * </p>
   *
   * @param off The output stream offset at which to apply the correction
   * @param cumulativeDiff The input offset is given by adding this
   *                       to the output offset
   */
  protected void addOffCorrectMap(int off, int cumulativeDiff) {
    if (offsets == null) {
      offsets = new int[64];
      diffs = new int[64];
    } else if (size == offsets.length) {
      offsets = ArrayUtil.grow(offsets);
      diffs = ArrayUtil.grow(diffs);
    }
    
    assert (size == 0 || off >= offsets[size - 1])
        : "Offset #" + size + "(" + off + ") is less than the last recorded offset "
          + offsets[size - 1] + "\n" + Arrays.toString(offsets) + "\n" + Arrays.toString(diffs);
    
    if (size == 0 || off != offsets[size - 1]) {
      offsets[size] = off;
      diffs[size++] = cumulativeDiff;
    } else { // Overwrite the diff at the last recorded offset
      diffs[size - 1] = cumulativeDiff;
    }
  }
}
