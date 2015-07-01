package org.apache.lucene.util;

import java.util.concurrent.atomic.AtomicLong;

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
 * Simple counter class
 * 
 * @lucene.internal
 * @lucene.experimental
 */
public abstract class Counter {

  /**
   * Adds the given delta to the counters current value
   * 
   * @param delta
   *          the delta to add
   * @return the counters updated value
   */
  public abstract long addAndGet(long delta);

  /**
   * Returns the counters current value
   * 
   * @return the counters current value
   */
  public abstract long get();

  /**
   * Returns a new counter. The returned counter is not thread-safe.
   */
  public static Counter newCounter() {
    return newCounter(false);
  }

  /**
   * Returns a new counter.
   * 
   * @param threadSafe
   *          <code>true</code> if the returned counter can be used by multiple
   *          threads concurrently.
   * @return a new counter.
   */
  public static Counter newCounter(boolean threadSafe) {
    return threadSafe ? new AtomicCounter() : new SerialCounter();
  }

  private final static class SerialCounter extends Counter {
    private long count = 0;

    @Override
    public long addAndGet(long delta) {
      return count += delta;
    }

    @Override
    public long get() {
      return count;
    };
  }

  private final static class AtomicCounter extends Counter {
    private final AtomicLong count = new AtomicLong();

    @Override
    public long addAndGet(long delta) {
      return count.addAndGet(delta);
    }

    @Override
    public long get() {
      return count.get();
    }

  }
}
