package org.apache.lucene.store;

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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/** A straightforward implementation of {@link FSDirectory}
 *  using java.io.RandomAccessFile.  However, this class has
 *  poor concurrent performance (multiple threads will
 *  bottleneck) as it synchronizes when multiple threads
 *  read from the same file.  It's usually better to use
 *  {@link NIOFSDirectory} or {@link MMapDirectory} instead. 
 *  <p>
 *  NOTE: Because this uses RandomAccessFile, it will generally
 *  not work with non-default filesystem providers. It is only
 *  provided for applications that relied on the fact that 
 *  RandomAccessFile's IO was not interruptible.
 */
public class RAFDirectory extends FSDirectory {
    
  /** Create a new RAFDirectory for the named location.
   *  The directory is created at the named location if it does not yet exist.
   *
   * @param path the path of the directory
   * @param lockFactory the lock factory to use
   * @throws IOException if there is a low-level I/O error
   */
  public RAFDirectory(Path path, LockFactory lockFactory) throws IOException {
    super(path, lockFactory);
    path.toFile(); // throw exception if we can't get a File
  }
  
  /** Create a new SimpleFSDirectory for the named location and {@link FSLockFactory#getDefault()}.
   *  The directory is created at the named location if it does not yet exist.
   *
   * @param path the path of the directory
   * @throws IOException if there is a low-level I/O error
   */
  public RAFDirectory(Path path) throws IOException {
    this(path, FSLockFactory.getDefault());
  }

  /** Creates an IndexInput for the file with the given name. */
  @Override
  public IndexInput openInput(String name, IOContext context) throws IOException {
    ensureOpen();
    final File path = directory.resolve(name).toFile();
    RandomAccessFile raf = new RandomAccessFile(path, "r");
    return new RAFIndexInput("SimpleFSIndexInput(path=\"" + path.getPath() + "\")", raf, context);
  }

  /**
   * Reads bytes with {@link RandomAccessFile#seek(long)} followed by
   * {@link RandomAccessFile#read(byte[], int, int)}.  
   */
  static final class RAFIndexInput extends BufferedIndexInput {
    /**
     * The maximum chunk size is 8192 bytes, because {@link RandomAccessFile} mallocs
     * a native buffer outside of stack if the read buffer size is larger.
     */
    private static final int CHUNK_SIZE = 8192;
    
    /** the file channel we will read from */
    protected final RandomAccessFile file;
    /** is this instance a clone and hence does not own the file to close it */
    boolean isClone = false;
    /** start offset: non-zero in the slice case */
    protected final long off;
    /** end offset (start+length) */
    protected final long end;
    
    public RAFIndexInput(String resourceDesc, RandomAccessFile file, IOContext context) throws IOException {
      super(resourceDesc, context);
      this.file = file; 
      this.off = 0L;
      this.end = file.length();
    }
    
    public RAFIndexInput(String resourceDesc, RandomAccessFile file, long off, long length, int bufferSize) {
      super(resourceDesc, bufferSize);
      this.file = file;
      this.off = off;
      this.end = off + length;
      this.isClone = true;
    }
    
    @Override
    public void close() throws IOException {
      if (!isClone) {
        file.close();
      }
    }
    
    @Override
    public RAFIndexInput clone() {
      RAFIndexInput clone = (RAFIndexInput)super.clone();
      clone.isClone = true;
      return clone;
    }
    
    @Override
    public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
      if (offset < 0 || length < 0 || offset + length > this.length()) {
        throw new IllegalArgumentException("slice() " + sliceDescription + " out of bounds: "  + this);
      }
      return new RAFIndexInput(sliceDescription, file, off + offset, length, getBufferSize());
    }

    @Override
    public final long length() {
      return end - off;
    }
  
    /** IndexInput methods */
    @Override
    protected void readInternal(byte[] b, int offset, int len)
         throws IOException {
      synchronized (file) {
        long position = off + getFilePointer();
        file.seek(position);
        int total = 0;

        if (position + len > end) {
          throw new EOFException("read past EOF: " + this);
        }

        try {
          while (total < len) {
            final int toRead = Math.min(CHUNK_SIZE, len - total);
            final int i = file.read(b, offset + total, toRead);
            if (i < 0) { // be defensive here, even though we checked before hand, something could have changed
             throw new EOFException("read past EOF: " + this + " off: " + offset + " len: " + len + " total: " + total + " chunkLen: " + toRead + " end: " + end);
            }
            assert i > 0 : "RandomAccessFile.read with non zero-length toRead must always read at least one byte";
            total += i;
          }
          assert total == len;
        } catch (IOException ioe) {
          throw new IOException(ioe.getMessage() + ": " + this, ioe);
        }
      }
    }
  
    @Override
    protected void seekInternal(long position) {
    }
    
    boolean isFDValid() throws IOException {
      return file.getFD().valid();
    }
  }
}
