package org.apache.lucene.codecs.compressing;

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

import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.TermVectorsFormat;
import org.apache.lucene.codecs.TermVectorsReader;
import org.apache.lucene.codecs.TermVectorsWriter;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;

/**
 * A {@link TermVectorsFormat} that compresses chunks of documents together in
 * order to improve the compression ratio.
 * @lucene.experimental
 */
public class CompressingTermVectorsFormat extends TermVectorsFormat {

  private final String formatName;
  private final String segmentSuffix;
  private final CompressionMode compressionMode;
  private final int chunkSize;
  private final int blockSize;

  /**
   * Create a new {@link CompressingTermVectorsFormat}.
   * <p>
   * <code>formatName</code> is the name of the format. This name will be used
   * in the file formats to perform
   * {@link CodecUtil#checkIndexHeader codec header checks}.
   * <p>
   * The <code>compressionMode</code> parameter allows you to choose between
   * compression algorithms that have various compression and decompression
   * speeds so that you can pick the one that best fits your indexing and
   * searching throughput. You should never instantiate two
   * {@link CompressingTermVectorsFormat}s that have the same name but
   * different {@link CompressionMode}s.
   * <p>
   * <code>chunkSize</code> is the minimum byte size of a chunk of documents.
   * Higher values of <code>chunkSize</code> should improve the compression
   * ratio but will require more memory at indexing time and might make document
   * loading a little slower (depending on the size of your OS cache compared
   * to the size of your index).
   *
   * @param formatName the name of the {@link StoredFieldsFormat}
   * @param segmentSuffix a suffix to append to files created by this format
   * @param compressionMode the {@link CompressionMode} to use
   * @param chunkSize the minimum number of bytes of a single chunk of stored documents
   * @param blockSize the number of chunks to store in an index block.
   * @see CompressionMode
   */
  public CompressingTermVectorsFormat(String formatName, String segmentSuffix,
      CompressionMode compressionMode, int chunkSize, int blockSize) {
    this.formatName = formatName;
    this.segmentSuffix = segmentSuffix;
    this.compressionMode = compressionMode;
    if (chunkSize < 1) {
      throw new IllegalArgumentException("chunkSize must be >= 1");
    }
    this.chunkSize = chunkSize;
    if (blockSize < 1) {
      throw new IllegalArgumentException("blockSize must be >= 1");
    }
    this.blockSize = blockSize;
  }

  @Override
  public final TermVectorsReader vectorsReader(Directory directory,
      SegmentInfo segmentInfo, FieldInfos fieldInfos, IOContext context)
      throws IOException {
    return new CompressingTermVectorsReader(directory, segmentInfo, segmentSuffix,
        fieldInfos, context, formatName, compressionMode);
  }

  @Override
  public final TermVectorsWriter vectorsWriter(Directory directory,
      SegmentInfo segmentInfo, IOContext context) throws IOException {
    return new CompressingTermVectorsWriter(directory, segmentInfo, segmentSuffix,
        context, formatName, compressionMode, chunkSize, blockSize);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(compressionMode=" + compressionMode
        + ", chunkSize=" + chunkSize + ", blockSize=" + blockSize + ")";
  }

}
