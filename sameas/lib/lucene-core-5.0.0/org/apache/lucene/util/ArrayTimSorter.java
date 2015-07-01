package org.apache.lucene.util;

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

import java.util.Comparator;

/**
 * A {@link TimSorter} for object arrays.
 * @lucene.internal
 */
final class ArrayTimSorter<T> extends TimSorter {

  private final Comparator<? super T> comparator;
  private final T[] arr;
  private final T[] tmp;

  /** Create a new {@link ArrayTimSorter}. */
  public ArrayTimSorter(T[] arr, Comparator<? super T> comparator, int maxTempSlots) {
    super(maxTempSlots);
    this.arr = arr;
    this.comparator = comparator;
    if (maxTempSlots > 0) {
      @SuppressWarnings("unchecked")
      final T[] tmp = (T[]) new Object[maxTempSlots];
      this.tmp = tmp;
    } else {
      this.tmp = null;
    }
  }

  @Override
  protected int compare(int i, int j) {
    return comparator.compare(arr[i], arr[j]);
  }

  @Override
  protected void swap(int i, int j) {
    ArrayUtil.swap(arr, i, j);
  }

  @Override
  protected void copy(int src, int dest) {
    arr[dest] = arr[src];
  }

  @Override
  protected void save(int start, int len) {
    System.arraycopy(arr, start, tmp, 0, len);
  }

  @Override
  protected void restore(int src, int dest) {
    arr[dest] = tmp[src];
  }

  @Override
  protected int compareSaved(int i, int j) {
    return comparator.compare(tmp[i], arr[j]);
  }

}
