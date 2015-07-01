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
import java.util.Objects;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;

/**
 * This exception is thrown when Lucene detects
 * an inconsistency in the index.
 */
public class CorruptIndexException extends IOException {
  /** Create exception with a message only */
  public CorruptIndexException(String message, DataInput input) {
    this(message, input, null);
  }

  /** Create exception with a message only */
  public CorruptIndexException(String message, DataOutput output) {
    this(message, output, null);
  }
  
  /** Create exception with message and root cause. */
  public CorruptIndexException(String message, DataInput input, Throwable cause) {
    this(message, Objects.toString(input), cause);
  }

  /** Create exception with message and root cause. */
  public CorruptIndexException(String message, DataOutput output, Throwable cause) {
    this(message, Objects.toString(output), cause);
  }
  
  /** Create exception with a message only */
  public CorruptIndexException(String message, String resourceDescription) {
    this(message, resourceDescription, null);
  }
  
  /** Create exception with message and root cause. */
  public CorruptIndexException(String message, String resourceDescription, Throwable cause) {
    super(Objects.toString(message) + " (resource=" + resourceDescription + ")", cause);
  }
}
