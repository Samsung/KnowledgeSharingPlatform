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

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Bits;

/** Abstract API that produces numeric, binary, sorted, sortedset,
 *  and sortednumeric docvalues.
 *
 * @lucene.experimental
 */
public abstract class DocValuesProducer implements Closeable, Accountable {
  
  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected DocValuesProducer() {}

  /** Returns {@link NumericDocValues} for this field.
   *  The returned instance need not be thread-safe: it will only be
   *  used by a single thread. */
  public abstract NumericDocValues getNumeric(FieldInfo field) throws IOException;

  /** Returns {@link BinaryDocValues} for this field.
   *  The returned instance need not be thread-safe: it will only be
   *  used by a single thread. */
  public abstract BinaryDocValues getBinary(FieldInfo field) throws IOException;

  /** Returns {@link SortedDocValues} for this field.
   *  The returned instance need not be thread-safe: it will only be
   *  used by a single thread. */
  public abstract SortedDocValues getSorted(FieldInfo field) throws IOException;
  
  /** Returns {@link SortedNumericDocValues} for this field.
   *  The returned instance need not be thread-safe: it will only be
   *  used by a single thread. */
  public abstract SortedNumericDocValues getSortedNumeric(FieldInfo field) throws IOException;
  
  /** Returns {@link SortedSetDocValues} for this field.
   *  The returned instance need not be thread-safe: it will only be
   *  used by a single thread. */
  public abstract SortedSetDocValues getSortedSet(FieldInfo field) throws IOException;
  
  /** Returns a {@link Bits} at the size of <code>reader.maxDoc()</code>, 
   *  with turned on bits for each docid that does have a value for this field.
   *  The returned instance need not be thread-safe: it will only be
   *  used by a single thread. */
  public abstract Bits getDocsWithField(FieldInfo field) throws IOException;
  
  /** 
   * Checks consistency of this producer
   * <p>
   * Note that this may be costly in terms of I/O, e.g. 
   * may involve computing a checksum value against large data files.
   * @lucene.internal
   */
  public abstract void checkIntegrity() throws IOException;
  
  /** 
   * Returns an instance optimized for merging.
   * <p>
   * The default implementation returns {@code this} */
  public DocValuesProducer getMergeInstance() throws IOException {
    return this;
  }
}
