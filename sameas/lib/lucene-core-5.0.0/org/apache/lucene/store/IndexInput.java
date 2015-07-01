package org.apache.lucene.store;

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

/** Abstract base class for input from a file in a {@link Directory}.  A
 * random-access input stream.  Used for all Lucene index input operations.
 *
 * <p>{@code IndexInput} may only be used from one thread, because it is not
 * thread safe (it keeps internal state like file position). To allow
 * multithreaded use, every {@code IndexInput} instance must be cloned before
 * used in another thread. Subclasses must therefore implement {@link #clone()},
 * returning a new {@code IndexInput} which operates on the same underlying
 * resource, but positioned independently. Lucene never closes cloned
 * {@code IndexInput}s, it will only do this on the original one.
 * The original instance must take care that cloned instances throw
 * {@link AlreadyClosedException} when the original one is closed.
 
 * @see Directory
 */
public abstract class IndexInput extends DataInput implements Cloneable,Closeable {

  private final String resourceDescription;

  /** resourceDescription should be a non-null, opaque string
   *  describing this resource; it's returned from
   *  {@link #toString}. */
  protected IndexInput(String resourceDescription) {
    if (resourceDescription == null) {
      throw new IllegalArgumentException("resourceDescription must not be null");
    }
    this.resourceDescription = resourceDescription;
  }

  /** Closes the stream to further operations. */
  @Override
  public abstract void close() throws IOException;

  /** Returns the current position in this file, where the next read will
   * occur.
   * @see #seek(long)
   */
  public abstract long getFilePointer();

  /** Sets current position in this file, where the next read will occur.
   * @see #getFilePointer()
   */
  public abstract void seek(long pos) throws IOException;

  /** The number of bytes in the file. */
  public abstract long length();

  @Override
  public String toString() {
    return resourceDescription;
  }
  
  /** {@inheritDoc}
   * <p><b>Warning:</b> Lucene never closes cloned
   * {@code IndexInput}s, it will only do this on the original one.
   * The original instance must take care that cloned instances throw
   * {@link AlreadyClosedException} when the original one is closed.
   */
  @Override
  public IndexInput clone() {
    return (IndexInput) super.clone();
  }
  
  /**
   * Creates a slice of this index input, with the given description, offset, and length. 
   * The slice is seeked to the beginning.
   */
  public abstract IndexInput slice(String sliceDescription, long offset, long length) throws IOException;
  
  /**
   * Creates a random-access slice of this index input, with the given offset and length. 
   * <p>
   * The default implementation calls {@link #slice}, and it doesn't support random access,
   * it implements absolute reads as seek+read.
   */
  public RandomAccessInput randomAccessSlice(long offset, long length) throws IOException {
    final IndexInput slice = slice("randomaccess", offset, length);
    if (slice instanceof RandomAccessInput) {
      // slice() already supports random access
      return (RandomAccessInput) slice;
    } else {
      // return default impl
      return new RandomAccessInput() {
        @Override
        public byte readByte(long pos) throws IOException {
          slice.seek(pos);
          return slice.readByte();
        }
        
        @Override
        public short readShort(long pos) throws IOException {
          slice.seek(pos);
          return slice.readShort();
        }
        
        @Override
        public int readInt(long pos) throws IOException {
          slice.seek(pos);
          return slice.readInt();
        }
        
        @Override
        public long readLong(long pos) throws IOException {
          slice.seek(pos);
          return slice.readLong();
        }
      };
    }
  }
}
