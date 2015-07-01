package org.apache.lucene.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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

/**
 * Manages reference counting for a given object. Extensions can override
 * {@link #release()} to do custom logic when reference counting hits 0.
 */
public class RefCount<T> {
  
  private final AtomicInteger refCount = new AtomicInteger(1);
  
  protected final T object;
  
  public RefCount(T object) {
    this.object = object;
  }

  /**
   * Called when reference counting hits 0. By default this method does nothing,
   * but extensions can override to e.g. release resources attached to object
   * that is managed by this class.
   */
  protected void release() throws IOException {}
  
  /**
   * Decrements the reference counting of this object. When reference counting
   * hits 0, calls {@link #release()}.
   */
  public final void decRef() throws IOException {
    final int rc = refCount.decrementAndGet();
    if (rc == 0) {
      boolean success = false;
      try {
        release();
        success = true;
      } finally {
        if (!success) {
          // Put reference back on failure
          refCount.incrementAndGet();
        }
      }
    } else if (rc < 0) {
      throw new IllegalStateException("too many decRef calls: refCount is " + rc + " after decrement");
    }
  }
  
  public final T get() {
    return object;
  }
  
  /** Returns the current reference count. */
  public final int getRefCount() {
    return refCount.get();
  }
  
  /**
   * Increments the reference count. Calls to this method must be matched with
   * calls to {@link #decRef()}.
   */
  public final void incRef() {
    refCount.incrementAndGet();
  }
  
}

