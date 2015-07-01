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
import java.io.FileDescriptor;
import java.nio.ByteBuffer;

/**
 * Provides JNI access to native methods such as madvise() for
 * {@link NativeUnixDirectory}
 */
public final class NativePosixUtil {
  public final static int NORMAL = 0;
  public final static int SEQUENTIAL = 1;
  public final static int RANDOM = 2;
  public final static int WILLNEED = 3;
  public final static int DONTNEED = 4;
  public final static int NOREUSE = 5;

  static {
    System.loadLibrary("NativePosixUtil");
  }

  private static native int posix_fadvise(FileDescriptor fd, long offset, long len, int advise) throws IOException;
  public static native int posix_madvise(ByteBuffer buf, int advise) throws IOException;
  public static native int madvise(ByteBuffer buf, int advise) throws IOException;
  public static native FileDescriptor open_direct(String filename, boolean read) throws IOException;
  public static native long pread(FileDescriptor fd, long pos, ByteBuffer byteBuf) throws IOException;

  public static void advise(FileDescriptor fd, long offset, long len, int advise) throws IOException {
    final int code = posix_fadvise(fd, offset, len, advise);
    if (code != 0) {
      throw new RuntimeException("posix_fadvise failed code=" + code);
    }
  }
}
    
