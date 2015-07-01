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

import java.util.zip.Checksum;

/** 
 * Wraps another {@link Checksum} with an internal buffer
 * to speed up checksum calculations.
 */
public class BufferedChecksum implements Checksum {
  private final Checksum in;
  private final byte buffer[];
  private int upto;
  /** Default buffer size: 256 */
  public static final int DEFAULT_BUFFERSIZE = 256;
  
  /** Create a new BufferedChecksum with {@link #DEFAULT_BUFFERSIZE} */
  public BufferedChecksum(Checksum in) {
    this(in, DEFAULT_BUFFERSIZE);
  }
  
  /** Create a new BufferedChecksum with the specified bufferSize */
  public BufferedChecksum(Checksum in, int bufferSize) {
    this.in = in;
    this.buffer = new byte[bufferSize];
  }
  
  @Override
  public void update(int b) {
    if (upto == buffer.length) {
      flush();
    }
    buffer[upto++] = (byte) b;
  }

  @Override
  public void update(byte[] b, int off, int len) {
    if (len >= buffer.length) {
      flush();
      in.update(b, off, len);
    } else { 
      if (upto + len > buffer.length) {
        flush();
      }
      System.arraycopy(b, off, buffer, upto, len);
      upto += len;
    } 
  }

  @Override
  public long getValue() {
    flush();
    return in.getValue();
  }

  @Override
  public void reset() {
    upto = 0;
    in.reset();
  }
  
  private void flush() {
    if (upto > 0) {
      in.update(buffer, 0, upto);
    }
    upto = 0;
  }
}
