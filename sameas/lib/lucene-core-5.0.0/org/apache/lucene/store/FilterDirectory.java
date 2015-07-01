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
import java.util.Collection;

/** Directory implementation that delegates calls to another directory.
 *  This class can be used to add limitations on top of an existing
 *  {@link Directory} implementation such as
 *  {@link NRTCachingDirectory} or to add additional
 *  sanity checks for tests. However, if you plan to write your own
 *  {@link Directory} implementation, you should consider extending directly
 *  {@link Directory} or {@link BaseDirectory} rather than try to reuse
 *  functionality of existing {@link Directory}s by extending this class.
 *  @lucene.internal */
public class FilterDirectory extends Directory {

  /** Get the wrapped instance by <code>dir</code> as long as this reader is
   *  an instance of {@link FilterDirectory}.  */
  public static Directory unwrap(Directory dir) {
    while (dir instanceof FilterDirectory) {
      dir = ((FilterDirectory) dir).in;
    }
    return dir;
  }

  protected final Directory in;

  /** Sole constructor, typically called from sub-classes. */
  protected FilterDirectory(Directory in) {
    this.in = in;
  }

  /** Return the wrapped {@link Directory}. */
  public final Directory getDelegate() {
    return in;
  }

  @Override
  public String[] listAll() throws IOException {
    return in.listAll();
  }

  @Override
  public void deleteFile(String name) throws IOException {
    in.deleteFile(name);
  }

  @Override
  public long fileLength(String name) throws IOException {
    return in.fileLength(name);
  }

  @Override
  public IndexOutput createOutput(String name, IOContext context)
      throws IOException {
    return in.createOutput(name, context);
  }

  @Override
  public void sync(Collection<String> names) throws IOException {
    in.sync(names);
  }

  @Override
  public void renameFile(String source, String dest) throws IOException {
    in.renameFile(source, dest);
  }

  @Override
  public IndexInput openInput(String name, IOContext context)
      throws IOException {
    return in.openInput(name, context);
  }

  @Override
  public Lock makeLock(String name) {
    return in.makeLock(name);
  }

  @Override
  public void close() throws IOException {
    in.close();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + in.toString() + ")";
  }

}
