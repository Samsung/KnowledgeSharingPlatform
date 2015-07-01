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

import org.apache.lucene.util.AttributeImpl;

/** Default implementation of {@link OffsetAttribute}. */
public class OffsetAttributeImpl extends AttributeImpl implements OffsetAttribute, Cloneable {
  private int startOffset;
  private int endOffset;
  
  /** Initialize this attribute with startOffset and endOffset of 0. */
  public OffsetAttributeImpl() {}

  @Override
  public int startOffset() {
    return startOffset;
  }

  @Override
  public void setOffset(int startOffset, int endOffset) {

    // TODO: we could assert that this is set-once, ie,
    // current values are -1?  Very few token filters should
    // change offsets once set by the tokenizer... and
    // tokenizer should call clearAtts before re-using
    // OffsetAtt

    if (startOffset < 0 || endOffset < startOffset) {
      throw new IllegalArgumentException("startOffset must be non-negative, and endOffset must be >= startOffset, "
          + "startOffset=" + startOffset + ",endOffset=" + endOffset);
    }

    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }
  
  @Override
  public int endOffset() {
    return endOffset;
  }


  @Override
  public void clear() {
    // TODO: we could use -1 as default here?  Then we can
    // assert in setOffset...
    startOffset = 0;
    endOffset = 0;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    
    if (other instanceof OffsetAttributeImpl) {
      OffsetAttributeImpl o = (OffsetAttributeImpl) other;
      return o.startOffset == startOffset && o.endOffset == endOffset;
    }
    
    return false;
  }

  @Override
  public int hashCode() {
    int code = startOffset;
    code = code * 31 + endOffset;
    return code;
  } 
  
  @Override
  public void copyTo(AttributeImpl target) {
    OffsetAttribute t = (OffsetAttribute) target;
    t.setOffset(startOffset, endOffset);
  }  
}
