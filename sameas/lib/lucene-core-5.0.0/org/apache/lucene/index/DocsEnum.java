package org.apache.lucene.index;

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

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Bits; // javadocs

/** Iterates through the documents and term freqs.
 *  NOTE: you must first call {@link #nextDoc} before using
 *  any of the per-doc methods. */
public abstract class DocsEnum extends DocIdSetIterator {
  
  /**
   * Flag to pass to {@link TermsEnum#docs(Bits,DocsEnum,int)} if you don't
   * require term frequencies in the returned enum. When passed to
   * {@link TermsEnum#docsAndPositions(Bits,DocsAndPositionsEnum,int)} means
   * that no offsets and payloads will be returned.
   */
  public static final int FLAG_NONE = 0x0;

  /** Flag to pass to {@link TermsEnum#docs(Bits,DocsEnum,int)}
   *  if you require term frequencies in the returned enum. */
  public static final int FLAG_FREQS = 0x1;

  private AttributeSource atts = null;

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected DocsEnum() {
  }

  /**
   * Returns term frequency in the current document, or 1 if the field was
   * indexed with {@link IndexOptions#DOCS}. Do not call this before
   * {@link #nextDoc} is first called, nor after {@link #nextDoc} returns
   * {@link DocIdSetIterator#NO_MORE_DOCS}.
   * 
   * <p>
   * <b>NOTE:</b> if the {@link DocsEnum} was obtain with {@link #FLAG_NONE},
   * the result of this method is undefined.
   */
  public abstract int freq() throws IOException;
  
  /** Returns the related attributes. */
  public AttributeSource attributes() {
    if (atts == null) atts = new AttributeSource();
    return atts;
  }
}
