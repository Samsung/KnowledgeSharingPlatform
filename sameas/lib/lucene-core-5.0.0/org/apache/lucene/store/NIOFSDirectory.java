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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException; // javadoc @link
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future; // javadoc

/**
 * An {@link FSDirectory} implementation that uses java.nio's FileChannel's
 * positional read, which allows multiple threads to read from the same file
 * without synchronizing.
 * <p>
 * This class only uses FileChannel when reading; writing is achieved with
 * {@link FSDirectory.FSIndexOutput}.
 * <p>
 * <b>NOTE</b>: NIOFSDirectory is not recommended on Windows because of a bug in
 * how FileChannel.read is implemented in Sun's JRE. Inside of the
 * implementation the position is apparently synchronized. See <a
 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6265734">here</a>
 * for details.
 * </p>
 * <p>
 * <b>NOTE:</b> Accessing this class either directly or
 * indirectly from a thread while it's interrupted can close the
 * underlying file descriptor immediately if at the same time the thread is
 * blocked on IO. The file descriptor will remain closed and subsequent access
 * to {@link NIOFSDirectory} will throw a {@link ClosedChannelException}. If
 * your application uses either {@link Thread#interrupt()} or
 * {@link Future#cancel(boolean)} you should use the legacy {@code RAFDirectory}
 * from the Lucene {@code misc} module in favor of {@link NIOFSDirectory}.
 * </p>
 */
public class NIOFSDirectory extends FSDirectory {

  /** Create a new NIOFSDirectory for the named location.
   *  The directory is created at the named location if it does not yet exist.
   * 
   * @param path the path of the directory
   * @param lockFactory the lock factory to use
   * @throws IOException if there is a low-level I/O error
   */
  public NIOFSDirectory(Path path, LockFactory lockFactory) throws IOException {
    super(path, lockFactory);
  }

  /** Create a new NIOFSDirectory for the named location and {@link FSLockFactory#getDefault()}.
   *  The directory is created at the named location if it does not yet exist.
   *
   * @param path the path of the directory
   * @throws IOException if there is a low-level I/O error
   */
  public NIOFSDirectory(Path path) throws IOException {
    this(path, FSLockFactory.getDefault());
  }

  /** Creates an IndexInput for the file with the given name. */
  @Override
  public IndexInput openInput(String name, IOContext context) throws IOException {
    ensureOpen();
    Path path = getDirectory().resolve(name);
    FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
    return new NIOFSIndexInput("NIOFSIndexInput(path=\"" + path + "\")", fc, context);
  }
  
  /**
   * Reads bytes with {@link FileChannel#read(ByteBuffer, long)}
   */
  static final class NIOFSIndexInput extends BufferedIndexInput {
    /**
     * The maximum chunk size for reads of 16384 bytes.
     */
    private static final int CHUNK_SIZE = 16384;
    
    /** the file channel we will read from */
    protected final FileChannel channel;
    /** is this instance a clone and hence does not own the file to close it */
    boolean isClone = false;
    /** start offset: non-zero in the slice case */
    protected final long off;
    /** end offset (start+length) */
    protected final long end;
    
    private ByteBuffer byteBuf; // wraps the buffer for NIO

    public NIOFSIndexInput(String resourceDesc, FileChannel fc, IOContext context) throws IOException {
      super(resourceDesc, context);
      this.channel = fc; 
      this.off = 0L;
      this.end = fc.size();
    }
    
    public NIOFSIndexInput(String resourceDesc, FileChannel fc, long off, long length, int bufferSize) {
      super(resourceDesc, bufferSize);
      this.channel = fc;
      this.off = off;
      this.end = off + length;
      this.isClone = true;
    }
    
    @Override
    public void close() throws IOException {
      if (!isClone) {
        channel.close();
      }
    }
    
    @Override
    public NIOFSIndexInput clone() {
      NIOFSIndexInput clone = (NIOFSIndexInput)super.clone();
      clone.isClone = true;
      return clone;
    }
    
    @Override
    public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
      if (offset < 0 || length < 0 || offset + length > this.length()) {
        throw new IllegalArgumentException("slice() " + sliceDescription + " out of bounds: "  + this);
      }
      return new NIOFSIndexInput(sliceDescription, channel, off + offset, length, getBufferSize());
    }

    @Override
    public final long length() {
      return end - off;
    }

    @Override
    protected void newBuffer(byte[] newBuffer) {
      super.newBuffer(newBuffer);
      byteBuf = ByteBuffer.wrap(newBuffer);
    }

    @Override
    protected void readInternal(byte[] b, int offset, int len) throws IOException {
      final ByteBuffer bb;

      // Determine the ByteBuffer we should use
      if (b == buffer) {
        // Use our own pre-wrapped byteBuf:
        assert byteBuf != null;
        bb = byteBuf;
        byteBuf.clear().position(offset);
      } else {
        bb = ByteBuffer.wrap(b, offset, len);
      }

      long pos = getFilePointer() + off;
      
      if (pos + len > end) {
        throw new EOFException("read past EOF: " + this);
      }

      try {
        int readLength = len;
        while (readLength > 0) {
          final int toRead = Math.min(CHUNK_SIZE, readLength);
          bb.limit(bb.position() + toRead);
          assert bb.remaining() == toRead;
          final int i = channel.read(bb, pos);
          if (i < 0) { // be defensive here, even though we checked before hand, something could have changed
            throw new EOFException("read past EOF: " + this + " off: " + offset + " len: " + len + " pos: " + pos + " chunkLen: " + toRead + " end: " + end);
          }
          assert i > 0 : "FileChannel.read with non zero-length bb.remaining() must always read at least one byte (FileChannel is in blocking mode, see spec of ReadableByteChannel)";
          pos += i;
          readLength -= i;
        }
        assert readLength == 0;
      } catch (IOException ioe) {
        throw new IOException(ioe.getMessage() + ": " + this, ioe);
      }
    }

    @Override
    protected void seekInternal(long pos) throws IOException {}
  }
}
