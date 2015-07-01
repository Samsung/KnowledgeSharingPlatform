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

/**
 * <p>A FlushInfo provides information required for a FLUSH context.
 *  It is used as part of an {@link IOContext} in case of FLUSH context.</p>
 */


public class FlushInfo {
  
  public final int numDocs;
  
  public final long estimatedSegmentSize;
  
  /**
   * <p>Creates a new {@link FlushInfo} instance from
   * the values required for a FLUSH {@link IOContext} context.
   * 
   * These values are only estimates and are not the actual values.
   * 
   */
  
  public FlushInfo(int numDocs, long estimatedSegmentSize) {
    this.numDocs = numDocs;
    this.estimatedSegmentSize = estimatedSegmentSize;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (int) (estimatedSegmentSize ^ (estimatedSegmentSize >>> 32));
    result = prime * result + numDocs;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FlushInfo other = (FlushInfo) obj;
    if (estimatedSegmentSize != other.estimatedSegmentSize)
      return false;
    if (numDocs != other.numDocs)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FlushInfo [numDocs=" + numDocs + ", estimatedSegmentSize="
        + estimatedSegmentSize + "]";
  }

}
