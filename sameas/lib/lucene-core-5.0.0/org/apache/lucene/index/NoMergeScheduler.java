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

/**
 * A {@link MergeScheduler} which never executes any merges. It is also a
 * singleton and can be accessed through {@link NoMergeScheduler#INSTANCE}. Use
 * it if you want to prevent an {@link IndexWriter} from ever executing merges,
 * regardless of the {@link MergePolicy} used. Note that you can achieve the
 * same thing by using {@link NoMergePolicy}, however with
 * {@link NoMergeScheduler} you also ensure that no unnecessary code of any
 * {@link MergeScheduler} implementation is ever executed. Hence it is
 * recommended to use both if you want to disable merges from ever happening.
 */
public final class NoMergeScheduler extends MergeScheduler {

  /** The single instance of {@link NoMergeScheduler} */
  public static final MergeScheduler INSTANCE = new NoMergeScheduler();

  private NoMergeScheduler() {
    // prevent instantiation
  }

  @Override
  public void close() {}

  @Override
  public void merge(IndexWriter writer, MergeTrigger trigger, boolean newMergesFound) {}

  @Override
  public MergeScheduler clone() {
    return this;
  }
}
