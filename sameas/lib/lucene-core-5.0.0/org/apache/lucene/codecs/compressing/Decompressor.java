package org.apache.lucene.codecs.compressing;

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

import org.apache.lucene.store.DataInput;
import org.apache.lucene.util.BytesRef;

/**
 * A decompressor.
 */
public abstract class Decompressor implements Cloneable {

  /** Sole constructor, typically called from sub-classes. */
  protected Decompressor() {}

  /**
   * Decompress bytes that were stored between offsets <code>offset</code> and
   * <code>offset+length</code> in the original stream from the compressed
   * stream <code>in</code> to <code>bytes</code>. After returning, the length
   * of <code>bytes</code> (<code>bytes.length</code>) must be equal to
   * <code>length</code>. Implementations of this method are free to resize
   * <code>bytes</code> depending on their needs.
   *
   * @param in the input that stores the compressed stream
   * @param originalLength the length of the original data (before compression)
   * @param offset bytes before this offset do not need to be decompressed
   * @param length bytes after <code>offset+length</code> do not need to be decompressed
   * @param bytes a {@link BytesRef} where to store the decompressed data
   */
  public abstract void decompress(DataInput in, int originalLength, int offset, int length, BytesRef bytes) throws IOException;

  @Override
  public abstract Decompressor clone();

}
