package org.apache.lucene.analysis.util;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Simple {@link ResourceLoader} that opens resource files
 * from the local file system, optionally resolving against
 * a base directory.
 * 
 * <p>This loader wraps a delegate {@link ResourceLoader}
 * that is used to resolve all files, the current base directory
 * does not contain. {@link #newInstance} is always resolved
 * against the delegate, as a {@link ClassLoader} is needed.
 * 
 * <p>You can chain several {@code FilesystemResourceLoader}s
 * to allow lookup of files in more than one base directory.
 */
public final class FilesystemResourceLoader implements ResourceLoader {
  private final Path baseDirectory;
  private final ResourceLoader delegate;
  
  /**
   * Creates a resource loader that resolves resources against the given
   * base directory (may be {@code null} to refer to CWD).
   * Files not found in file system and class lookups are delegated to context
   * classloader.
   */
  public FilesystemResourceLoader(Path baseDirectory) {
    this(baseDirectory, new ClasspathResourceLoader());
  }

  /**
   * Creates a resource loader that resolves resources against the given
   * base directory (may be {@code null} to refer to CWD).
   * Files not found in file system and class lookups are delegated
   * to the given delegate {@link ResourceLoader}.
   */
  public FilesystemResourceLoader(Path baseDirectory, ResourceLoader delegate) {
    if (baseDirectory == null) {
      throw new NullPointerException();
    }
    if (!Files.isDirectory(baseDirectory))
      throw new IllegalArgumentException(baseDirectory + " is not a directory");
    if (delegate == null)
      throw new IllegalArgumentException("delegate ResourceLoader may not be null");
    this.baseDirectory = baseDirectory;
    this.delegate = delegate;
  }

  @Override
  public InputStream openResource(String resource) throws IOException {
    try {
      return Files.newInputStream(baseDirectory.resolve(resource));
    } catch (FileNotFoundException | NoSuchFileException fnfe) {
      return delegate.openResource(resource);
    }
  }

  @Override
  public <T> T newInstance(String cname, Class<T> expectedType) {
    return delegate.newInstance(cname, expectedType);
  }

  @Override
  public <T> Class<? extends T> findClass(String cname, Class<T> expectedType) {
    return delegate.findClass(cname, expectedType);
  }
}
