package org.apache.lucene.search;

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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

/**
 * Utility class to safely share {@link IndexSearcher} instances across multiple
 * threads, while periodically reopening. This class ensures each searcher is
 * closed only once all threads have finished using it.
 * 
 * <p>
 * Use {@link #acquire} to obtain the current searcher, and {@link #release} to
 * release it, like this:
 * 
 * <pre class="prettyprint">
 * IndexSearcher s = manager.acquire();
 * try {
 *   // Do searching, doc retrieval, etc. with s
 * } finally {
 *   manager.release(s);
 * }
 * // Do not use s after this!
 * s = null;
 * </pre>
 * 
 * <p>
 * In addition you should periodically call {@link #maybeRefresh}. While it's
 * possible to call this just before running each query, this is discouraged
 * since it penalizes the unlucky queries that need to refresh. It's better to use
 * a separate background thread, that periodically calls {@link #maybeRefresh}. Finally,
 * be sure to call {@link #close} once you are done.
 * 
 * @see SearcherFactory
 * 
 * @lucene.experimental
 */
public final class SearcherManager extends ReferenceManager<IndexSearcher> {

  private final SearcherFactory searcherFactory;

  /**
   * Creates and returns a new SearcherManager from the given
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
   * @param searcherFactory
   *          An optional {@link SearcherFactory}. Pass <code>null</code> if you
   *          don't require the searcher to be warmed before going live or other
   *          custom behavior.
   * 
   * @throws IOException if there is a low-level I/O error
   */
  public SearcherManager(IndexWriter writer, boolean applyAllDeletes, SearcherFactory searcherFactory) throws IOException {
    if (searcherFactory == null) {
      searcherFactory = new SearcherFactory();
    }
    this.searcherFactory = searcherFactory;
    current = getSearcher(searcherFactory, DirectoryReader.open(writer, applyAllDeletes));
  }
  
  /**
   * Creates and returns a new SearcherManager from the given {@link Directory}. 
   * @param dir the directory to open the DirectoryReader on.
   * @param searcherFactory An optional {@link SearcherFactory}. Pass
   *        <code>null</code> if you don't require the searcher to be warmed
   *        before going live or other custom behavior.
   *        
   * @throws IOException if there is a low-level I/O error
   */
  public SearcherManager(Directory dir, SearcherFactory searcherFactory) throws IOException {
    if (searcherFactory == null) {
      searcherFactory = new SearcherFactory();
    }
    this.searcherFactory = searcherFactory;
    current = getSearcher(searcherFactory, DirectoryReader.open(dir));
  }

  /**
   * Creates and returns a new SearcherManager from an existing {@link DirectoryReader}.  Note that
   * this steals the incoming reference.
   *
   * @param reader the DirectoryReader.
   * @param searcherFactory An optional {@link SearcherFactory}. Pass
   *        <code>null</code> if you don't require the searcher to be warmed
   *        before going live or other custom behavior.
   *        
   * @throws IOException if there is a low-level I/O error
   */
  public SearcherManager(DirectoryReader reader, SearcherFactory searcherFactory) throws IOException {
    if (searcherFactory == null) {
      searcherFactory = new SearcherFactory();
    }
    this.searcherFactory = searcherFactory;
    this.current = getSearcher(searcherFactory, reader);
  }

  @Override
  protected void decRef(IndexSearcher reference) throws IOException {
    reference.getIndexReader().decRef();
  }
  
  @Override
  protected IndexSearcher refreshIfNeeded(IndexSearcher referenceToRefresh) throws IOException {
    final IndexReader r = referenceToRefresh.getIndexReader();
    assert r instanceof DirectoryReader: "searcher's IndexReader should be a DirectoryReader, but got " + r;
    final IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) r);
    if (newReader == null) {
      return null;
    } else {
      return getSearcher(searcherFactory, newReader);
    }
  }
  
  @Override
  protected boolean tryIncRef(IndexSearcher reference) {
    return reference.getIndexReader().tryIncRef();
  }

  @Override
  protected int getRefCount(IndexSearcher reference) {
    return reference.getIndexReader().getRefCount();
  }

  /**
   * Returns <code>true</code> if no changes have occured since this searcher
   * ie. reader was opened, otherwise <code>false</code>.
   * @see DirectoryReader#isCurrent() 
   */
  public boolean isSearcherCurrent() throws IOException {
    final IndexSearcher searcher = acquire();
    try {
      final IndexReader r = searcher.getIndexReader();
      assert r instanceof DirectoryReader: "searcher's IndexReader should be a DirectoryReader, but got " + r;
      return ((DirectoryReader) r).isCurrent();
    } finally {
      release(searcher);
    }
  }

  /** Expert: creates a searcher from the provided {@link
   *  IndexReader} using the provided {@link
   *  SearcherFactory}.  NOTE: this decRefs incoming reader
   * on throwing an exception. */
  public static IndexSearcher getSearcher(SearcherFactory searcherFactory, IndexReader reader) throws IOException {
    boolean success = false;
    final IndexSearcher searcher;
    try {
      searcher = searcherFactory.newSearcher(reader);
      if (searcher.getIndexReader() != reader) {
        throw new IllegalStateException("SearcherFactory must wrap exactly the provided reader (got " + searcher.getIndexReader() + " but expected " + reader + ")");
      }
      success = true;
    } finally {
      if (!success) {
        reader.decRef();
      }
    }
    return searcher;
  }
}
