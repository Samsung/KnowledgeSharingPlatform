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

import org.apache.lucene.codecs.PostingsFormat; // javadocs
import org.apache.lucene.codecs.perfield.PerFieldPostingsFormat; // javadocs
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.util.InfoStream;
import org.apache.lucene.util.MutableBits;

/**
 * Holder class for common parameters used during write.
 * @lucene.experimental
 */
public class SegmentWriteState {

  /** {@link InfoStream} used for debugging messages. */
  public final InfoStream infoStream;

  /** {@link Directory} where this segment will be written
   *  to. */
  public final Directory directory;

  /** {@link SegmentInfo} describing this segment. */
  public final SegmentInfo segmentInfo;

  /** {@link FieldInfos} describing all fields in this
   *  segment. */
  public final FieldInfos fieldInfos;

  /** Number of deleted documents set while flushing the
   *  segment. */
  public int delCountOnFlush;

  /**
   * Deletes and updates to apply while we are flushing the segment. A Term is
   * enrolled in here if it was deleted/updated at one point, and it's mapped to
   * the docIDUpto, meaning any docID &lt; docIDUpto containing this term should
   * be deleted/updated.
   */
  public final BufferedUpdates segUpdates;

  /** {@link MutableBits} recording live documents; this is
   *  only set if there is one or more deleted documents. */
  public MutableBits liveDocs;

  /** Unique suffix for any postings files written for this
   *  segment.  {@link PerFieldPostingsFormat} sets this for
   *  each of the postings formats it wraps.  If you create
   *  a new {@link PostingsFormat} then any files you
   *  write/read must be derived using this suffix (use
   *  {@link IndexFileNames#segmentFileName(String,String,String)}).
   *  
   *  Note: the suffix must be either empty, or be a textual suffix contain exactly two parts (separated by underscore), or be a base36 generation. */
  public final String segmentSuffix;
  
  /** {@link IOContext} for all writes; you should pass this
   *  to {@link Directory#createOutput(String,IOContext)}. */
  public final IOContext context;

  /** Sole constructor. */
  public SegmentWriteState(InfoStream infoStream, Directory directory, SegmentInfo segmentInfo, FieldInfos fieldInfos,
      BufferedUpdates segUpdates, IOContext context) {
    this(infoStream, directory, segmentInfo, fieldInfos, segUpdates, context, "");
  }

  /**
   * Constructor which takes segment suffix.
   * 
   * @see #SegmentWriteState(InfoStream, Directory, SegmentInfo, FieldInfos,
   *      BufferedUpdates, IOContext)
   */
  public SegmentWriteState(InfoStream infoStream, Directory directory, SegmentInfo segmentInfo, FieldInfos fieldInfos,
      BufferedUpdates segUpdates, IOContext context, String segmentSuffix) {
    this.infoStream = infoStream;
    this.segUpdates = segUpdates;
    this.directory = directory;
    this.segmentInfo = segmentInfo;
    this.fieldInfos = fieldInfos;
    assert assertSegmentSuffix(segmentSuffix);
    this.segmentSuffix = segmentSuffix;
    this.context = context;
  }
  
  /** Create a shallow copy of {@link SegmentWriteState} with a new segment suffix. */
  public SegmentWriteState(SegmentWriteState state, String segmentSuffix) {
    infoStream = state.infoStream;
    directory = state.directory;
    segmentInfo = state.segmentInfo;
    fieldInfos = state.fieldInfos;
    context = state.context;
    this.segmentSuffix = segmentSuffix;
    segUpdates = state.segUpdates;
    delCountOnFlush = state.delCountOnFlush;
    liveDocs = state.liveDocs;
  }
  
  // currently only used by assert? clean up and make real check?
  // either it's a segment suffix (_X_Y) or it's a parseable generation
  // TODO: this is very confusing how ReadersAndUpdates passes generations via
  // this mechanism, maybe add 'generation' explicitly to ctor create the 'actual suffix' here?
  private boolean assertSegmentSuffix(String segmentSuffix) {
    assert segmentSuffix != null;
    if (!segmentSuffix.isEmpty()) {
      int numParts = segmentSuffix.split("_").length;
      if (numParts == 2) {
        return true;
      } else if (numParts == 1) {
        Long.parseLong(segmentSuffix, Character.MAX_RADIX);
        return true;
      }
      return false; // invalid
    }
    return true;
  }
}
