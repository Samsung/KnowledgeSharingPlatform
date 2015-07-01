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

import java.util.Set;
import java.util.ServiceLoader; // javadocs

import org.apache.lucene.index.IndexWriterConfig; // javadocs
import org.apache.lucene.util.NamedSPILoader;

/**
 * Encodes/decodes an inverted index segment.
 * <p>
 * Note, when extending this class, the name ({@link #getName}) is 
 * written into the index. In order for the segment to be read, the
 * name must resolve to your implementation via {@link #forName(String)}.
 * This method uses Java's 
 * {@link ServiceLoader Service Provider Interface} (SPI) to resolve codec names.
 * <p>
 * If you implement your own codec, make sure that it has a no-arg constructor
 * so SPI can load it.
 * @see ServiceLoader
 */
public abstract class Codec implements NamedSPILoader.NamedSPI {

  private static final NamedSPILoader<Codec> loader =
    new NamedSPILoader<>(Codec.class);

  private final String name;

  /**
   * Creates a new codec.
   * <p>
   * The provided name will be written into the index segment: in order to
   * for the segment to be read this class should be registered with Java's
   * SPI mechanism (registered in META-INF/ of your jar file, etc).
   * @param name must be all ascii alphanumeric, and less than 128 characters in length.
   */
  protected Codec(String name) {
    NamedSPILoader.checkServiceName(name);
    this.name = name;
  }
  
  /** Returns this codec's name */
  @Override
  public final String getName() {
    return name;
  }
  
  /** Encodes/decodes postings */
  public abstract PostingsFormat postingsFormat();

  /** Encodes/decodes docvalues */
  public abstract DocValuesFormat docValuesFormat();
  
  /** Encodes/decodes stored fields */
  public abstract StoredFieldsFormat storedFieldsFormat();
  
  /** Encodes/decodes term vectors */
  public abstract TermVectorsFormat termVectorsFormat();
  
  /** Encodes/decodes field infos file */
  public abstract FieldInfosFormat fieldInfosFormat();
  
  /** Encodes/decodes segment info file */
  public abstract SegmentInfoFormat segmentInfoFormat();
  
  /** Encodes/decodes document normalization values */
  public abstract NormsFormat normsFormat();

  /** Encodes/decodes live docs */
  public abstract LiveDocsFormat liveDocsFormat();
  
  /** Encodes/decodes compound files */
  public abstract CompoundFormat compoundFormat();
  
  /** looks up a codec by name */
  public static Codec forName(String name) {
    if (loader == null) {
      throw new IllegalStateException("You called Codec.forName() before all Codecs could be initialized. "+
          "This likely happens if you call it from a Codec's ctor.");
    }
    return loader.lookup(name);
  }
  
  /** returns a list of all available codec names */
  public static Set<String> availableCodecs() {
    if (loader == null) {
      throw new IllegalStateException("You called Codec.availableCodecs() before all Codecs could be initialized. "+
          "This likely happens if you call it from a Codec's ctor.");
    }
    return loader.availableServices();
  }
  
  /** 
   * Reloads the codec list from the given {@link ClassLoader}.
   * Changes to the codecs are visible after the method ends, all
   * iterators ({@link #availableCodecs()},...) stay consistent. 
   * 
   * <p><b>NOTE:</b> Only new codecs are added, existing ones are
   * never removed or replaced.
   * 
   * <p><em>This method is expensive and should only be called for discovery
   * of new codecs on the given classpath/classloader!</em>
   */
  public static void reloadCodecs(ClassLoader classloader) {
    loader.reload(classloader);
  }
  
  private static Codec defaultCodec = Codec.forName("Lucene50");
  
  /** expert: returns the default codec used for newly created
   *  {@link IndexWriterConfig}s.
   */
  // TODO: should we use this, or maybe a system property is better?
  public static Codec getDefault() {
    return defaultCodec;
  }
  
  /** expert: sets the default codec used for newly created
   *  {@link IndexWriterConfig}s.
   */
  public static void setDefault(Codec codec) {
    defaultCodec = codec;
  }

  /**
   * returns the codec's name. Subclasses can override to provide
   * more detail (such as parameters).
   */
  @Override
  public String toString() {
    return name;
  }
}
