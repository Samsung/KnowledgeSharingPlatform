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
import java.util.Collection;
import java.util.Collections;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Accountables;

/**
 * A memory-resident {@link IndexOutput} implementation.
 *
 * @lucene.internal
 */
public class RAMOutputStream extends IndexOutput implements Accountable {
  static final int BUFFER_SIZE = 1024;

  private final RAMFile file;

  private byte[] currentBuffer;
  private int currentBufferIndex;
  
  private int bufferPosition;
  private long bufferStart;
  private int bufferLength;
  
  private final Checksum crc;

  /** Construct an empty output buffer. */
  public RAMOutputStream() {
    this("noname", new RAMFile(), false);
  }

  /** Creates this, with no name. */
  public RAMOutputStream(RAMFile f, boolean checksum) {
    this("noname", f, checksum);
  }

  /** Creates this, with specified name. */
  public RAMOutputStream(String name, RAMFile f, boolean checksum) {
    super("RAMOutputStream(name=\"" + name + "\")");
    file = f;

    // make sure that we switch to the
    // first needed buffer lazily
    currentBufferIndex = -1;
    currentBuffer = null;
    if (checksum) {
      crc = new BufferedChecksum(new CRC32());
    } else {
      crc = null;
    }
  }

  /** Copy the current contents of this buffer to the named output. */
  public void writeTo(DataOutput out) throws IOException {
    flush();
    final long end = file.length;
    long pos = 0;
    int buffer = 0;
    while (pos < end) {
      int length = BUFFER_SIZE;
      long nextPos = pos + length;
      if (nextPos > end) {                        // at the last buffer
        length = (int)(end - pos);
      }
      out.writeBytes(file.getBuffer(buffer++), length);
      pos = nextPos;
    }
  }

  /** Copy the current contents of this buffer to output
   *  byte array */
  public void writeTo(byte[] bytes, int offset) throws IOException {
    flush();
    final long end = file.length;
    long pos = 0;
    int buffer = 0;
    int bytesUpto = offset;
    while (pos < end) {
      int length = BUFFER_SIZE;
      long nextPos = pos + length;
      if (nextPos > end) {                        // at the last buffer
        length = (int)(end - pos);
      }
      System.arraycopy(file.getBuffer(buffer++), 0, bytes, bytesUpto, length);
      bytesUpto += length;
      pos = nextPos;
    }
  }

  /** Resets this to an empty file. */
  public void reset() {
    currentBuffer = null;
    currentBufferIndex = -1;
    bufferPosition = 0;
    bufferStart = 0;
    bufferLength = 0;
    file.setLength(0);
    if (crc != null) {
      crc.reset();
    }
  }

  @Override
  public void close() throws IOException {
    flush();
  }

  @Override
  public void writeByte(byte b) throws IOException {
    if (bufferPosition == bufferLength) {
      currentBufferIndex++;
      switchCurrentBuffer();
    }
    if (crc != null) {
      crc.update(b);
    }
    currentBuffer[bufferPosition++] = b;
  }

  @Override
  public void writeBytes(byte[] b, int offset, int len) throws IOException {
    assert b != null;
    if (crc != null) {
      crc.update(b, offset, len);
    }
    while (len > 0) {
      if (bufferPosition ==  bufferLength) {
        currentBufferIndex++;
        switchCurrentBuffer();
      }

      int remainInBuffer = currentBuffer.length - bufferPosition;
      int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
      System.arraycopy(b, offset, currentBuffer, bufferPosition, bytesToCopy);
      offset += bytesToCopy;
      len -= bytesToCopy;
      bufferPosition += bytesToCopy;
    }
  }

  private final void switchCurrentBuffer() {
    if (currentBufferIndex == file.numBuffers()) {
      currentBuffer = file.addBuffer(BUFFER_SIZE);
    } else {
      currentBuffer = file.getBuffer(currentBufferIndex);
    }
    bufferPosition = 0;
    bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;
    bufferLength = currentBuffer.length;
  }

  private void setFileLength() {
    long pointer = bufferStart + bufferPosition;
    if (pointer > file.length) {
      file.setLength(pointer);
    }
  }

  /** Forces any buffered output to be written. */
  protected void flush() throws IOException {
    setFileLength();
  }

  @Override
  public long getFilePointer() {
    return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
  }

  /** Returns byte usage of all buffers. */
  @Override
  public long ramBytesUsed() {
    return (long) file.numBuffers() * (long) BUFFER_SIZE;
  }
  
  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.singleton(Accountables.namedAccountable("file", file));
  }

  @Override
  public long getChecksum() throws IOException {
    if (crc == null) {
      throw new IllegalStateException("internal RAMOutputStream created with checksum disabled");
    } else {
      return crc.getValue();
    }
  }
}
