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

import java.io.IOException;
import java.io.EOFException;

/** A memory-resident {@link IndexInput} implementation. 
 *  
 *  @lucene.internal */
public class RAMInputStream extends IndexInput implements Cloneable {
  static final int BUFFER_SIZE = RAMOutputStream.BUFFER_SIZE;

  private final RAMFile file;
  private final long length;

  private byte[] currentBuffer;
  private int currentBufferIndex;
  
  private int bufferPosition;
  private long bufferStart;
  private int bufferLength;

  public RAMInputStream(String name, RAMFile f) throws IOException {
    this(name, f, f.length);
  }

  RAMInputStream(String name, RAMFile f, long length) throws IOException {
    super("RAMInputStream(name=" + name + ")");
    this.file = f;
    this.length = length;
    if (length/BUFFER_SIZE >= Integer.MAX_VALUE) {
      throw new IOException("RAMInputStream too large length=" + length + ": " + name); 
    }

    // make sure that we switch to the
    // first needed buffer lazily
    currentBufferIndex = -1;
    currentBuffer = null;
  }

  @Override
  public void close() {
    // nothing to do here
  }

  @Override
  public long length() {
    return length;
  }

  @Override
  public byte readByte() throws IOException {
    if (bufferPosition >= bufferLength) {
      currentBufferIndex++;
      switchCurrentBuffer(true);
    }
    return currentBuffer[bufferPosition++];
  }

  @Override
  public void readBytes(byte[] b, int offset, int len) throws IOException {
    while (len > 0) {
      if (bufferPosition >= bufferLength) {
        currentBufferIndex++;
        switchCurrentBuffer(true);
      }

      int remainInBuffer = bufferLength - bufferPosition;
      int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
      System.arraycopy(currentBuffer, bufferPosition, b, offset, bytesToCopy);
      offset += bytesToCopy;
      len -= bytesToCopy;
      bufferPosition += bytesToCopy;
    }
  }

  private final void switchCurrentBuffer(boolean enforceEOF) throws IOException {
    bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;
    if (bufferStart > length || currentBufferIndex >= file.numBuffers()) {
      // end of file reached, no more buffers left
      if (enforceEOF) {
        throw new EOFException("read past EOF: " + this);
      } else {
        // Force EOF if a read takes place at this position
        currentBufferIndex--;
        bufferPosition = BUFFER_SIZE;
      }
    } else {
      currentBuffer = file.getBuffer(currentBufferIndex);
      bufferPosition = 0;
      long buflen = length - bufferStart;
      bufferLength = buflen > BUFFER_SIZE ? BUFFER_SIZE : (int) buflen;
    }
  }

  @Override
  public long getFilePointer() {
    return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
  }

  @Override
  public void seek(long pos) throws IOException {
    if (currentBuffer==null || pos < bufferStart || pos >= bufferStart + BUFFER_SIZE) {
      currentBufferIndex = (int) (pos / BUFFER_SIZE);
      switchCurrentBuffer(false);
    }
    bufferPosition = (int) (pos % BUFFER_SIZE);
  }

  @Override
  public IndexInput slice(String sliceDescription, final long offset, final long length) throws IOException {
    if (offset < 0 || length < 0 || offset + length > this.length) {
      throw new IllegalArgumentException("slice() " + sliceDescription + " out of bounds: "  + this);
    }
    final String newResourceDescription = (sliceDescription == null) ? toString() : (toString() + " [slice=" + sliceDescription + "]");
    return new RAMInputStream(newResourceDescription, file, offset + length) {
      {
        seek(0L);
      }
      
      @Override
      public void seek(long pos) throws IOException {
        if (pos < 0L) {
          throw new IllegalArgumentException("Seeking to negative position: " + this);
        }
        super.seek(pos + offset);
      }
      
      @Override
      public long getFilePointer() {
        return super.getFilePointer() - offset;
      }

      @Override
      public long length() {
        return super.length() - offset;
      }

      @Override
      public IndexInput slice(String sliceDescription, long ofs, long len) throws IOException {
        return super.slice(sliceDescription, offset + ofs, len);
      }
    };
  }
}
