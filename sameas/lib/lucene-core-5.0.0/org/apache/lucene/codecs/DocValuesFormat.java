package org.apache.lucene.codecs;

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
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.util.NamedSPILoader;

/** 
 * Encodes/decodes per-document values.
 * <p>
 * Note, when extending this class, the name ({@link #getName}) may
 * written into the index in certain configurations. In order for the segment 
 * to be read, the name must resolve to your implementation via {@link #forName(String)}.
 * This method uses Java's 
 * {@link ServiceLoader Service Provider Interface} (SPI) to resolve format names.
 * <p>
 * If you implement your own format, make sure that it has a no-arg constructor
 * so SPI can load it.
 * @see ServiceLoader
 * @lucene.experimental */
public abstract class DocValuesFormat implements NamedSPILoader.NamedSPI {
  
  private static final NamedSPILoader<DocValuesFormat> loader =
      new NamedSPILoader<>(DocValuesFormat.class);
  
  /** Unique name that's used to retrieve this format when
   *  reading the index.
   */
  private final String name;

  /**
   * Creates a new docvalues format.
   * <p>
   * The provided name will be written into the index segment in some configurations
   * (such as when using {@code PerFieldDocValuesFormat}): in such configurations,
   * for the segment to be read this class should be registered with Java's
   * SPI mechanism (registered in META-INF/ of your jar file, etc).
   * @param name must be all ascii alphanumeric, and less than 128 characters in length.
   */
  protected DocValuesFormat(String name) {
    NamedSPILoader.checkServiceName(name);
    this.name = name;
  }

  /** Returns a {@link DocValuesConsumer} to write docvalues to the
   *  index. */
  public abstract DocValuesConsumer fieldsConsumer(SegmentWriteState state) throws IOException;

  /** 
   * Returns a {@link DocValuesProducer} to read docvalues from the index. 
   * <p>
   * NOTE: by the time this call returns, it must hold open any files it will 
   * need to use; else, those files may be deleted. Additionally, required files 
   * may be deleted during the execution of this call before there is a chance 
   * to open them. Under these circumstances an IOException should be thrown by 
   * the implementation. IOExceptions are expected and will automatically cause 
   * a retry of the segment opening logic with the newly revised segments.
   */
  public abstract DocValuesProducer fieldsProducer(SegmentReadState state) throws IOException;

  @Override
  public final String getName() {
    return name;
  }
  
  @Override
  public String toString() {
    return "DocValuesFormat(name=" + name + ")";
  }
  
  /** looks up a format by name */
  public static DocValuesFormat forName(String name) {
    if (loader == null) {
      throw new IllegalStateException("You called DocValuesFormat.forName() before all formats could be initialized. "+
          "This likely happens if you call it from a DocValuesFormat's ctor.");
    }
    return loader.lookup(name);
  }
  
  /** returns a list of all available format names */
  public static Set<String> availableDocValuesFormats() {
    if (loader == null) {
      throw new IllegalStateException("You called DocValuesFormat.availableDocValuesFormats() before all formats could be initialized. "+
          "This likely happens if you call it from a DocValuesFormat's ctor.");
    }
    return loader.availableServices();
  }
  
  /** 
   * Reloads the DocValues format list from the given {@link ClassLoader}.
   * Changes to the docvalues formats are visible after the method ends, all
   * iterators ({@link #availableDocValuesFormats()},...) stay consistent. 
   * 
   * <p><b>NOTE:</b> Only new docvalues formats are added, existing ones are
   * never removed or replaced.
   * 
   * <p><em>This method is expensive and should only be called for discovery
   * of new docvalues formats on the given classpath/classloader!</em>
   */
  public static void reloadDocValuesFormats(ClassLoader classloader) {
    loader.reload(classloader);
  }
}
