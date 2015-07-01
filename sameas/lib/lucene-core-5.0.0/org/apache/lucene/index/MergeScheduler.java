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

import java.io.Closeable;
import java.io.IOException;

import org.apache.lucene.util.InfoStream;

/** <p>Expert: {@link IndexWriter} uses an instance
 *  implementing this interface to execute the merges
 *  selected by a {@link MergePolicy}.  The default
 *  MergeScheduler is {@link ConcurrentMergeScheduler}.</p>
 *  <p>Implementers of sub-classes should make sure that {@link #clone()}
 *  returns an independent instance able to work with any {@link IndexWriter}
 *  instance.</p>
 * @lucene.experimental
*/
public abstract class MergeScheduler implements Closeable {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected MergeScheduler() {
  }

  /** Run the merges provided by {@link IndexWriter#getNextMerge()}.
   * @param writer the {@link IndexWriter} to obtain the merges from.
   * @param trigger the {@link MergeTrigger} that caused this merge to happen
   * @param newMergesFound <code>true</code> iff any new merges were found by the caller otherwise <code>false</code>
   * */
  public abstract void merge(IndexWriter writer, MergeTrigger trigger, boolean newMergesFound) throws IOException;

  /** Close this MergeScheduler. */
  @Override
  public abstract void close() throws IOException;

  /** For messages about merge scheduling */
  protected InfoStream infoStream;

  /** IndexWriter calls this on init. */
  final void setInfoStream(InfoStream infoStream) {
    this.infoStream = infoStream;
  }

  /**
   * Returns true if infoStream messages are enabled. This method is usually used in
   * conjunction with {@link #message(String)}:
   * 
   * <pre class="prettyprint">
   * if (verbose()) {
   *   message(&quot;your message&quot;);
   * }
   * </pre>
   */
  protected boolean verbose() {
    return infoStream != null && infoStream.isEnabled("MS");
  }
 
  /**
   * Outputs the given message - this method assumes {@link #verbose()} was
   * called and returned true.
   */
  protected void message(String message) {
    infoStream.message("MS", message);
  }
}
