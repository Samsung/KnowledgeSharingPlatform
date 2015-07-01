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

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Bits;

/** The core terms dictionaries (BlockTermsReader,
 *  BlockTreeTermsReader) interact with a single instance
 *  of this class to manage creation of {@link DocsEnum} and
 *  {@link DocsAndPositionsEnum} instances.  It provides an
 *  IndexInput (termsIn) where this class may read any
 *  previously stored data that it had written in its
 *  corresponding {@link PostingsWriterBase} at indexing
 *  time. 
 *  @lucene.experimental */

// TODO: maybe move under blocktree?  but it's used by other terms dicts (e.g. Block)

// TODO: find a better name; this defines the API that the
// terms dict impls use to talk to a postings impl.
// TermsDict + PostingsReader/WriterBase == PostingsConsumer/Producer
public abstract class PostingsReaderBase implements Closeable, Accountable {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected PostingsReaderBase() {
  }

  /** Performs any initialization, such as reading and
   *  verifying the header from the provided terms
   *  dictionary {@link IndexInput}. */
  public abstract void init(IndexInput termsIn, SegmentReadState state) throws IOException;

  /** Return a newly created empty TermState */
  public abstract BlockTermState newTermState() throws IOException;

  /** Actually decode metadata for next term 
   *  @see PostingsWriterBase#encodeTerm 
   */
  public abstract void decodeTerm(long[] longs, DataInput in, FieldInfo fieldInfo, BlockTermState state, boolean absolute) throws IOException;

  /** Must fully consume state, since after this call that
   *  TermState may be reused. */
  public abstract DocsEnum docs(FieldInfo fieldInfo, BlockTermState state, Bits skipDocs, DocsEnum reuse, int flags) throws IOException;

  /** Must fully consume state, since after this call that
   *  TermState may be reused. */
  public abstract DocsAndPositionsEnum docsAndPositions(FieldInfo fieldInfo, BlockTermState state, Bits skipDocs, DocsAndPositionsEnum reuse,
                                                        int flags) throws IOException;
  
  /** 
   * Checks consistency of this reader.
   * <p>
   * Note that this may be costly in terms of I/O, e.g. 
   * may involve computing a checksum value against large data files.
   * @lucene.internal
   */
  public abstract void checkIntegrity() throws IOException;
  
  @Override
  public abstract void close() throws IOException;
}
