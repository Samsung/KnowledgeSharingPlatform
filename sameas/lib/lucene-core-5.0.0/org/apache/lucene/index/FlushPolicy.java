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
import java.util.Iterator;

import org.apache.lucene.index.DocumentsWriterPerThreadPool.ThreadState;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.InfoStream;

/**
 * {@link FlushPolicy} controls when segments are flushed from a RAM resident
 * internal data-structure to the {@link IndexWriter}s {@link Directory}.
 * <p>
 * Segments are traditionally flushed by:
 * <ul>
 * <li>RAM consumption - configured via
 * {@link IndexWriterConfig#setRAMBufferSizeMB(double)}</li>
 * <li>Number of RAM resident documents - configured via
 * {@link IndexWriterConfig#setMaxBufferedDocs(int)}</li>
 * </ul>
 * The policy also applies pending delete operations (by term and/or query),
 * given the threshold set in
 * {@link IndexWriterConfig#setMaxBufferedDeleteTerms(int)}.
 * <p>
 * {@link IndexWriter} consults the provided {@link FlushPolicy} to control the
 * flushing process. The policy is informed for each added or updated document
 * as well as for each delete term. Based on the {@link FlushPolicy}, the
 * information provided via {@link ThreadState} and
 * {@link DocumentsWriterFlushControl}, the {@link FlushPolicy} decides if a
 * {@link DocumentsWriterPerThread} needs flushing and mark it as flush-pending
 * via {@link DocumentsWriterFlushControl#setFlushPending}, or if deletes need
 * to be applied.
 * 
 * @see ThreadState
 * @see DocumentsWriterFlushControl
 * @see DocumentsWriterPerThread
 * @see IndexWriterConfig#setFlushPolicy(FlushPolicy)
 */
abstract class FlushPolicy {
  protected LiveIndexWriterConfig indexWriterConfig;
  protected InfoStream infoStream;

  /**
   * Called for each delete term. If this is a delete triggered due to an update
   * the given {@link ThreadState} is non-null.
   * <p>
   * Note: This method is called synchronized on the given
   * {@link DocumentsWriterFlushControl} and it is guaranteed that the calling
   * thread holds the lock on the given {@link ThreadState}
   */
  public abstract void onDelete(DocumentsWriterFlushControl control,
      ThreadState state);

  /**
   * Called for each document update on the given {@link ThreadState}'s
   * {@link DocumentsWriterPerThread}.
   * <p>
   * Note: This method is called  synchronized on the given
   * {@link DocumentsWriterFlushControl} and it is guaranteed that the calling
   * thread holds the lock on the given {@link ThreadState}
   */
  public void onUpdate(DocumentsWriterFlushControl control, ThreadState state) {
    onInsert(control, state);
    onDelete(control, state);
  }

  /**
   * Called for each document addition on the given {@link ThreadState}s
   * {@link DocumentsWriterPerThread}.
   * <p>
   * Note: This method is synchronized by the given
   * {@link DocumentsWriterFlushControl} and it is guaranteed that the calling
   * thread holds the lock on the given {@link ThreadState}
   */
  public abstract void onInsert(DocumentsWriterFlushControl control,
      ThreadState state);

  /**
   * Called by DocumentsWriter to initialize the FlushPolicy
   */
  protected synchronized void init(LiveIndexWriterConfig indexWriterConfig) {
    this.indexWriterConfig = indexWriterConfig;
    infoStream = indexWriterConfig.getInfoStream();
  }

  /**
   * Returns the current most RAM consuming non-pending {@link ThreadState} with
   * at least one indexed document.
   * <p>
   * This method will never return <code>null</code>
   */
  protected ThreadState findLargestNonPendingWriter(
      DocumentsWriterFlushControl control, ThreadState perThreadState) {
    assert perThreadState.dwpt.getNumDocsInRAM() > 0;
    long maxRamSoFar = perThreadState.bytesUsed;
    // the dwpt which needs to be flushed eventually
    ThreadState maxRamUsingThreadState = perThreadState;
    assert !perThreadState.flushPending : "DWPT should have flushed";
    Iterator<ThreadState> activePerThreadsIterator = control.allActiveThreadStates();
    int count = 0;
    while (activePerThreadsIterator.hasNext()) {
      ThreadState next = activePerThreadsIterator.next();
      if (!next.flushPending) {
        final long nextRam = next.bytesUsed;
        if (nextRam > 0 && next.dwpt.getNumDocsInRAM() > 0) {
          if (infoStream.isEnabled("FP")) {
            infoStream.message("FP", "thread state has " + nextRam + " bytes; docInRAM=" + next.dwpt.getNumDocsInRAM());
          }
          count++;
          if (nextRam > maxRamSoFar) {
            maxRamSoFar = nextRam;
            maxRamUsingThreadState = next;
          }
        }
      }
    }
    if (infoStream.isEnabled("FP")) {
      infoStream.message("FP", count + " in-use non-flushing threads states");
    }
    assert assertMessage("set largest ram consuming thread pending on lower watermark");
    return maxRamUsingThreadState;
  }
  
  private boolean assertMessage(String s) {
    if (infoStream.isEnabled("FP")) {
      infoStream.message("FP", s);
    }
    return true;
  }
}
