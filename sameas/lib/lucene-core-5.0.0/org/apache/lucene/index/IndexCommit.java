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

import java.util.Collection;
import java.util.Map;
import java.io.IOException;

import org.apache.lucene.store.Directory;

/**
 * <p>Expert: represents a single commit into an index as seen by the
 * {@link IndexDeletionPolicy} or {@link IndexReader}.</p>
 *
 * <p> Changes to the content of an index are made visible
 * only after the writer who made that change commits by
 * writing a new segments file
 * (<code>segments_N</code>). This point in time, when the
 * action of writing of a new segments file to the directory
 * is completed, is an index commit.</p>
 *
 * <p>Each index commit point has a unique segments file
 * associated with it. The segments file associated with a
 * later index commit point would have a larger N.</p>
 *
 * @lucene.experimental
*/

public abstract class IndexCommit implements Comparable<IndexCommit> {

  /**
   * Get the segments file (<code>segments_N</code>) associated 
   * with this commit point.
   */
  public abstract String getSegmentsFileName();

  /**
   * Returns all index files referenced by this commit point.
   */
  public abstract Collection<String> getFileNames() throws IOException;

  /**
   * Returns the {@link Directory} for the index.
   */
  public abstract Directory getDirectory();
  
  /**
   * Delete this commit point.  This only applies when using
   * the commit point in the context of IndexWriter's
   * IndexDeletionPolicy.
   * <p>
   * Upon calling this, the writer is notified that this commit 
   * point should be deleted. 
   * <p>
   * Decision that a commit-point should be deleted is taken by the {@link IndexDeletionPolicy} in effect
   * and therefore this should only be called by its {@link IndexDeletionPolicy#onInit onInit()} or 
   * {@link IndexDeletionPolicy#onCommit onCommit()} methods.
  */
  public abstract void delete();

  /** Returns true if this commit should be deleted; this is
   *  only used by {@link IndexWriter} after invoking the
   *  {@link IndexDeletionPolicy}. */
  public abstract boolean isDeleted();

  /** Returns number of segments referenced by this commit. */
  public abstract int getSegmentCount();

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected IndexCommit() {
  }

  /** Two IndexCommits are equal if both their Directory and versions are equal. */
  @Override
  public boolean equals(Object other) {
    if (other instanceof IndexCommit) {
      IndexCommit otherCommit = (IndexCommit) other;
      return otherCommit.getDirectory() == getDirectory() && otherCommit.getGeneration() == getGeneration();
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return getDirectory().hashCode() + Long.valueOf(getGeneration()).hashCode();
  }

  /** Returns the generation (the _N in segments_N) for this
   *  IndexCommit */
  public abstract long getGeneration();

  /** Returns userData, previously passed to {@link
   *  IndexWriter#setCommitData(Map)} for this commit.  Map is
   *  {@code String -> String}. */
  public abstract Map<String,String> getUserData() throws IOException;
  
  @Override
  public int compareTo(IndexCommit commit) {
    if (getDirectory() != commit.getDirectory()) {
      throw new UnsupportedOperationException("cannot compare IndexCommits from different Directory instances");
    }

    long gen = getGeneration();
    long comgen = commit.getGeneration();
    return Long.compare(gen, comgen);
  }
}
