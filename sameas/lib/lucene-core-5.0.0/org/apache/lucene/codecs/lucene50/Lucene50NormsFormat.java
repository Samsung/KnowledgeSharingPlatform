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

import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.codecs.NormsConsumer;
import org.apache.lucene.codecs.NormsFormat;
import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.SmallFloat;
import org.apache.lucene.util.packed.BlockPackedWriter;
import org.apache.lucene.util.packed.MonotonicBlockPackedWriter;
import org.apache.lucene.util.packed.PackedInts;

/**
 * Lucene 5.0 Score normalization format.
 * <p>
 * Encodes normalization values with these strategies:
 * <p>
 * <ul>
 *    <li>Uncompressed: when values fit into a single byte and would require more than 4 bits
 *        per value, they are just encoded as an uncompressed byte array.
 *    <li>Constant: when there is only one value present for the entire field, no actual data
 *        is written: this constant is encoded in the metadata
 *    <li>Table-compressed: when the number of unique values is very small (&lt; 64), and
 *        when there are unused "gaps" in the range of values used (such as {@link SmallFloat}), 
 *        a lookup table is written instead. Each per-document entry is instead the ordinal 
 *        to this table, and those ordinals are compressed with bitpacking ({@link PackedInts}). 
 *    <li>Delta-compressed: per-document integers written as deltas from the minimum value,
 *        compressed with bitpacking. For more information, see {@link BlockPackedWriter}.
 *        This is only used when norms of larger than one byte are present.
 *    <li>Indirect: when norms are extremely sparse, missing values are omitted.
 *        Access to an individual value is slower, but missing norm values are never accessed
 *        by search code.
 *    <li>Patched bitset: when a single norm value dominates, a sparse bitset encodes docs
 *        with exceptions, so that access to the common value is still very fast. outliers
 *        fall through to an exception handling mechanism (Indirect or Constant).
 *    <li>Patched table: when a small number of norm values dominate, a table is used for the
 *        common values to allow fast access. less common values fall through to an exception
 *        handling mechanism (Indirect).
 * </ul>
 * <p>
 * Files:
 * <ol>
 *   <li><tt>.nvd</tt>: Norms data</li>
 *   <li><tt>.nvm</tt>: Norms metadata</li>
 * </ol>
 * <ol>
 *   <li><a name="nvm" id="nvm"></a>
 *   <p>The Norms metadata or .nvm file.</p>
 *   <p>For each norms field, this stores metadata, such as the offset into the 
 *      Norms data (.nvd)</p>
 *   <p>Norms metadata (.dvm) --&gt; Header,&lt;Entry&gt;<sup>NumFields</sup>,Footer</p>
 *   <ul>
 *     <li>Header --&gt; {@link CodecUtil#writeIndexHeader IndexHeader}</li>
 *     <li>Entry --&gt; FieldNumber,Type,Offset</li>
 *     <li>FieldNumber --&gt; {@link DataOutput#writeVInt vInt}</li>
 *     <li>Type --&gt; {@link DataOutput#writeByte Byte}</li>
 *     <li>Offset --&gt; {@link DataOutput#writeLong Int64}</li>
 *     <li>Footer --&gt; {@link CodecUtil#writeFooter CodecFooter}</li>
 *   </ul>
 *   <p>FieldNumber of -1 indicates the end of metadata.</p>
 *   <p>Offset is the pointer to the start of the data in the norms data (.nvd), or the singleton value for Constant</p>
 *   <p>Type indicates how Numeric values will be compressed:
 *      <ul>
 *         <li>0 --&gt; delta-compressed. For each block of 16k integers, every integer is delta-encoded
 *             from the minimum value within the block. 
 *         <li>1 --&gt; table-compressed. When the number of unique numeric values is small and it would save space,
 *             a lookup table of unique values is written, followed by the ordinal for each document.
 *         <li>2 --&gt; constant. When there is a single value for the entire field.
 *         <li>3 --&gt; uncompressed: Values written as a simple byte[].
 *         <li>4 --&gt; indirect. Only documents with a value are written with monotonic compression. a nested
 *             entry for the same field will follow for the exception handler.
 *         <li>5 --&gt; patched bitset. Encoded the same as indirect.
 *         <li>6 --&gt; patched table. Documents with very common values are written with a lookup table.
 *             Other values are written using a nested indirect.
 *      </ul>
 *   <li><a name="nvd" id="nvd"></a>
 *   <p>The Norms data or .nvd file.</p>
 *   <p>For each Norms field, this stores the actual per-document data (the heavy-lifting)</p>
 *   <p>Norms data (.nvd) --&gt; Header,&lt;Uncompressed | TableCompressed | DeltaCompressed | MonotonicCompressed &gt;<sup>NumFields</sup>,Footer</p>
 *   <ul>
 *     <li>Header --&gt; {@link CodecUtil#writeIndexHeader IndexHeader}</li>
 *     <li>Uncompressed --&gt;  {@link DataOutput#writeByte Byte}<sup>maxDoc</sup></li>
 *     <li>TableCompressed --&gt; PackedIntsVersion,Table,BitPackedData</li>
 *     <li>Table --&gt; TableSize, {@link DataOutput#writeLong int64}<sup>TableSize</sup></li>
 *     <li>BitpackedData --&gt; {@link PackedInts}</li>
 *     <li>DeltaCompressed --&gt; PackedIntsVersion,BlockSize,DeltaCompressedData</li>
 *     <li>DeltaCompressedData --&gt; {@link BlockPackedWriter BlockPackedWriter(blockSize=16k)}</li>
 *     <li>MonotonicCompressed --&gt; PackedIntsVersion,BlockSize,MonotonicCompressedData</li>
 *     <li>MonotonicCompressedData --&gt; {@link MonotonicBlockPackedWriter MonotonicBlockPackedWriter(blockSize=16k)}</li>
 *     <li>PackedIntsVersion,BlockSize,TableSize --&gt; {@link DataOutput#writeVInt vInt}</li>
 *     <li>Footer --&gt; {@link CodecUtil#writeFooter CodecFooter}</li>
 *   </ul>
 * </ol>
 * @lucene.experimental
 */
public class Lucene50NormsFormat extends NormsFormat {

  /** Sole Constructor */
  public Lucene50NormsFormat() {}
  
  @Override
  public NormsConsumer normsConsumer(SegmentWriteState state) throws IOException {
    return new Lucene50NormsConsumer(state, DATA_CODEC, DATA_EXTENSION, METADATA_CODEC, METADATA_EXTENSION);
  }

  @Override
  public NormsProducer normsProducer(SegmentReadState state) throws IOException {
    return new Lucene50NormsProducer(state, DATA_CODEC, DATA_EXTENSION, METADATA_CODEC, METADATA_EXTENSION);
  }
  
  private static final String DATA_CODEC = "Lucene50NormsData";
  private static final String DATA_EXTENSION = "nvd";
  private static final String METADATA_CODEC = "Lucene50NormsMetadata";
  private static final String METADATA_EXTENSION = "nvm";
  static final int VERSION_START = 0;
  static final int VERSION_CURRENT = VERSION_START;
}
