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

import org.apache.lucene.index.DocumentsWriterPerThreadPool.ThreadState;

/**
 * Default {@link FlushPolicy} implementation that flushes new segments based on
 * RAM used and document count depending on the IndexWriter's
 * {@link IndexWriterConfig}. It also applies pending deletes based on the
 * number of buffered delete terms.
 * 
 * <ul>
 * <li>
 * {@link #onDelete(DocumentsWriterFlushControl, DocumentsWriterPerThreadPool.ThreadState)}
 * - applies pending delete operations based on the global number of buffered
 * delete terms iff {@link IndexWriterConfig#getMaxBufferedDeleteTerms()} is
 * enabled</li>
 * <li>
 * {@link #onInsert(DocumentsWriterFlushControl, DocumentsWriterPerThreadPool.ThreadState)}
 * - flushes either on the number of documents per
 * {@link DocumentsWriterPerThread} (
 * {@link DocumentsWriterPerThread#getNumDocsInRAM()}) or on the global active
 * memory consumption in the current indexing session iff
 * {@link IndexWriterConfig#getMaxBufferedDocs()} or
 * {@link IndexWriterConfig#getRAMBufferSizeMB()} is enabled respectively</li>
 * <li>
 * {@link #onUpdate(DocumentsWriterFlushControl, DocumentsWriterPerThreadPool.ThreadState)}
 * - calls
 * {@link #onInsert(DocumentsWriterFlushControl, DocumentsWriterPerThreadPool.ThreadState)}
 * and
 * {@link #onDelete(DocumentsWriterFlushControl, DocumentsWriterPerThreadPool.ThreadState)}
 * in order</li>
 * </ul>
 * All {@link IndexWriterConfig} settings are used to mark
 * {@link DocumentsWriterPerThread} as flush pending during indexing with
 * respect to their live updates.
 * <p>
 * If {@link IndexWriterConfig#setRAMBufferSizeMB(double)} is enabled, the
 * largest ram consuming {@link DocumentsWriterPerThread} will be marked as
 * pending iff the global active RAM consumption is {@code >=} the configured max RAM
 * buffer.
 */
class FlushByRamOrCountsPolicy extends FlushPolicy {

  @Override
  public void onDelete(DocumentsWriterFlushControl control, ThreadState state) {
    if (flushOnDeleteTerms()) {
      // Flush this state by num del terms
      final int maxBufferedDeleteTerms = indexWriterConfig
          .getMaxBufferedDeleteTerms();
      if (control.getNumGlobalTermDeletes() >= maxBufferedDeleteTerms) {
        control.setApplyAllDeletes();
      }
    }
    if ((flushOnRAM() &&
        control.getDeleteBytesUsed() > (1024*1024*indexWriterConfig.getRAMBufferSizeMB()))) {
      control.setApplyAllDeletes();
     if (infoStream.isEnabled("FP")) {
       infoStream.message("FP", "force apply deletes bytesUsed=" + control.getDeleteBytesUsed() + " vs ramBuffer=" + (1024*1024*indexWriterConfig.getRAMBufferSizeMB()));
     }
   }
  }

  @Override
  public void onInsert(DocumentsWriterFlushControl control, ThreadState state) {
    if (flushOnDocCount()
        && state.dwpt.getNumDocsInRAM() >= indexWriterConfig
            .getMaxBufferedDocs()) {
      // Flush this state by num docs
      control.setFlushPending(state);
    } else if (flushOnRAM()) {// flush by RAM
      final long limit = (long) (indexWriterConfig.getRAMBufferSizeMB() * 1024.d * 1024.d);
      final long totalRam = control.activeBytes() + control.getDeleteBytesUsed();
      if (totalRam >= limit) {
        if (infoStream.isEnabled("FP")) {
          infoStream.message("FP", "trigger flush: activeBytes=" + control.activeBytes() + " deleteBytes=" + control.getDeleteBytesUsed() + " vs limit=" + limit);
        }
        markLargestWriterPending(control, state, totalRam);
      }
    }
  }
  
  /**
   * Marks the most ram consuming active {@link DocumentsWriterPerThread} flush
   * pending
   */
  protected void markLargestWriterPending(DocumentsWriterFlushControl control,
      ThreadState perThreadState, final long currentBytesPerThread) {
    control.setFlushPending(findLargestNonPendingWriter(control, perThreadState));
  }
  
  /**
   * Returns <code>true</code> if this {@link FlushPolicy} flushes on
   * {@link IndexWriterConfig#getMaxBufferedDocs()}, otherwise
   * <code>false</code>.
   */
  protected boolean flushOnDocCount() {
    return indexWriterConfig.getMaxBufferedDocs() != IndexWriterConfig.DISABLE_AUTO_FLUSH;
  }

  /**
   * Returns <code>true</code> if this {@link FlushPolicy} flushes on
   * {@link IndexWriterConfig#getMaxBufferedDeleteTerms()}, otherwise
   * <code>false</code>.
   */
  protected boolean flushOnDeleteTerms() {
    return indexWriterConfig.getMaxBufferedDeleteTerms() != IndexWriterConfig.DISABLE_AUTO_FLUSH;
  }

  /**
   * Returns <code>true</code> if this {@link FlushPolicy} flushes on
   * {@link IndexWriterConfig#getRAMBufferSizeMB()}, otherwise
   * <code>false</code>.
   */
  protected boolean flushOnRAM() {
    return indexWriterConfig.getRAMBufferSizeMB() != IndexWriterConfig.DISABLE_AUTO_FLUSH;
  }
}
