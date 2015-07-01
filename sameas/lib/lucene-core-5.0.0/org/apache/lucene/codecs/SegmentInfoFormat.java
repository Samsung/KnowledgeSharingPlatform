package org.apache.lucene.codecs;

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

import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;

/**
 * Expert: Controls the format of the 
 * {@link SegmentInfo} (segment metadata file).
 * <p>
 * 
 * @see SegmentInfo
 * @lucene.experimental
 */
public abstract class SegmentInfoFormat {
  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected SegmentInfoFormat() {
  }

  /**
   * Read {@link SegmentInfo} data from a directory.
   * @param directory directory to read from
   * @param segmentName name of the segment to read
   * @param segmentID expected identifier for the segment
   * @return infos instance to be populated with data
   * @throws IOException If an I/O error occurs
   */
  public abstract SegmentInfo read(Directory directory, String segmentName, byte segmentID[], IOContext context) throws IOException;

  /**
   * Write {@link SegmentInfo} data.
   * The codec must add its SegmentInfo filename(s) to {@code info} before doing i/o. 
   * @throws IOException If an I/O error occurs
   */
  public abstract void write(Directory dir, SegmentInfo info, IOContext ioContext) throws IOException;
}
