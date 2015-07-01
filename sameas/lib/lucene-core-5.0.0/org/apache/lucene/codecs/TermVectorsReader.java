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

import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute; // javadocs
import org.apache.lucene.index.DocsAndPositionsEnum; // javadocs
import org.apache.lucene.index.Fields;
import org.apache.lucene.util.Accountable;

/**
 * Codec API for reading term vectors:
 * 
 * @lucene.experimental
 */
public abstract class TermVectorsReader implements Cloneable, Closeable, Accountable {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected TermVectorsReader() {
  }

  /** Returns term vectors for this document, or null if
   *  term vectors were not indexed. If offsets are
   *  available they are in an {@link OffsetAttribute}
   *  available from the {@link DocsAndPositionsEnum}. */
  public abstract Fields get(int doc) throws IOException;
  
  /** 
   * Checks consistency of this reader.
   * <p>
   * Note that this may be costly in terms of I/O, e.g. 
   * may involve computing a checksum value against large data files.
   * @lucene.internal
   */
  public abstract void checkIntegrity() throws IOException;
  
  /** Create a clone that one caller at a time may use to
   *  read term vectors. */
  @Override
  public abstract TermVectorsReader clone();
  
  /** 
   * Returns an instance optimized for merging.
   * <p>
   * The default implementation returns {@code this} */
  public TermVectorsReader getMergeInstance() throws IOException {
    return this;
  }
}
