package org.apache.lucene.analysis.payloads;
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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.util.BytesRef;


/**
 *  Does nothing other than convert the char array to a byte array using the specified encoding.
 *
 **/
public class IdentityEncoder extends AbstractEncoder implements PayloadEncoder{
  protected Charset charset = StandardCharsets.UTF_8;
  
  public IdentityEncoder() {
  }

  public IdentityEncoder(Charset charset) {
    this.charset = charset;
  }

  @Override
  public BytesRef encode(char[] buffer, int offset, int length) {
    final ByteBuffer bb = charset.encode(CharBuffer.wrap(buffer, offset, length));
    if (bb.hasArray()) {
      return new BytesRef(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
    } else {
      // normally it should always have an array, but who knows?
      final byte[] b = new byte[bb.remaining()];
      bb.get(b);
      return new BytesRef(b);
    }
  }
}
