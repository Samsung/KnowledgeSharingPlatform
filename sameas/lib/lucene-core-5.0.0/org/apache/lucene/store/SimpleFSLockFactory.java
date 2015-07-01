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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>Implements {@link LockFactory} using {@link
 * Files#createFile}.</p>
 *
 * <p><b>NOTE:</b> the {@linkplain File#createNewFile() javadocs
 * for <code>File.createNewFile()</code>} contain a vague
 * yet spooky warning about not using the API for file
 * locking.  This warning was added due to <a target="_top"
 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4676183">this
 * bug</a>, and in fact the only known problem with using
 * this API for locking is that the Lucene write lock may
 * not be released when the JVM exits abnormally.</p>

 * <p>When this happens, a {@link LockObtainFailedException}
 * is hit when trying to create a writer, in which case you
 * need to explicitly clear the lock file first by
 * manually removing the file.  But, first be certain that
 * no writer is in fact writing to the index otherwise you
 * can easily corrupt your index.</p>
 *
 * <p>Special care needs to be taken if you change the locking
 * implementation: First be certain that no writer is in fact
 * writing to the index otherwise you can easily corrupt
 * your index. Be sure to do the LockFactory change all Lucene
 * instances and clean up all leftover lock files before starting
 * the new configuration for the first time. Different implementations
 * can not work together!</p>
 *
 * <p>If you suspect that this or any other LockFactory is
 * not working properly in your environment, you can easily
 * test it by using {@link VerifyingLockFactory}, {@link
 * LockVerifyServer} and {@link LockStressTest}.</p>
 * 
 * <p>This is a singleton, you have to use {@link #INSTANCE}.
 *
 * @see LockFactory
 */

public final class SimpleFSLockFactory extends FSLockFactory {

  /**
   * Singleton instance
   */
  public static final SimpleFSLockFactory INSTANCE = new SimpleFSLockFactory();
  
  private SimpleFSLockFactory() {}

  @Override
  protected Lock makeFSLock(FSDirectory dir, String lockName) {
    return new SimpleFSLock(dir.getDirectory(), lockName);
  }
  
  static class SimpleFSLock extends Lock {

    Path lockFile;
    Path lockDir;

    public SimpleFSLock(Path lockDir, String lockFileName) {
      this.lockDir = lockDir;
      lockFile = lockDir.resolve(lockFileName);
    }

    @Override
    public boolean obtain() throws IOException {
      try {
        Files.createDirectories(lockDir);
        Files.createFile(lockFile);
        return true;
      } catch (IOException ioe) {
        // On Windows, on concurrent createNewFile, the 2nd process gets "access denied".
        // In that case, the lock was not aquired successfully, so return false.
        // We record the failure reason here; the obtain with timeout (usually the
        // one calling us) will use this as "root cause" if it fails to get the lock.
        failureReason = ioe;
        return false;
      }
    }

    @Override
    public void close() throws LockReleaseFailedException {
      // TODO: wierd that clearLock() throws the raw IOException...
      try {
        Files.deleteIfExists(lockFile);
      } catch (Throwable cause) {
        throw new LockReleaseFailedException("failed to delete " + lockFile, cause);
      }
    }

    @Override
    public boolean isLocked() {
      return Files.exists(lockFile);
    }

    @Override
    public String toString() {
      return "SimpleFSLock@" + lockFile;
    }
  }

}
