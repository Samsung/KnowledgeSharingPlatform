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

/**
 * A codec that forwards all its method calls to another codec.
 * <p>
 * Extend this class when you need to reuse the functionality of an existing
 * codec. For example, if you want to build a codec that redefines LuceneMN's
 * {@link LiveDocsFormat}:
 * <pre class="prettyprint">
 *   public final class CustomCodec extends FilterCodec {
 *
 *     public CustomCodec() {
 *       super("CustomCodec", new LuceneMNCodec());
 *     }
 *
 *     public LiveDocsFormat liveDocsFormat() {
 *       return new CustomLiveDocsFormat();
 *     }
 *
 *   }
 * </pre>
 * 
 * <p><em>Please note:</em> Don't call {@link Codec#forName} from
 * the no-arg constructor of your own codec. When the SPI framework
 * loads your own Codec as SPI component, SPI has not yet fully initialized!
 * If you want to extend another Codec, instantiate it directly by calling
 * its constructor.
 * 
 * @lucene.experimental
 */
public abstract class FilterCodec extends Codec {

  /** The codec to filter. */
  protected final Codec delegate;
  
  /** Sole constructor. When subclassing this codec,
   * create a no-arg ctor and pass the delegate codec
   * and a unique name to this ctor.
   */
  protected FilterCodec(String name, Codec delegate) {
    super(name);
    this.delegate = delegate;
  }

  @Override
  public DocValuesFormat docValuesFormat() {
    return delegate.docValuesFormat();
  }

  @Override
  public FieldInfosFormat fieldInfosFormat() {
    return delegate.fieldInfosFormat();
  }

  @Override
  public LiveDocsFormat liveDocsFormat() {
    return delegate.liveDocsFormat();
  }

  @Override
  public NormsFormat normsFormat() {
    return delegate.normsFormat();
  }

  @Override
  public PostingsFormat postingsFormat() {
    return delegate.postingsFormat();
  }

  @Override
  public SegmentInfoFormat segmentInfoFormat() {
    return delegate.segmentInfoFormat();
  }

  @Override
  public StoredFieldsFormat storedFieldsFormat() {
    return delegate.storedFieldsFormat();
  }

  @Override
  public TermVectorsFormat termVectorsFormat() {
    return delegate.termVectorsFormat();
  }

  @Override
  public CompoundFormat compoundFormat() {
    return delegate.compoundFormat();
  }
}
