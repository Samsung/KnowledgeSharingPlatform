package org.apache.lucene.search.spans;

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

/**
 * A {@link Spans} implementation which allows wrapping another spans instance
 * and override some selected methods.
 */
public class FilterSpans extends Spans {
 
  /** The wrapped spans instance. */
  protected final Spans in;
  
  /** Wrap the given {@link Spans}. */
  public FilterSpans(Spans in) {
    this.in = in;
  }
  
  @Override
  public boolean next() throws IOException {
    return in.next();
  }

  @Override
  public boolean skipTo(int target) throws IOException {
    return in.skipTo(target);
  }

  @Override
  public int doc() {
    return in.doc();
  }

  @Override
  public int start() {
    return in.start();
  }

  @Override
  public int end() {
    return in.end();
  }
  
  @Override
  public Collection<byte[]> getPayload() throws IOException {
    return in.getPayload();
  }

  @Override
  public boolean isPayloadAvailable() throws IOException {
    return in.isPayloadAvailable();
  }
  
  @Override
  public long cost() {
    return in.cost();
  }
  
  @Override
  public String toString() {
    return "Filter(" + in.toString() + ")";
  }
  
}
