package org.apache.lucene.analysis.tokenattributes;

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

import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.BytesRef;

/**
 * This attribute is requested by TermsHashPerField to index the contents.
 * This attribute can be used to customize the final byte[] encoding of terms.
 * <p>
 * Consumers of this attribute call {@link #getBytesRef()} up-front, and then
 * invoke {@link #fillBytesRef()} for each term. Example:
 * <pre class="prettyprint">
 *   final TermToBytesRefAttribute termAtt = tokenStream.getAttribute(TermToBytesRefAttribute.class);
 *   final BytesRef bytes = termAtt.getBytesRef();
 *
 *   while (tokenStream.incrementToken() {
 *
 *     // you must call termAtt.fillBytesRef() before doing something with the bytes.
 *     // this encodes the term value (internally it might be a char[], etc) into the bytes.
 *     int hashCode = termAtt.fillBytesRef();
 *
 *     if (isInteresting(bytes)) {
 *     
 *       // because the bytes are reused by the attribute (like CharTermAttribute's char[] buffer),
 *       // you should make a copy if you need persistent access to the bytes, otherwise they will
 *       // be rewritten across calls to incrementToken()
 *
 *       doSomethingWith(new BytesRef(bytes));
 *     }
 *   }
 *   ...
 * </pre>
 * @lucene.experimental This is a very expert API, please use
 * {@link CharTermAttributeImpl} and its implementation of this method
 * for UTF-8 terms.
 */
public interface TermToBytesRefAttribute extends Attribute {

  /** 
   * Updates the bytes {@link #getBytesRef()} to contain this term's
   * final encoding.
   */
  public void fillBytesRef();
  
  /**
   * Retrieve this attribute's BytesRef. The bytes are updated 
   * from the current term when the consumer calls {@link #fillBytesRef()}.
   * @return this Attributes internal BytesRef.
   */
  public BytesRef getBytesRef();
}
