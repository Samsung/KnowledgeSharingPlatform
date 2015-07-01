package org.apache.lucene.index;

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

import java.util.Objects;

import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.TermVectorsReader;
import org.apache.lucene.util.Bits;

/** 
 * A <code>FilterCodecReader</code> contains another CodecReader, which it
 * uses as its basic source of data, possibly transforming the data along the
 * way or providing additional functionality.
 */
public class FilterCodecReader extends CodecReader {
  /** 
   * The underlying CodecReader instance. 
   */
  protected final CodecReader in;
  
  /**
   * Creates a new FilterCodecReader.
   * @param in the underlying CodecReader instance.
   */
  public FilterCodecReader(CodecReader in) {
    this.in = Objects.requireNonNull(in);
  }

  @Override
  public StoredFieldsReader getFieldsReader() {
    return in.getFieldsReader();
  }

  @Override
  public TermVectorsReader getTermVectorsReader() {
    return in.getTermVectorsReader();
  }

  @Override
  public NormsProducer getNormsReader() {
    return in.getNormsReader();
  }

  @Override
  public DocValuesProducer getDocValuesReader() {
    return in.getDocValuesReader();
  }

  @Override
  public FieldsProducer getPostingsReader() {
    return in.getPostingsReader();
  }

  @Override
  public Bits getLiveDocs() {
    return in.getLiveDocs();
  }

  @Override
  public FieldInfos getFieldInfos() {
    return in.getFieldInfos();
  }

  @Override
  public int numDocs() {
    return in.numDocs();
  }

  @Override
  public int maxDoc() {
    return in.maxDoc();
  }

  @Override
  public void addCoreClosedListener(CoreClosedListener listener) {
    in.addCoreClosedListener(listener);
  }

  @Override
  public void removeCoreClosedListener(CoreClosedListener listener) {
    in.removeCoreClosedListener(listener);
  }
}
