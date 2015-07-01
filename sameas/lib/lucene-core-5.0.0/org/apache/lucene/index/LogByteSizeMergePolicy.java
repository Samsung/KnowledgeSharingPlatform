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

import java.io.IOException;

/** This is a {@link LogMergePolicy} that measures size of a
 *  segment as the total byte size of the segment's files. */
public class LogByteSizeMergePolicy extends LogMergePolicy {

  /** Default minimum segment size.  @see setMinMergeMB */
  public static final double DEFAULT_MIN_MERGE_MB = 1.6;

  /** Default maximum segment size.  A segment of this size
   *  or larger will never be merged.  @see setMaxMergeMB */
  public static final double DEFAULT_MAX_MERGE_MB = 2048;

  /** Default maximum segment size.  A segment of this size
   *  or larger will never be merged during forceMerge.  @see setMaxMergeMBForForceMerge */
  public static final double DEFAULT_MAX_MERGE_MB_FOR_FORCED_MERGE = Long.MAX_VALUE;

  /** Sole constructor, setting all settings to their
   *  defaults. */
  public LogByteSizeMergePolicy() {
    minMergeSize = (long) (DEFAULT_MIN_MERGE_MB*1024*1024);
    maxMergeSize = (long) (DEFAULT_MAX_MERGE_MB*1024*1024);
    maxMergeSizeForForcedMerge = (long) (DEFAULT_MAX_MERGE_MB_FOR_FORCED_MERGE*1024*1024);
  }
  
  @Override
  protected long size(SegmentCommitInfo info, IndexWriter writer) throws IOException {
    return sizeBytes(info, writer);
  }

  /** <p>Determines the largest segment (measured by total
   *  byte size of the segment's files, in MB) that may be
   *  merged with other segments.  Small values (e.g., less
   *  than 50 MB) are best for interactive indexing, as this
   *  limits the length of pauses while indexing to a few
   *  seconds.  Larger values are best for batched indexing
   *  and speedier searches.</p>
   *
   *  <p>Note that {@link #setMaxMergeDocs} is also
   *  used to check whether a segment is too large for
   *  merging (it's either or).</p>*/
  public void setMaxMergeMB(double mb) {
    maxMergeSize = (long) (mb*1024*1024);
  }

  /** Returns the largest segment (measured by total byte
   *  size of the segment's files, in MB) that may be merged
   *  with other segments.
   *  @see #setMaxMergeMB */
  public double getMaxMergeMB() {
    return ((double) maxMergeSize)/1024/1024;
  }

  /** <p>Determines the largest segment (measured by total
   *  byte size of the segment's files, in MB) that may be
   *  merged with other segments during forceMerge. Setting
   *  it low will leave the index with more than 1 segment,
   *  even if {@link IndexWriter#forceMerge} is called.*/
  public void setMaxMergeMBForForcedMerge(double mb) {
    maxMergeSizeForForcedMerge = (long) (mb*1024*1024);
  }

  /** Returns the largest segment (measured by total byte
   *  size of the segment's files, in MB) that may be merged
   *  with other segments during forceMerge.
   *  @see #setMaxMergeMBForForcedMerge */
  public double getMaxMergeMBForForcedMerge() {
    return ((double) maxMergeSizeForForcedMerge)/1024/1024;
  }

  /** Sets the minimum size for the lowest level segments.
   * Any segments below this size are considered to be on
   * the same level (even if they vary drastically in size)
   * and will be merged whenever there are mergeFactor of
   * them.  This effectively truncates the "long tail" of
   * small segments that would otherwise be created into a
   * single level.  If you set this too large, it could
   * greatly increase the merging cost during indexing (if
   * you flush many small segments). */
  public void setMinMergeMB(double mb) {
    minMergeSize = (long) (mb*1024*1024);
  }

  /** Get the minimum size for a segment to remain
   *  un-merged.
   *  @see #setMinMergeMB **/
  public double getMinMergeMB() {
    return ((double) minMergeSize)/1024/1024;
  }
}
