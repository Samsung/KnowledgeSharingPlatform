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
 * <p>A MergeInfo provides information required for a MERGE context.
 *  It is used as part of an {@link IOContext} in case of MERGE context.</p>
 */

public class MergeInfo {
  
  public final int totalDocCount;
  
  public final long estimatedMergeBytes;
  
  public final boolean isExternal;
  
  public final int mergeMaxNumSegments;
  

  /**
   * <p>Creates a new {@link MergeInfo} instance from
   * the values required for a MERGE {@link IOContext} context.
   * 
   * These values are only estimates and are not the actual values.
   * 
   */

  public MergeInfo(int totalDocCount, long estimatedMergeBytes, boolean isExternal, int mergeMaxNumSegments) {
    this.totalDocCount = totalDocCount;
    this.estimatedMergeBytes = estimatedMergeBytes;
    this.isExternal = isExternal;
    this.mergeMaxNumSegments = mergeMaxNumSegments;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (int) (estimatedMergeBytes ^ (estimatedMergeBytes >>> 32));
    result = prime * result + (isExternal ? 1231 : 1237);
    result = prime * result + mergeMaxNumSegments;
    result = prime * result + totalDocCount;
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
    MergeInfo other = (MergeInfo) obj;
    if (estimatedMergeBytes != other.estimatedMergeBytes)
      return false;
    if (isExternal != other.isExternal)
      return false;
    if (mergeMaxNumSegments != other.mergeMaxNumSegments)
      return false;
    if (totalDocCount != other.totalDocCount)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "MergeInfo [totalDocCount=" + totalDocCount
        + ", estimatedMergeBytes=" + estimatedMergeBytes + ", isExternal="
        + isExternal + ", mergeMaxNumSegments=" + mergeMaxNumSegments + "]";
  }
}
