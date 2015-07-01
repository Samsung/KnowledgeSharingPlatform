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

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;

/**
 * Utility class to safely share {@link DirectoryReader} instances across
 * multiple threads, while periodically reopening. This class ensures each
 * reader is closed only once all threads have finished using it.
 * 
 * @see SearcherManager
 * 
 * @lucene.experimental
 */
public final class ReaderManager extends ReferenceManager<DirectoryReader> {

  /**
   * Creates and returns a new ReaderManager from the given
   * {@link IndexWriter}.
   * 
   * @param writer
   *          the IndexWriter to open the IndexReader from.
   * @param applyAllDeletes
   *          If <code>true</code>, all buffered deletes will be applied (made
   *          visible) in the {@link IndexSearcher} / {@link DirectoryReader}.
   *          If <code>false</code>, the deletes may or may not be applied, but
   *          remain buffered (in IndexWriter) so that they will be applied in
   *          the future. Applying deletes can be costly, so if your app can
   *          tolerate deleted documents being returned you might gain some
   *          performance by passing <code>false</code>. See
   *          {@link DirectoryReader#openIfChanged(DirectoryReader, IndexWriter, boolean)}.
   * 
   * @throws IOException If there is a low-level I/O error
   */
  public ReaderManager(IndexWriter writer, boolean applyAllDeletes) throws IOException {
    current = DirectoryReader.open(writer, applyAllDeletes);
  }
  
  /**
   * Creates and returns a new ReaderManager from the given {@link Directory}. 
   * @param dir the directory to open the DirectoryReader on.
   *        
   * @throws IOException If there is a low-level I/O error
   */
  public ReaderManager(Directory dir) throws IOException {
    current = DirectoryReader.open(dir);
  }

  /**
   * Creates and returns a new ReaderManager from the given
   * already-opened {@link DirectoryReader}, stealing
   * the incoming reference.
   *
   * @param reader the directoryReader to use for future reopens
   *        
   * @throws IOException If there is a low-level I/O error
   */
  public ReaderManager(DirectoryReader reader) throws IOException {
    current = reader;
  }

  @Override
  protected void decRef(DirectoryReader reference) throws IOException {
    reference.decRef();
  }
  
  @Override
  protected DirectoryReader refreshIfNeeded(DirectoryReader referenceToRefresh) throws IOException {
    return DirectoryReader.openIfChanged(referenceToRefresh);
  }
  
  @Override
  protected boolean tryIncRef(DirectoryReader reference) {
    return reference.tryIncRef();
  }

  @Override
  protected int getRefCount(DirectoryReader reference) {
    return reference.getRefCount();
  }

}
