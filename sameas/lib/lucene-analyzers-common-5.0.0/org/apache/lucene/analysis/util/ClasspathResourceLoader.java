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

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple {@link ResourceLoader} that uses {@link ClassLoader#getResourceAsStream(String)}
 * and {@link Class#forName(String,boolean,ClassLoader)} to open resources and
 * classes, respectively.
 */
public final class ClasspathResourceLoader implements ResourceLoader {
  private final Class<?> clazz;
  private final ClassLoader loader;
  
  /**
   * Creates an instance using the context classloader to load Resources and classes.
   * Resource paths must be absolute.
   */
  public ClasspathResourceLoader() {
    this(Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an instance using the given classloader to load Resources and classes.
   * Resource paths must be absolute.
   */
  public ClasspathResourceLoader(ClassLoader loader) {
    this(null, loader);
  }

  /**
   * Creates an instance using the context classloader to load Resources and classes
   * Resources are resolved relative to the given class, if path is not absolute.
   */
  public ClasspathResourceLoader(Class<?> clazz) {
    this(clazz, clazz.getClassLoader());
  }

  private ClasspathResourceLoader(Class<?> clazz, ClassLoader loader) {
    this.clazz = clazz;
    this.loader = loader;
  }

  @Override
  public InputStream openResource(String resource) throws IOException {
    final InputStream stream = (clazz != null) ?
      clazz.getResourceAsStream(resource) :
      loader.getResourceAsStream(resource);
    if (stream == null)
      throw new IOException("Resource not found: " + resource);
    return stream;
  }
  
  @Override
  public <T> Class<? extends T> findClass(String cname, Class<T> expectedType) {
    try {
      return Class.forName(cname, true, loader).asSubclass(expectedType);
    } catch (Exception e) {
      throw new RuntimeException("Cannot load class: " + cname, e);
    }
  }

  @Override
  public <T> T newInstance(String cname, Class<T> expectedType) {
    Class<? extends T> clazz = findClass(cname, expectedType);
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Cannot create instance: " + cname, e);
    }
  }
}
