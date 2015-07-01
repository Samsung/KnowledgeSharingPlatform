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

/** Default implementation of {@link PositionLengthAttribute}. */
public class PositionLengthAttributeImpl extends AttributeImpl implements PositionLengthAttribute, Cloneable {
  private int positionLength = 1;
  
  /** Initializes this attribute with position length of 1. */
  public PositionLengthAttributeImpl() {}
  
  @Override
  public void setPositionLength(int positionLength) {
    if (positionLength < 1) {
      throw new IllegalArgumentException
        ("Position length must be 1 or greater: got " + positionLength);
    }
    this.positionLength = positionLength;
  }

  @Override
  public int getPositionLength() {
    return positionLength;
  }

  @Override
  public void clear() {
    this.positionLength = 1;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    
    if (other instanceof PositionLengthAttributeImpl) {
      PositionLengthAttributeImpl _other = (PositionLengthAttributeImpl) other;
      return positionLength ==  _other.positionLength;
    }
 
    return false;
  }

  @Override
  public int hashCode() {
    return positionLength;
  }
  
  @Override
  public void copyTo(AttributeImpl target) {
    PositionLengthAttribute t = (PositionLengthAttribute) target;
    t.setPositionLength(positionLength);
  }  
}
