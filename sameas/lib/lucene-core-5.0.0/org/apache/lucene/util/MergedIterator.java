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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.lucene.util.PriorityQueue;

/**
 * Provides a merged sorted view from several sorted iterators.
 * <p>
 * If built with <code>removeDuplicates</code> set to true and an element
 * appears in multiple iterators then it is deduplicated, that is this iterator
 * returns the sorted union of elements.
 * <p>
 * If built with <code>removeDuplicates</code> set to false then all elements
 * in all iterators are returned.
 * <p>
 * Caveats:
 * <ul>
 *   <li>The behavior is undefined if the iterators are not actually sorted.
 *   <li>Null elements are unsupported.
 *   <li>If removeDuplicates is set to true and if a single iterator contains
 *       duplicates then they will not be deduplicated.
 *   <li>When elements are deduplicated it is not defined which one is returned.
 *   <li>If removeDuplicates is set to false then the order in which duplicates
 *       are returned isn't defined.
 * </ul>
 * @lucene.internal
 */
public final class MergedIterator<T extends Comparable<T>> implements Iterator<T> {
  private T current;
  private final TermMergeQueue<T> queue; 
  private final SubIterator<T>[] top;
  private final boolean removeDuplicates;
  private int numTop;

  @SuppressWarnings({"unchecked","rawtypes"})
  public MergedIterator(Iterator<T>... iterators) {
    this(true, iterators);
  }

  @SuppressWarnings({"unchecked","rawtypes"})
  public MergedIterator(boolean removeDuplicates, Iterator<T>... iterators) {
    this.removeDuplicates = removeDuplicates;
    queue = new TermMergeQueue<>(iterators.length);
    top = new SubIterator[iterators.length];
    int index = 0;
    for (Iterator<T> iterator : iterators) {
      if (iterator.hasNext()) {
        SubIterator<T> sub = new SubIterator<>();
        sub.current = iterator.next();
        sub.iterator = iterator;
        sub.index = index++;
        queue.add(sub);
      }
    }
  }
  
  @Override
  public boolean hasNext() {
    if (queue.size() > 0) {
      return true;
    }
    
    for (int i = 0; i < numTop; i++) {
      if (top[i].iterator.hasNext()) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public T next() {
    // restore queue
    pushTop();
    
    // gather equal top elements
    if (queue.size() > 0) {
      pullTop();
    } else {
      current = null;
    }
    if (current == null) {
      throw new NoSuchElementException();
    }
    return current;
  }
  
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  private void pullTop() {
    assert numTop == 0;
    top[numTop++] = queue.pop();
    if (removeDuplicates) {
      // extract all subs from the queue that have the same top element
      while (queue.size() != 0
             && queue.top().current.equals(top[0].current)) {
        top[numTop++] = queue.pop();
      }
    }
    current = top[0].current;
  }
  
  private void pushTop() {
    // call next() on each top, and put back into queue
    for (int i = 0; i < numTop; i++) {
      if (top[i].iterator.hasNext()) {
        top[i].current = top[i].iterator.next();
        queue.add(top[i]);
      } else {
        // no more elements
        top[i].current = null;
      }
    }
    numTop = 0;
  }
  
  private static class SubIterator<I extends Comparable<I>> {
    Iterator<I> iterator;
    I current;
    int index;
  }
  
  private static class TermMergeQueue<C extends Comparable<C>> extends PriorityQueue<SubIterator<C>> {
    TermMergeQueue(int size) {
      super(size);
    }
    
    @Override
    protected boolean lessThan(SubIterator<C> a, SubIterator<C> b) {
      final int cmp = a.current.compareTo(b.current);
      if (cmp != 0) {
        return cmp < 0;
      } else {
        return a.index < b.index;
      }
    }
  }
}
