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

import java.util.List;

import org.apache.lucene.store.*;

/**
 Instances of this reader type can only
 be used to get stored fields from the underlying LeafReaders,
 but it is not possible to directly retrieve postings. To do that, get
 the {@link LeafReaderContext} for all sub-readers via {@link #leaves()}.
 Alternatively, you can mimic an {@link LeafReader} (with a serious slowdown),
 by wrapping composite readers with {@link SlowCompositeReaderWrapper}.
 
 <p>IndexReader instances for indexes on disk are usually constructed
 with a call to one of the static <code>DirectoryReader.open()</code> methods,
 e.g. {@link DirectoryReader#open(Directory)}. {@link DirectoryReader} implements
 the {@code CompositeReader} interface, it is not possible to directly get postings.
 <p> Concrete subclasses of IndexReader are usually constructed with a call to
 one of the static <code>open()</code> methods, e.g. {@link
 DirectoryReader#open(Directory)}.

 <p> For efficiency, in this API documents are often referred to via
 <i>document numbers</i>, non-negative integers which each name a unique
 document in the index.  These document numbers are ephemeral -- they may change
 as documents are added to and deleted from an index.  Clients should thus not
 rely on a given document having the same number between sessions.

 <p>
 <a name="thread-safety"></a><p><b>NOTE</b>: {@link
 IndexReader} instances are completely thread
 safe, meaning multiple threads can call any of its methods,
 concurrently.  If your application requires external
 synchronization, you should <b>not</b> synchronize on the
 <code>IndexReader</code> instance; use your own
 (non-Lucene) objects instead.
*/
public abstract class CompositeReader extends IndexReader {

  private volatile CompositeReaderContext readerContext = null; // lazy init

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected CompositeReader() { 
    super();
  }
  
  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    // walk up through class hierarchy to get a non-empty simple name (anonymous classes have no name):
    for (Class<?> clazz = getClass(); clazz != null; clazz = clazz.getSuperclass()) {
      if (!clazz.isAnonymousClass()) {
        buffer.append(clazz.getSimpleName());
        break;
      }
    }
    buffer.append('(');
    final List<? extends IndexReader> subReaders = getSequentialSubReaders();
    assert subReaders != null;
    if (!subReaders.isEmpty()) {
      buffer.append(subReaders.get(0));
      for (int i = 1, c = subReaders.size(); i < c; ++i) {
        buffer.append(" ").append(subReaders.get(i));
      }
    }
    buffer.append(')');
    return buffer.toString();
  }
  
  /** Expert: returns the sequential sub readers that this
   *  reader is logically composed of. This method may not
   *  return {@code null}.
   *  
   *  <p><b>NOTE:</b> In contrast to previous Lucene versions this method
   *  is no longer public, code that wants to get all {@link LeafReader}s
   *  this composite is composed of should use {@link IndexReader#leaves()}.
   * @see IndexReader#leaves()
   */
  protected abstract List<? extends IndexReader> getSequentialSubReaders();

  @Override
  public final CompositeReaderContext getContext() {
    ensureOpen();
    // lazy init without thread safety for perf reasons: Building the readerContext twice does not hurt!
    if (readerContext == null) {
      assert getSequentialSubReaders() != null;
      readerContext = CompositeReaderContext.create(this);
    }
    return readerContext;
  }
}
