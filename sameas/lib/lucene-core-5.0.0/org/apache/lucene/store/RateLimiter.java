package org.apache.lucene.store;

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

import java.io.IOException;

import org.apache.lucene.util.ThreadInterruptedException;

/** Abstract base class to rate limit IO.  Typically implementations are
 *  shared across multiple IndexInputs or IndexOutputs (for example
 *  those involved all merging).  Those IndexInputs and
 *  IndexOutputs would call {@link #pause} whenever the have read
 *  or written more than {@link #getMinPauseCheckBytes} bytes. */
public abstract class RateLimiter {

  /**
   * Sets an updated MB per second rate limit.
   */
  public abstract void setMBPerSec(double mbPerSec);

  /**
   * The current MB per second rate limit.
   */
  public abstract double getMBPerSec();
  
  /** Pauses, if necessary, to keep the instantaneous IO
   *  rate at or below the target. 
   *  <p>
   *  Note: the implementation is thread-safe
   *  </p>
   *  @return the pause time in nano seconds 
   * */
  public abstract long pause(long bytes) throws IOException;
  
  /** How many bytes caller should add up itself before invoking {@link #pause}. */
  public abstract long getMinPauseCheckBytes();

  /**
   * Simple class to rate limit IO.
   */
  public static class SimpleRateLimiter extends RateLimiter {

    private final static int MIN_PAUSE_CHECK_MSEC = 5;

    private volatile double mbPerSec;
    private volatile long minPauseCheckBytes;
    private long lastNS;

    // TODO: we could also allow eg a sub class to dynamically
    // determine the allowed rate, eg if an app wants to
    // change the allowed rate over time or something

    /** mbPerSec is the MB/sec max IO rate */
    public SimpleRateLimiter(double mbPerSec) {
      setMBPerSec(mbPerSec);
      lastNS = System.nanoTime();
    }

    /**
     * Sets an updated mb per second rate limit.
     */
    @Override
    public void setMBPerSec(double mbPerSec) {
      this.mbPerSec = mbPerSec;
      minPauseCheckBytes = (long) ((MIN_PAUSE_CHECK_MSEC / 1000.0) * mbPerSec * 1024 * 1024);
    }

    @Override
    public long getMinPauseCheckBytes() {
      return minPauseCheckBytes;
    }

    /**
     * The current mb per second rate limit.
     */
    @Override
    public double getMBPerSec() {
      return this.mbPerSec;
    }
    
    /** Pauses, if necessary, to keep the instantaneous IO
     *  rate at or below the target.  Be sure to only call
     *  this method when bytes &gt; {@link #getMinPauseCheckBytes},
     *  otherwise it will pause way too long!
     *
     *  @return the pause time in nano seconds */  
    @Override
    public long pause(long bytes) {

      long startNS = System.nanoTime();

      double secondsToPause = (bytes/1024./1024.) / mbPerSec;

      long targetNS;

      // Sync'd to read + write lastNS:
      synchronized (this) {

        // Time we should sleep until; this is purely instantaneous
        // rate (just adds seconds onto the last time we had paused to);
        // maybe we should also offer decayed recent history one?
        targetNS = lastNS + (long) (1000000000 * secondsToPause);

        if (startNS >= targetNS) {
          // OK, current time is already beyond the target sleep time,
          // no pausing to do.

          // Set to startNS, not targetNS, to enforce the instant rate, not
          // the "averaaged over all history" rate:
          lastNS = startNS;
          return 0;
        }

        lastNS = targetNS;
      }

      long curNS = startNS;

      // While loop because Thread.sleep doesn't always sleep
      // enough:
      while (true) {
        final long pauseNS = targetNS - curNS;
        if (pauseNS > 0) {
          try {
            // NOTE: except maybe on real-time JVMs, minimum realistic sleep time
            // is 1 msec; if you pass just 1 nsec the default impl rounds
            // this up to 1 msec:
            int sleepNS;
            int sleepMS;
            if (pauseNS > 100000L * Integer.MAX_VALUE) {
              // Not really practical (sleeping for 25 days) but we shouldn't overflow int:
              sleepMS = Integer.MAX_VALUE;
              sleepNS = 0;
            } else {
              sleepMS = (int) (pauseNS/1000000);
              sleepNS = (int) (pauseNS % 1000000);
            }
            Thread.sleep(sleepMS, sleepNS);
          } catch (InterruptedException ie) {
            throw new ThreadInterruptedException(ie);
          }
          curNS = System.nanoTime();
          continue;
        }
        break;
      }

      return curNS - startNS;
    }
  }
}
