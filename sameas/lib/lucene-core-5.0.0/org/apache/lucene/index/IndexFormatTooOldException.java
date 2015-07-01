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

package org.apache.lucene.index;

import java.io.IOException;
import java.util.Objects;

import org.apache.lucene.store.DataInput;

/**
 * This exception is thrown when Lucene detects
 * an index that is too old for this Lucene version
 */
public class IndexFormatTooOldException extends IOException {

  /** Creates an {@code IndexFormatTooOldException}.
   *
   *  @param resourceDesc describes the file that was too old
   *  @param version the version of the file that was too old
   * 
   * @lucene.internal */
  public IndexFormatTooOldException(String resourceDesc, String version) {
    super("Format version is not supported (resource " + resourceDesc + "): " +
        version + ". This version of Lucene only supports indexes created with release 4.0 and later.");
  }

  /** Creates an {@code IndexFormatTooOldException}.
   *
   *  @param in the open file that's too old
   *  @param version the version of the file that was too old
   *
   * @lucene.internal */
  public IndexFormatTooOldException(DataInput in, String version) {
    this(Objects.toString(in), version);
  }

  /** Creates an {@code IndexFormatTooOldException}.
   *
   *  @param resourceDesc describes the file that was too old
   *  @param version the version of the file that was too old
   *  @param minVersion the minimum version accepted
   *  @param maxVersion the maxium version accepted
   * 
   * @lucene.internal */
  public IndexFormatTooOldException(String resourceDesc, int version, int minVersion, int maxVersion) {
    super("Format version is not supported (resource " + resourceDesc + "): " +
        version + " (needs to be between " + minVersion + " and " + maxVersion +
        "). This version of Lucene only supports indexes created with release 4.0 and later.");
  }

  /** Creates an {@code IndexFormatTooOldException}.
   *
   *  @param in the open file that's too old
   *  @param version the version of the file that was too old
   *  @param minVersion the minimum version accepted
   *  @param maxVersion the maxium version accepted
   *
   * @lucene.internal */
  public IndexFormatTooOldException(DataInput in, int version, int minVersion, int maxVersion) {
    this(Objects.toString(in), version, minVersion, maxVersion);
  }
}
