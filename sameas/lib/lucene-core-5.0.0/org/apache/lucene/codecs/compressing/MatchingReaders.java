package org.apache.lucene.codecs.compressing;

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

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.MergeState;
import org.apache.lucene.index.SegmentReader;

/** 
 * Computes which segments have identical field name to number mappings,
 * which allows stored fields and term vectors in this codec to be bulk-merged.
 */
class MatchingReaders {
  
  /** {@link SegmentReader}s that have identical field
   * name/number mapping, so their stored fields and term
   * vectors may be bulk merged. */
  final boolean[] matchingReaders;

  /** How many {@link #matchingReaders} are set. */
  final int count;
  
  MatchingReaders(MergeState mergeState) {
    // If the i'th reader is a SegmentReader and has
    // identical fieldName -> number mapping, then this
    // array will be non-null at position i:
    int numReaders = mergeState.maxDocs.length;
    int matchedCount = 0;
    matchingReaders = new boolean[numReaders];

    // If this reader is a SegmentReader, and all of its
    // field name -> number mappings match the "merged"
    // FieldInfos, then we can do a bulk copy of the
    // stored fields:

    nextReader:
    for (int i = 0; i < numReaders; i++) {
      for (FieldInfo fi : mergeState.fieldInfos[i]) {
        FieldInfo other = mergeState.mergeFieldInfos.fieldInfo(fi.number);
        if (other == null || !other.name.equals(fi.name)) {
          continue nextReader;
        }
      }
      matchingReaders[i] = true;
      matchedCount++;
    }
    
    this.count = matchedCount;

    if (mergeState.infoStream.isEnabled("SM")) {
      mergeState.infoStream.message("SM", "merge store matchedCount=" + count + " vs " + numReaders);
      if (count != numReaders) {
        mergeState.infoStream.message("SM", "" + (numReaders - count) + " non-bulk merges");
      }
    }
  }
}
