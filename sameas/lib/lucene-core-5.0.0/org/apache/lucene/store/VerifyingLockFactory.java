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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link LockFactory} that wraps another {@link
 * LockFactory} and verifies that each lock obtain/release
 * is "correct" (never results in two processes holding the
 * lock at the same time).  It does this by contacting an
 * external server ({@link LockVerifyServer}) to assert that
 * at most one process holds the lock at a time.  To use
 * this, you should also run {@link LockVerifyServer} on the
 * host and port matching what you pass to the constructor.
 *
 * @see LockVerifyServer
 * @see LockStressTest
 */

public final class VerifyingLockFactory extends LockFactory {

  final LockFactory lf;
  final InputStream in;
  final OutputStream out;

  private class CheckedLock extends Lock {
    private final Lock lock;

    public CheckedLock(Lock lock) {
      this.lock = lock;
    }

    private void verify(byte message) throws IOException {
      out.write(message);
      out.flush();
      final int ret = in.read();
      if (ret < 0) {
        throw new IllegalStateException("Lock server died because of locking error.");
      }
      if (ret != message) {
        throw new IOException("Protocol violation.");
      }
    }

    @Override
    public synchronized boolean obtain() throws IOException {
      boolean obtained = lock.obtain();
      if (obtained)
        verify((byte) 1);
      return obtained;
    }

    @Override
    public synchronized boolean isLocked() throws IOException {
      return lock.isLocked();
    }

    @Override
    public synchronized void close() throws IOException {
      if (isLocked()) {
        verify((byte) 0);
        lock.close();
      }
    }
  }

  /**
   * @param lf the LockFactory that we are testing
   * @param in the socket's input to {@link LockVerifyServer}
   * @param out the socket's output to {@link LockVerifyServer}
  */
  public VerifyingLockFactory(LockFactory lf, InputStream in, OutputStream out) throws IOException {
    this.lf = lf;
    this.in = in;
    this.out = out;
  }

  @Override
  public Lock makeLock(Directory dir, String lockName) {
    return new CheckedLock(lf.makeLock(dir, lockName));
  }
}
