package org.apache.lucene.index;

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

import java.util.concurrent.TimeUnit;

import static java.lang.System.nanoTime;

/**
 * An implementation of {@link QueryTimeout} that can be used by
 * the {@link ExitableDirectoryReader} class to time out and exit out
 * when a query takes a long time to rewrite.
 */
public class QueryTimeoutImpl implements QueryTimeout {

  /**
   * The local variable to store the time beyond which, the processing should exit.
   */
  private Long timeoutAt;

  /** 
   * Sets the time at which to time out by adding the given timeAllowed to the current time.
   * 
   * @param timeAllowed Number of milliseconds after which to time out. Use {@code Long.MAX_VALUE}
   *                    to effectively never time out.
   */                    
  public QueryTimeoutImpl(long timeAllowed) {
    if (timeAllowed < 0L) {
      timeAllowed = Long.MAX_VALUE;
    }
    timeoutAt = nanoTime() + TimeUnit.NANOSECONDS.convert(timeAllowed, TimeUnit.MILLISECONDS);
  }

  /**
   * Returns time at which to time out, in nanoseconds relative to the (JVM-specific)
   * epoch for {@link System#nanoTime()}, to compare with the value returned by
   * {@code nanoTime()}.
   */
  public Long getTimeoutAt() {
    return timeoutAt;
  }

  /**
   * Return true if {@link #reset()} has not been called
   * and the elapsed time has exceeded the time allowed.
   */
  @Override
  public boolean shouldExit() {
    return timeoutAt != null && nanoTime() - timeoutAt > 0;
  }

  /**
   * Reset the timeout value.
   */
  public void reset() {
    timeoutAt = null;
  }
  
  @Override
  public String toString() {
    return "timeoutAt: " + timeoutAt + " (System.nanoTime(): " + nanoTime() + ")";
  }
}


