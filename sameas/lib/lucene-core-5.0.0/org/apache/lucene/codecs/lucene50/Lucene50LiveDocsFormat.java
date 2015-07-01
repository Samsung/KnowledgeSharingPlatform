package org.apache.lucene.codecs.lucene50;

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

import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.codecs.LiveDocsFormat;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.MutableBits;

/** 
 * Lucene 5.0 live docs format 
 * <p>
 * <p>The .liv file is optional, and only exists when a segment contains
 * deletions.</p>
 * <p>Although per-segment, this file is maintained exterior to compound segment
 * files.</p>
 * <p>Deletions (.liv) --&gt; IndexHeader,Generation,Bits</p>
 * <ul>
 *   <li>SegmentHeader --&gt; {@link CodecUtil#writeIndexHeader IndexHeader}</li>
 *   <li>Bits --&gt; &lt;{@link DataOutput#writeLong Int64}&gt; <sup>LongCount</sup></li>
 * </ul>
 */
public final class Lucene50LiveDocsFormat extends LiveDocsFormat {
  
  /** Sole constructor. */
  public Lucene50LiveDocsFormat() {
  }
  
  /** extension of live docs */
  private static final String EXTENSION = "liv";
  
  /** codec of live docs */
  private static final String CODEC_NAME = "Lucene50LiveDocs";
  
  /** supported version range */
  private static final int VERSION_START = 0;
  private static final int VERSION_CURRENT = VERSION_START;

  @Override
  public MutableBits newLiveDocs(int size) throws IOException {
    FixedBitSet bits = new FixedBitSet(size);
    bits.set(0, size);
    return bits;
  }

  @Override
  public MutableBits newLiveDocs(Bits existing) throws IOException {
    FixedBitSet fbs = (FixedBitSet) existing;
    return fbs.clone();
  }

  @Override
  public Bits readLiveDocs(Directory dir, SegmentCommitInfo info, IOContext context) throws IOException {
    long gen = info.getDelGen();
    String name = IndexFileNames.fileNameFromGeneration(info.info.name, EXTENSION, gen);
    final int length = info.info.getDocCount();
    try (ChecksumIndexInput input = dir.openChecksumInput(name, context)) {
      Throwable priorE = null;
      try {
        CodecUtil.checkIndexHeader(input, CODEC_NAME, VERSION_START, VERSION_CURRENT, 
                                     info.info.getId(), Long.toString(gen, Character.MAX_RADIX));
        long data[] = new long[FixedBitSet.bits2words(length)];
        for (int i = 0; i < data.length; i++) {
          data[i] = input.readLong();
        }
        FixedBitSet fbs = new FixedBitSet(data, length);
        if (fbs.length() - fbs.cardinality() != info.getDelCount()) {
          throw new CorruptIndexException("bits.deleted=" + (fbs.length() - fbs.cardinality()) + 
                                          " info.delcount=" + info.getDelCount(), input);
        }
        return fbs;
      } catch (Throwable exception) {
        priorE = exception;
      } finally {
        CodecUtil.checkFooter(input, priorE);
      }
    }
    throw new AssertionError();
  }

  @Override
  public void writeLiveDocs(MutableBits bits, Directory dir, SegmentCommitInfo info, int newDelCount, IOContext context) throws IOException {
    long gen = info.getNextDelGen();
    String name = IndexFileNames.fileNameFromGeneration(info.info.name, EXTENSION, gen);
    FixedBitSet fbs = (FixedBitSet) bits;
    if (fbs.length() - fbs.cardinality() != info.getDelCount() + newDelCount) {
      throw new CorruptIndexException("bits.deleted=" + (fbs.length() - fbs.cardinality()) + 
                                      " info.delcount=" + info.getDelCount() + " newdelcount=" + newDelCount, name);
    }
    long data[] = fbs.getBits();
    try (IndexOutput output = dir.createOutput(name, context)) {
      CodecUtil.writeIndexHeader(output, CODEC_NAME, VERSION_CURRENT, info.info.getId(), Long.toString(gen, Character.MAX_RADIX));
      for (int i = 0; i < data.length; i++) {
        output.writeLong(data[i]);
      }
      CodecUtil.writeFooter(output);
    }
  }

  @Override
  public void files(SegmentCommitInfo info, Collection<String> files) throws IOException {
    if (info.hasDeletions()) {
      files.add(IndexFileNames.fileNameFromGeneration(info.info.name, EXTENSION, info.getDelGen()));
    }
  }
}
