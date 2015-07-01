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
import java.util.Collection;

import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;

/**
 * Encodes/decodes compound files
 * @lucene.experimental
 */
public abstract class CompoundFormat {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  public CompoundFormat() {
  }
  
  // TODO: this is very minimal. If we need more methods,
  // we can add 'producer' classes.
  
  /**
   * Returns a Directory view (read-only) for the compound files in this segment
   */
  public abstract Directory getCompoundReader(Directory dir, SegmentInfo si, IOContext context) throws IOException;
  
  /**
   * Packs the provided files into a compound format.
   */
  public abstract void write(Directory dir, SegmentInfo si, Collection<String> files, IOContext context) throws IOException;

  /**
   * Returns the compound file names used by this segment.
   */
  // TODO: get this out of here, and use trackingdirwrapper. but this is really scary in IW right now...
  // NOTE: generally si.useCompoundFile is not even yet 'set' when this is called.
  public abstract String[] files(SegmentInfo si);
}
