package org.apache.lucene.index;

import java.io.IOException;

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
 * A utility for executing 2-phase commit on several objects.
 * 
 * @see TwoPhaseCommit
 * @lucene.experimental
 */
public final class TwoPhaseCommitTool {
  
  /** No instance */
  private TwoPhaseCommitTool() {}

  /**
   * Thrown by {@link TwoPhaseCommitTool#execute(TwoPhaseCommit...)} when an
   * object fails to prepareCommit().
   */
  public static class PrepareCommitFailException extends IOException {

    /** Sole constructor. */
    public PrepareCommitFailException(Throwable cause, TwoPhaseCommit obj) {
      super("prepareCommit() failed on " + obj, cause);
    }
  }

  /**
   * Thrown by {@link TwoPhaseCommitTool#execute(TwoPhaseCommit...)} when an
   * object fails to commit().
   */
  public static class CommitFailException extends IOException {

    /** Sole constructor. */
    public CommitFailException(Throwable cause, TwoPhaseCommit obj) {
      super("commit() failed on " + obj, cause);
    }
    
  }

  /** rollback all objects, discarding any exceptions that occur. */
  private static void rollback(TwoPhaseCommit... objects) {
    for (TwoPhaseCommit tpc : objects) {
      // ignore any exception that occurs during rollback - we want to ensure
      // all objects are rolled-back.
      if (tpc != null) {
        try {
          tpc.rollback();
        } catch (Throwable t) {}
      }
    }
  }

  /**
   * Executes a 2-phase commit algorithm by first
   * {@link TwoPhaseCommit#prepareCommit()} all objects and only if all succeed,
   * it proceeds with {@link TwoPhaseCommit#commit()}. If any of the objects
   * fail on either the preparation or actual commit, it terminates and
   * {@link TwoPhaseCommit#rollback()} all of them.
   * <p>
   * <b>NOTE:</b> it may happen that an object fails to commit, after few have
   * already successfully committed. This tool will still issue a rollback
   * instruction on them as well, but depending on the implementation, it may
   * not have any effect.
   * <p>
   * <b>NOTE:</b> if any of the objects are {@code null}, this method simply
   * skips over them.
   * 
   * @throws PrepareCommitFailException
   *           if any of the objects fail to
   *           {@link TwoPhaseCommit#prepareCommit()}
   * @throws CommitFailException
   *           if any of the objects fail to {@link TwoPhaseCommit#commit()}
   */
  public static void execute(TwoPhaseCommit... objects)
      throws PrepareCommitFailException, CommitFailException {
    TwoPhaseCommit tpc = null;
    try {
      // first, all should successfully prepareCommit()
      for (int i = 0; i < objects.length; i++) {
        tpc = objects[i];
        if (tpc != null) {
          tpc.prepareCommit();
        }
      }
    } catch (Throwable t) {
      // first object that fails results in rollback all of them and
      // throwing an exception.
      rollback(objects);
      throw new PrepareCommitFailException(t, tpc);
    }
    
    // If all successfully prepareCommit(), attempt the actual commit()
    try {
      for (int i = 0; i < objects.length; i++) {
        tpc = objects[i];
        if (tpc != null) {
          tpc.commit();
        }
      }
    } catch (Throwable t) {
      // first object that fails results in rollback all of them and
      // throwing an exception.
      rollback(objects);
      throw new CommitFailException(t, tpc);
    }
  }

}
