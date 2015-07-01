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
import java.util.Arrays;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.packed.PackedInts.Decoder;
import org.apache.lucene.util.packed.PackedInts.FormatAndBits;
import org.apache.lucene.util.packed.PackedInts;

import static org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat.BLOCK_SIZE;

/**
 * Encode all values in normal area with fixed bit width, 
 * which is determined by the max value in this block.
 */
final class ForUtil {

  /**
   * Special number of bits per value used whenever all values to encode are equal.
   */
  private static final int ALL_VALUES_EQUAL = 0;

  /**
   * Upper limit of the number of bytes that might be required to stored
   * <code>BLOCK_SIZE</code> encoded values.
   */
  static final int MAX_ENCODED_SIZE = BLOCK_SIZE * 4;

  /**
   * Upper limit of the number of values that might be decoded in a single call to
   * {@link #readBlock(IndexInput, byte[], int[])}. Although values after
   * <code>BLOCK_SIZE</code> are garbage, it is necessary to allocate value buffers
   * whose size is {@code >= MAX_DATA_SIZE} to avoid {@link ArrayIndexOutOfBoundsException}s.
   */
  static final int MAX_DATA_SIZE;
  static {
    int maxDataSize = 0;
    for(int version=PackedInts.VERSION_START;version<=PackedInts.VERSION_CURRENT;version++) {
      for (PackedInts.Format format : PackedInts.Format.values()) {
        for (int bpv = 1; bpv <= 32; ++bpv) {
          if (!format.isSupported(bpv)) {
            continue;
          }
          final PackedInts.Decoder decoder = PackedInts.getDecoder(format, version, bpv);
          final int iterations = computeIterations(decoder);
          maxDataSize = Math.max(maxDataSize, iterations * decoder.byteValueCount());
        }
      }
    }
    MAX_DATA_SIZE = maxDataSize;
  }

  /**
   * Compute the number of iterations required to decode <code>BLOCK_SIZE</code>
   * values with the provided {@link Decoder}.
   */
  private static int computeIterations(PackedInts.Decoder decoder) {
    return (int) Math.ceil((float) BLOCK_SIZE / decoder.byteValueCount());
  }

  /**
   * Compute the number of bytes required to encode a block of values that require
   * <code>bitsPerValue</code> bits per value with format <code>format</code>.
   */
  private static int encodedSize(PackedInts.Format format, int packedIntsVersion, int bitsPerValue) {
    final long byteCount = format.byteCount(packedIntsVersion, BLOCK_SIZE, bitsPerValue);
    assert byteCount >= 0 && byteCount <= Integer.MAX_VALUE : byteCount;
    return (int) byteCount;
  }

  private final int[] encodedSizes;
  private final PackedInts.Encoder[] encoders;
  private final PackedInts.Decoder[] decoders;
  private final int[] iterations;

  /**
   * Create a new {@link ForUtil} instance and save state into <code>out</code>.
   */
  ForUtil(float acceptableOverheadRatio, DataOutput out) throws IOException {
    out.writeVInt(PackedInts.VERSION_CURRENT);
    encodedSizes = new int[33];
    encoders = new PackedInts.Encoder[33];
    decoders = new PackedInts.Decoder[33];
    iterations = new int[33];

    for (int bpv = 1; bpv <= 32; ++bpv) {
      final FormatAndBits formatAndBits = PackedInts.fastestFormatAndBits(
          BLOCK_SIZE, bpv, acceptableOverheadRatio);
      assert formatAndBits.format.isSupported(formatAndBits.bitsPerValue);
      assert formatAndBits.bitsPerValue <= 32;
      encodedSizes[bpv] = encodedSize(formatAndBits.format, PackedInts.VERSION_CURRENT, formatAndBits.bitsPerValue);
      encoders[bpv] = PackedInts.getEncoder(
          formatAndBits.format, PackedInts.VERSION_CURRENT, formatAndBits.bitsPerValue);
      decoders[bpv] = PackedInts.getDecoder(
          formatAndBits.format, PackedInts.VERSION_CURRENT, formatAndBits.bitsPerValue);
      iterations[bpv] = computeIterations(decoders[bpv]);

      out.writeVInt(formatAndBits.format.getId() << 5 | (formatAndBits.bitsPerValue - 1));
    }
  }

  /**
   * Restore a {@link ForUtil} from a {@link DataInput}.
   */
  ForUtil(DataInput in) throws IOException {
    int packedIntsVersion = in.readVInt();
    PackedInts.checkVersion(packedIntsVersion);
    encodedSizes = new int[33];
    encoders = new PackedInts.Encoder[33];
    decoders = new PackedInts.Decoder[33];
    iterations = new int[33];

    for (int bpv = 1; bpv <= 32; ++bpv) {
      final int code = in.readVInt();
      final int formatId = code >>> 5;
      final int bitsPerValue = (code & 31) + 1;

      final PackedInts.Format format = PackedInts.Format.byId(formatId);
      assert format.isSupported(bitsPerValue);
      encodedSizes[bpv] = encodedSize(format, packedIntsVersion, bitsPerValue);
      encoders[bpv] = PackedInts.getEncoder(
          format, packedIntsVersion, bitsPerValue);
      decoders[bpv] = PackedInts.getDecoder(
          format, packedIntsVersion, bitsPerValue);
      iterations[bpv] = computeIterations(decoders[bpv]);
    }
  }

  /**
   * Write a block of data (<code>For</code> format).
   *
   * @param data     the data to write
   * @param encoded  a buffer to use to encode data
   * @param out      the destination output
   * @throws IOException If there is a low-level I/O error
   */
  void writeBlock(int[] data, byte[] encoded, IndexOutput out) throws IOException {
    if (isAllEqual(data)) {
      out.writeByte((byte) ALL_VALUES_EQUAL);
      out.writeVInt(data[0]);
      return;
    }

    final int numBits = bitsRequired(data);
    assert numBits > 0 && numBits <= 32 : numBits;
    final PackedInts.Encoder encoder = encoders[numBits];
    final int iters = iterations[numBits];
    assert iters * encoder.byteValueCount() >= BLOCK_SIZE;
    final int encodedSize = encodedSizes[numBits];
    assert iters * encoder.byteBlockCount() >= encodedSize;

    out.writeByte((byte) numBits);

    encoder.encode(data, 0, encoded, 0, iters);
    out.writeBytes(encoded, encodedSize);
  }

  /**
   * Read the next block of data (<code>For</code> format).
   *
   * @param in        the input to use to read data
   * @param encoded   a buffer that can be used to store encoded data
   * @param decoded   where to write decoded data
   * @throws IOException If there is a low-level I/O error
   */
  void readBlock(IndexInput in, byte[] encoded, int[] decoded) throws IOException {
    final int numBits = in.readByte();
    assert numBits <= 32 : numBits;

    if (numBits == ALL_VALUES_EQUAL) {
      final int value = in.readVInt();
      Arrays.fill(decoded, 0, BLOCK_SIZE, value);
      return;
    }

    final int encodedSize = encodedSizes[numBits];
    in.readBytes(encoded, 0, encodedSize);

    final PackedInts.Decoder decoder = decoders[numBits];
    final int iters = iterations[numBits];
    assert iters * decoder.byteValueCount() >= BLOCK_SIZE;

    decoder.decode(encoded, 0, decoded, 0, iters);
  }

  /**
   * Skip the next block of data.
   *
   * @param in      the input where to read data
   * @throws IOException If there is a low-level I/O error
   */
  void skipBlock(IndexInput in) throws IOException {
    final int numBits = in.readByte();
    if (numBits == ALL_VALUES_EQUAL) {
      in.readVInt();
      return;
    }
    assert numBits > 0 && numBits <= 32 : numBits;
    final int encodedSize = encodedSizes[numBits];
    in.seek(in.getFilePointer() + encodedSize);
  }

  private static boolean isAllEqual(final int[] data) {
    final int v = data[0];
    for (int i = 1; i < BLOCK_SIZE; ++i) {
      if (data[i] != v) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compute the number of bits required to serialize any of the longs in
   * <code>data</code>.
   */
  private static int bitsRequired(final int[] data) {
    long or = 0;
    for (int i = 0; i < BLOCK_SIZE; ++i) {
      assert data[i] >= 0;
      or |= data[i];
    }
    return PackedInts.bitsRequired(or);
  }

}
