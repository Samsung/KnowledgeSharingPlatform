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

import java.io.IOException;
import java.io.EOFException;
import java.nio.file.Path;

import org.apache.lucene.store.Directory; // javadoc

/**
 * Native {@link Directory} implementation for Microsoft Windows.
 * <p>
 * Steps:
 * <ol> 
 *   <li>Compile the source code to create WindowsDirectory.dll:
 *       <blockquote>
 * c:\mingw\bin\g++ -Wall -D_JNI_IMPLEMENTATION_ -Wl,--kill-at 
 * -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -static-libgcc 
 * -static-libstdc++ -shared WindowsDirectory.cpp -o WindowsDirectory.dll
 *       </blockquote> 
 *       For 64-bit JREs, use mingw64, with the -m64 option. 
 *   <li>Put WindowsDirectory.dll into some directory in your windows PATH
 *   <li>Open indexes with WindowsDirectory and use it.
 * </ol>
 * </p>
 * @lucene.experimental
 */
public class WindowsDirectory extends FSDirectory {
  private static final int DEFAULT_BUFFERSIZE = 4096; /* default pgsize on ia32/amd64 */
  
  static {
    System.loadLibrary("WindowsDirectory");
  }
  
  /** Create a new WindowsDirectory for the named location.
   * 
   * @param path the path of the directory
   * @param lockFactory the lock factory to use
   * @throws IOException If there is a low-level I/O error
   */
  public WindowsDirectory(Path path, LockFactory lockFactory) throws IOException {
    super(path, lockFactory);
  }

  /** Create a new WindowsDirectory for the named location and {@link FSLockFactory#getDefault()}.
   *
   * @param path the path of the directory
   * @throws IOException If there is a low-level I/O error
   */
  public WindowsDirectory(Path path) throws IOException {
    this(path, FSLockFactory.getDefault());
  }

  @Override
  public IndexInput openInput(String name, IOContext context) throws IOException {
    ensureOpen();
    return new WindowsIndexInput(getDirectory().resolve(name), Math.max(BufferedIndexInput.bufferSize(context), DEFAULT_BUFFERSIZE));
  }
  
  static class WindowsIndexInput extends BufferedIndexInput {
    private final long fd;
    private final long length;
    boolean isClone;
    boolean isOpen;
    
    public WindowsIndexInput(Path file, int bufferSize) throws IOException {
      super("WindowsIndexInput(path=\"" + file + "\")", bufferSize);
      fd = WindowsDirectory.open(file.toString());
      length = WindowsDirectory.length(fd);
      isOpen = true;
    }
    
    @Override
    protected void readInternal(byte[] b, int offset, int length) throws IOException {
      int bytesRead;
      try {
        bytesRead = WindowsDirectory.read(fd, b, offset, length, getFilePointer());
      } catch (IOException ioe) {
        throw new IOException(ioe.getMessage() + ": " + this, ioe);
      }

      if (bytesRead != length) {
        throw new EOFException("read past EOF: " + this);
      }
    }

    @Override
    protected void seekInternal(long pos) throws IOException {
    }

    @Override
    public synchronized void close() throws IOException {
      // NOTE: we synchronize and track "isOpen" because Lucene sometimes closes IIs twice!
      if (!isClone && isOpen) {
        WindowsDirectory.close(fd);
        isOpen = false;
      }
    }

    @Override
    public long length() {
      return length;
    }
    
    @Override
    public WindowsIndexInput clone() {
      WindowsIndexInput clone = (WindowsIndexInput)super.clone();
      clone.isClone = true;
      return clone;
    }
  }
  
  /** Opens a handle to a file. */
  private static native long open(String filename) throws IOException;
  
  /** Reads data from a file at pos into bytes */
  private static native int read(long fd, byte bytes[], int offset, int length, long pos) throws IOException;
  
  /** Closes a handle to a file */
  private static native void close(long fd) throws IOException;
  
  /** Returns the length of a file */
  private static native long length(long fd) throws IOException;
}
