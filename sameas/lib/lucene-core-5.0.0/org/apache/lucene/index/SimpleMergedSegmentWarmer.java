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

import org.apache.lucene.index.IndexWriter.IndexReaderWarmer;
import org.apache.lucene.util.InfoStream;

/** 
 * A very simple merged segment warmer that just ensures 
 * data structures are initialized.
 */
public class SimpleMergedSegmentWarmer extends IndexReaderWarmer {
  private final InfoStream infoStream;
  
  /**
   * Creates a new SimpleMergedSegmentWarmer
   * @param infoStream InfoStream to log statistics about warming.
   */
  public SimpleMergedSegmentWarmer(InfoStream infoStream) {
    this.infoStream = infoStream;
  }
  
  @Override
  public void warm(LeafReader reader) throws IOException {
    long startTime = System.currentTimeMillis();
    int indexedCount = 0;
    int docValuesCount = 0;
    int normsCount = 0;
    for (FieldInfo info : reader.getFieldInfos()) {
      if (info.getIndexOptions() != IndexOptions.NONE) {
        reader.terms(info.name); 
        indexedCount++;
        
        if (info.hasNorms()) {
          reader.getNormValues(info.name);
          normsCount++;
        }
      }
      
      if (info.getDocValuesType() != DocValuesType.NONE) {
        switch(info.getDocValuesType()) {
          case NUMERIC:
            reader.getNumericDocValues(info.name);
            break;
          case BINARY:
            reader.getBinaryDocValues(info.name);
            break;
          case SORTED:
            reader.getSortedDocValues(info.name);
            break;
          case SORTED_NUMERIC:
            reader.getSortedNumericDocValues(info.name);
            break;
          case SORTED_SET:
            reader.getSortedSetDocValues(info.name);
            break;
          default:
            assert false; // unknown dv type
        }
        docValuesCount++;
      }   
    }
    
    reader.document(0);
    reader.getTermVectors(0);
    
    if (infoStream.isEnabled("SMSW")) {
      infoStream.message("SMSW", 
             "Finished warming segment: " + reader + 
             ", indexed=" + indexedCount + 
             ", docValues=" + docValuesCount +
             ", norms=" + normsCount +
             ", time=" + (System.currentTimeMillis() - startTime));
    }
  }
}
