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
import java.util.List;

/**
 * A FilterDirectoryReader wraps another DirectoryReader, allowing implementations
 * to transform or extend it.
 *
 * Subclasses should implement doWrapDirectoryReader to return an instance of the
 * subclass.
 *
 * If the subclass wants to wrap the DirectoryReader's subreaders, it should also
 * implement a SubReaderWrapper subclass, and pass an instance to its super
 * constructor.
 */
public abstract class FilterDirectoryReader extends DirectoryReader {

  /** Get the wrapped instance by <code>reader</code> as long as this reader is
   *  an instance of {@link FilterDirectoryReader}.  */
  public static DirectoryReader unwrap(DirectoryReader reader) {
    while (reader instanceof FilterDirectoryReader) {
      reader = ((FilterDirectoryReader) reader).in;
    }
    return reader;
  }

  /**
   * Factory class passed to FilterDirectoryReader constructor that allows
   * subclasses to wrap the filtered DirectoryReader's subreaders.  You
   * can use this to, e.g., wrap the subreaders with specialised
   * FilterLeafReader implementations.
   */
  public static abstract class SubReaderWrapper {

    private LeafReader[] wrap(List<? extends LeafReader> readers) {
      LeafReader[] wrapped = new LeafReader[readers.size()];
      for (int i = 0; i < readers.size(); i++) {
        wrapped[i] = wrap(readers.get(i));
      }
      return wrapped;
    }

    /** Constructor */
    public SubReaderWrapper() {}

    /**
     * Wrap one of the parent DirectoryReader's subreaders
     * @param reader the subreader to wrap
     * @return a wrapped/filtered LeafReader
     */
    public abstract LeafReader wrap(LeafReader reader);

  }

  /** The filtered DirectoryReader */
  protected final DirectoryReader in;

  /**
   * Create a new FilterDirectoryReader that filters a passed in DirectoryReader,
   * using the supplied SubReaderWrapper to wrap its subreader.
   * @param in the DirectoryReader to filter
   * @param wrapper the SubReaderWrapper to use to wrap subreaders
   */
  public FilterDirectoryReader(DirectoryReader in, SubReaderWrapper wrapper) {
    super(in.directory(), wrapper.wrap(in.getSequentialSubReaders()));
    this.in = in;
  }

  /**
   * Called by the doOpenIfChanged() methods to return a new wrapped DirectoryReader.
   *
   * Implementations should just return an instantiation of themselves, wrapping the
   * passed in DirectoryReader.
   *
   * @param in the DirectoryReader to wrap
   * @return the wrapped DirectoryReader
   */
  protected abstract DirectoryReader doWrapDirectoryReader(DirectoryReader in);

  private final DirectoryReader wrapDirectoryReader(DirectoryReader in) {
    return in == null ? null : doWrapDirectoryReader(in);
  }

  @Override
  protected final DirectoryReader doOpenIfChanged() throws IOException {
    return wrapDirectoryReader(in.doOpenIfChanged());
  }

  @Override
  protected final DirectoryReader doOpenIfChanged(IndexCommit commit) throws IOException {
    return wrapDirectoryReader(in.doOpenIfChanged(commit));
  }

  @Override
  protected final DirectoryReader doOpenIfChanged(IndexWriter writer, boolean applyAllDeletes) throws IOException {
    return wrapDirectoryReader(in.doOpenIfChanged(writer, applyAllDeletes));
  }

  @Override
  public long getVersion() {
    return in.getVersion();
  }

  @Override
  public boolean isCurrent() throws IOException {
    return in.isCurrent();
  }

  @Override
  public IndexCommit getIndexCommit() throws IOException {
    return in.getIndexCommit();
  }

  @Override
  protected void doClose() throws IOException {
    in.doClose();
  }

  /** Returns the wrapped {@link DirectoryReader}. */
  public DirectoryReader getDelegate() {
    return in;
  }
}
