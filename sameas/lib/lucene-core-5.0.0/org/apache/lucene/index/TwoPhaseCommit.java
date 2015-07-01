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
 * An interface for implementations that support 2-phase commit. You can use
 * {@link TwoPhaseCommitTool} to execute a 2-phase commit algorithm over several
 * {@link TwoPhaseCommit}s.
 * 
 * @lucene.experimental
 */
public interface TwoPhaseCommit {

  /**
   * The first stage of a 2-phase commit. Implementations should do as much work
   * as possible in this method, but avoid actual committing changes. If the
   * 2-phase commit fails, {@link #rollback()} is called to discard all changes
   * since last successful commit.
   */
  public void prepareCommit() throws IOException;

  /**
   * The second phase of a 2-phase commit. Implementations should ideally do
   * very little work in this method (following {@link #prepareCommit()}, and
   * after it returns, the caller can assume that the changes were successfully
   * committed to the underlying storage.
   */
  public void commit() throws IOException;

  /**
   * Discards any changes that have occurred since the last commit. In a 2-phase
   * commit algorithm, where one of the objects failed to {@link #commit()} or
   * {@link #prepareCommit()}, this method is used to roll all other objects
   * back to their previous state.
   */
  public void rollback() throws IOException;

}
