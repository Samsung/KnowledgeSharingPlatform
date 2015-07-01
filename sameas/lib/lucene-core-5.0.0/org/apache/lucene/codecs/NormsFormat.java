package org.apache.lucene.codecs;

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

import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;

/**
 * Encodes/decodes per-document score normalization values.
 */
public abstract class NormsFormat {
  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected NormsFormat() {
  }

  /** Returns a {@link NormsConsumer} to write norms to the
   *  index. */
  public abstract NormsConsumer normsConsumer(SegmentWriteState state) throws IOException;

  /** 
   * Returns a {@link NormsProducer} to read norms from the index. 
   * <p>
   * NOTE: by the time this call returns, it must hold open any files it will 
   * need to use; else, those files may be deleted. Additionally, required files 
   * may be deleted during the execution of this call before there is a chance 
   * to open them. Under these circumstances an IOException should be thrown by 
   * the implementation. IOExceptions are expected and will automatically cause 
   * a retry of the segment opening logic with the newly revised segments.
   */
  public abstract NormsProducer normsProducer(SegmentReadState state) throws IOException;
}
