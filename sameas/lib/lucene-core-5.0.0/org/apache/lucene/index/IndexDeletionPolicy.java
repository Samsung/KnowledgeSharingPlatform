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

import java.util.List;
import java.io.IOException;

import org.apache.lucene.store.Directory;

/**
 * <p>Expert: policy for deletion of stale {@link IndexCommit index commits}. 
 * 
 * <p>Implement this interface, and pass it to one
 * of the {@link IndexWriter} or {@link IndexReader}
 * constructors, to customize when older
 * {@link IndexCommit point-in-time commits}
 * are deleted from the index directory.  The default deletion policy
 * is {@link KeepOnlyLastCommitDeletionPolicy}, which always
 * removes old commits as soon as a new commit is done (this
 * matches the behavior before 2.2).</p>
 *
 * <p>One expected use case for this (and the reason why it
 * was first created) is to work around problems with an
 * index directory accessed via filesystems like NFS because
 * NFS does not provide the "delete on last close" semantics
 * that Lucene's "point in time" search normally relies on.
 * By implementing a custom deletion policy, such as "a
 * commit is only removed once it has been stale for more
 * than X minutes", you can give your readers time to
 * refresh to the new commit before {@link IndexWriter}
 * removes the old commits.  Note that doing so will
 * increase the storage requirements of the index.  See <a
 * target="top"
 * href="http://issues.apache.org/jira/browse/LUCENE-710">LUCENE-710</a>
 * for details.</p>
 *
 * <p>Implementers of sub-classes should make sure that {@link #clone()}
 * returns an independent instance able to work with any other {@link IndexWriter}
 * or {@link Directory} instance.</p>
 */

public abstract class IndexDeletionPolicy {

  /** Sole constructor, typically called by sub-classes constructors. */
  protected IndexDeletionPolicy() {}

  /**
   * <p>This is called once when a writer is first
   * instantiated to give the policy a chance to remove old
   * commit points.</p>
   * 
   * <p>The writer locates all index commits present in the 
   * index directory and calls this method.  The policy may 
   * choose to delete some of the commit points, doing so by
   * calling method {@link IndexCommit#delete delete()} 
   * of {@link IndexCommit}.</p>
   * 
   * <p><u>Note:</u> the last CommitPoint is the most recent one,
   * i.e. the "front index state". Be careful not to delete it,
   * unless you know for sure what you are doing, and unless 
   * you can afford to lose the index content while doing that. 
   *
   * @param commits List of current 
   * {@link IndexCommit point-in-time commits},
   *  sorted by age (the 0th one is the oldest commit).
   *  Note that for a new index this method is invoked with
   *  an empty list.
   */
  public abstract void onInit(List<? extends IndexCommit> commits) throws IOException;

  /**
   * <p>This is called each time the writer completed a commit.
   * This gives the policy a chance to remove old commit points
   * with each commit.</p>
   *
   * <p>The policy may now choose to delete old commit points 
   * by calling method {@link IndexCommit#delete delete()} 
   * of {@link IndexCommit}.</p>
   * 
   * <p>This method is only called when {@link
   * IndexWriter#commit} or {@link IndexWriter#close} is
   * called, or possibly not at all if the {@link
   * IndexWriter#rollback} is called.
   *
   * <p><u>Note:</u> the last CommitPoint is the most recent one,
   * i.e. the "front index state". Be careful not to delete it,
   * unless you know for sure what you are doing, and unless 
   * you can afford to lose the index content while doing that.
   *  
   * @param commits List of {@link IndexCommit},
   *  sorted by age (the 0th one is the oldest commit).
   */
  public abstract void onCommit(List<? extends IndexCommit> commits) throws IOException;
}
