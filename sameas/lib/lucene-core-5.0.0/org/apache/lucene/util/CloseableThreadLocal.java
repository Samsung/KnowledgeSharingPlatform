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

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Java's builtin ThreadLocal has a serious flaw:
 *  it can take an arbitrarily long amount of time to
 *  dereference the things you had stored in it, even once the
 *  ThreadLocal instance itself is no longer referenced.
 *  This is because there is single, master map stored for
 *  each thread, which all ThreadLocals share, and that
 *  master map only periodically purges "stale" entries.
 *
 *  While not technically a memory leak, because eventually
 *  the memory will be reclaimed, it can take a long time
 *  and you can easily hit OutOfMemoryError because from the
 *  GC's standpoint the stale entries are not reclaimable.
 * 
 *  This class works around that, by only enrolling
 *  WeakReference values into the ThreadLocal, and
 *  separately holding a hard reference to each stored
 *  value.  When you call {@link #close}, these hard
 *  references are cleared and then GC is freely able to
 *  reclaim space by objects stored in it.
 *
 *  We can not rely on {@link ThreadLocal#remove()} as it
 *  only removes the value for the caller thread, whereas
 *  {@link #close} takes care of all
 *  threads.  You should not call {@link #close} until all
 *  threads are done using the instance.
 *
 * @lucene.internal
 */

public class CloseableThreadLocal<T> implements Closeable {

  private ThreadLocal<WeakReference<T>> t = new ThreadLocal<>();

  // Use a WeakHashMap so that if a Thread exits and is
  // GC'able, its entry may be removed:
  private Map<Thread,T> hardRefs = new WeakHashMap<>();
  
  // Increase this to decrease frequency of purging in get:
  private static int PURGE_MULTIPLIER = 20;

  // On each get or set we decrement this; when it hits 0 we
  // purge.  After purge, we set this to
  // PURGE_MULTIPLIER * stillAliveCount.  This keeps
  // amortized cost of purging linear.
  private final AtomicInteger countUntilPurge = new AtomicInteger(PURGE_MULTIPLIER);

  protected T initialValue() {
    return null;
  }
  
  public T get() {
    WeakReference<T> weakRef = t.get();
    if (weakRef == null) {
      T iv = initialValue();
      if (iv != null) {
        set(iv);
        return iv;
      } else {
        return null;
      }
    } else {
      maybePurge();
      return weakRef.get();
    }
  }

  public void set(T object) {

    t.set(new WeakReference<>(object));

    synchronized(hardRefs) {
      hardRefs.put(Thread.currentThread(), object);
      maybePurge();
    }
  }

  private void maybePurge() {
    if (countUntilPurge.getAndDecrement() == 0) {
      purge();
    }
  }

  // Purge dead threads
  private void purge() {
    synchronized(hardRefs) {
      int stillAliveCount = 0;
      for (Iterator<Thread> it = hardRefs.keySet().iterator(); it.hasNext();) {
        final Thread t = it.next();
        if (!t.isAlive()) {
          it.remove();
        } else {
          stillAliveCount++;
        }
      }
      int nextCount = (1+stillAliveCount) * PURGE_MULTIPLIER;
      if (nextCount <= 0) {
        // defensive: int overflow!
        nextCount = 1000000;
      }
      
      countUntilPurge.set(nextCount);
    }
  }

  @Override
  public void close() {
    // Clear the hard refs; then, the only remaining refs to
    // all values we were storing are weak (unless somewhere
    // else is still using them) and so GC may reclaim them:
    hardRefs = null;
    // Take care of the current thread right now; others will be
    // taken care of via the WeakReferences.
    if (t != null) {
      t.remove();
    }
    t = null;
  }
}
