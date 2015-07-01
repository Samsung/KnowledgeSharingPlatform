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

/**
 * An {@link Iterator} implementation that filters elements with a boolean predicate.
 *
 * @param <T> generic parameter for this iterator instance: this iterator implements {@link Iterator Iterator&lt;T&gt;}
 * @param <InnerT> generic parameter of the wrapped iterator, must be <tt>T</tt> or extend <tt>T</tt>
 * @see #predicateFunction
 * @lucene.internal
 */
public abstract class FilterIterator<T, InnerT extends T> implements Iterator<T> {
  
  private final Iterator<InnerT> iterator;
  private T next = null;
  private boolean nextIsSet = false;
  
  /** returns true, if this element should be returned by {@link #next()}. */
  protected abstract boolean predicateFunction(InnerT object);
  
  public FilterIterator(Iterator<InnerT> baseIterator) {
    this.iterator = baseIterator;
  }
  
  @Override
  public final boolean hasNext() {
    return nextIsSet || setNext();
  }
  
  @Override
  public final T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    assert nextIsSet;
    try {
      return next;
    } finally {
      nextIsSet = false;
      next = null;
    }
  }
  
  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }
  
  private boolean setNext() {
    while (iterator.hasNext()) {
      final InnerT object = iterator.next();
      if (predicateFunction(object)) {
        next = object;
        nextIsSet = true;
        return true;
      }
    }
    return false;
  }
}
