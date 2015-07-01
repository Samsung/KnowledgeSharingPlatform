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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.util.Accountable;

/** 
 * Represents a file in RAM as a list of byte[] buffers.
 * @lucene.internal */
public class RAMFile implements Accountable {
  protected ArrayList<byte[]> buffers = new ArrayList<>();
  long length;
  RAMDirectory directory;
  protected long sizeInBytes;

  // File used as buffer, in no RAMDirectory
  public RAMFile() {}
  
  RAMFile(RAMDirectory directory) {
    this.directory = directory;
  }

  // For non-stream access from thread that might be concurrent with writing
  public synchronized long getLength() {
    return length;
  }

  protected synchronized void setLength(long length) {
    this.length = length;
  }

  protected final byte[] addBuffer(int size) {
    byte[] buffer = newBuffer(size);
    synchronized(this) {
      buffers.add(buffer);
      sizeInBytes += size;
    }

    if (directory != null) {
      directory.sizeInBytes.getAndAdd(size);
    }
    return buffer;
  }

  protected final synchronized byte[] getBuffer(int index) {
    return buffers.get(index);
  }

  protected final synchronized int numBuffers() {
    return buffers.size();
  }

  /**
   * Expert: allocate a new buffer. 
   * Subclasses can allocate differently. 
   * @param size size of allocated buffer.
   * @return allocated buffer.
   */
  protected byte[] newBuffer(int size) {
    return new byte[size];
  }

  @Override
  public synchronized long ramBytesUsed() {
    return sizeInBytes;
  }
  
  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(length=" + length + ")";
  }
}
