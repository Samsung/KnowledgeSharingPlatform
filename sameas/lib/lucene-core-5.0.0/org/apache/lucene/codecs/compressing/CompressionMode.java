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
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;

/**
 * A compression mode. Tells how much effort should be spent on compression and
 * decompression of stored fields.
 * @lucene.experimental
 */
public abstract class CompressionMode {

  /**
   * A compression mode that trades compression ratio for speed. Although the
   * compression ratio might remain high, compression and decompression are
   * very fast. Use this mode with indices that have a high update rate but
   * should be able to load documents from disk quickly.
   */
  public static final CompressionMode FAST = new CompressionMode() {

    @Override
    public Compressor newCompressor() {
      return new LZ4FastCompressor();
    }

    @Override
    public Decompressor newDecompressor() {
      return LZ4_DECOMPRESSOR;
    }

    @Override
    public String toString() {
      return "FAST";
    }

  };

  /**
   * A compression mode that trades speed for compression ratio. Although
   * compression and decompression might be slow, this compression mode should
   * provide a good compression ratio. This mode might be interesting if/when
   * your index size is much bigger than your OS cache.
   */
  public static final CompressionMode HIGH_COMPRESSION = new CompressionMode() {

    @Override
    public Compressor newCompressor() {
      // 3 is the highest level that doesn't have lazy match evaluation
      return new DeflateCompressor(3);
    }

    @Override
    public Decompressor newDecompressor() {
      return new DeflateDecompressor();
    }

    @Override
    public String toString() {
      return "HIGH_COMPRESSION";
    }

  };

  /**
   * This compression mode is similar to {@link #FAST} but it spends more time
   * compressing in order to improve the compression ratio. This compression
   * mode is best used with indices that have a low update rate but should be
   * able to load documents from disk quickly.
   */
  public static final CompressionMode FAST_DECOMPRESSION = new CompressionMode() {

    @Override
    public Compressor newCompressor() {
      return new LZ4HighCompressor();
    }

    @Override
    public Decompressor newDecompressor() {
      return LZ4_DECOMPRESSOR;
    }

    @Override
    public String toString() {
      return "FAST_DECOMPRESSION";
    }

  };

  /** Sole constructor. */
  protected CompressionMode() {}

  /**
   * Create a new {@link Compressor} instance.
   */
  public abstract Compressor newCompressor();

  /**
   * Create a new {@link Decompressor} instance.
   */
  public abstract Decompressor newDecompressor();

  private static final Decompressor LZ4_DECOMPRESSOR = new Decompressor() {

    @Override
    public void decompress(DataInput in, int originalLength, int offset, int length, BytesRef bytes) throws IOException {
      assert offset + length <= originalLength;
      // add 7 padding bytes, this is not necessary but can help decompression run faster
      if (bytes.bytes.length < originalLength + 7) {
        bytes.bytes = new byte[ArrayUtil.oversize(originalLength + 7, 1)];
      }
      final int decompressedLength = LZ4.decompress(in, offset + length, bytes.bytes, 0);
      if (decompressedLength > originalLength) {
        throw new CorruptIndexException("Corrupted: lengths mismatch: " + decompressedLength + " > " + originalLength, in);
      }
      bytes.offset = offset;
      bytes.length = length;
    }

    @Override
    public Decompressor clone() {
      return this;
    }

  };

  private static final class LZ4FastCompressor extends Compressor {

    private final LZ4.HashTable ht;

    LZ4FastCompressor() {
      ht = new LZ4.HashTable();
    }

    @Override
    public void compress(byte[] bytes, int off, int len, DataOutput out)
        throws IOException {
      LZ4.compress(bytes, off, len, out, ht);
    }

  }

  private static final class LZ4HighCompressor extends Compressor {

    private final LZ4.HCHashTable ht;

    LZ4HighCompressor() {
      ht = new LZ4.HCHashTable();
    }

    @Override
    public void compress(byte[] bytes, int off, int len, DataOutput out)
        throws IOException {
      LZ4.compressHC(bytes, off, len, out, ht);
    }

  }

  private static final class DeflateDecompressor extends Decompressor {

    final Inflater decompressor;
    byte[] compressed;

    DeflateDecompressor() {
      decompressor = new Inflater(true);
      compressed = new byte[0];
    }

    @Override
    public void decompress(DataInput in, int originalLength, int offset, int length, BytesRef bytes) throws IOException {
      assert offset + length <= originalLength;
      if (length == 0) {
        bytes.length = 0;
        return;
      }
      final int compressedLength = in.readVInt();
      // pad with extra "dummy byte": see javadocs for using Inflater(true)
      // we do it for compliance, but it's unnecessary for years in zlib.
      final int paddedLength = compressedLength + 1;
      compressed = ArrayUtil.grow(compressed, paddedLength);
      in.readBytes(compressed, 0, compressedLength);
      compressed[compressedLength] = 0; // explicitly set dummy byte to 0

      decompressor.reset();
      // extra "dummy byte"
      decompressor.setInput(compressed, 0, paddedLength);

      bytes.offset = bytes.length = 0;
      bytes.bytes = ArrayUtil.grow(bytes.bytes, originalLength);
      try {
        bytes.length = decompressor.inflate(bytes.bytes, bytes.length, originalLength);
      } catch (DataFormatException e) {
        throw new IOException(e);
      }
      if (!decompressor.finished()) {
        throw new CorruptIndexException("Invalid decoder state: needsInput=" + decompressor.needsInput() 
                                                            + ", needsDict=" + decompressor.needsDictionary(), in);
      }
      if (bytes.length != originalLength) {
        throw new CorruptIndexException("Lengths mismatch: " + bytes.length + " != " + originalLength, in);
      }
      bytes.offset = offset;
      bytes.length = length;
    }

    @Override
    public Decompressor clone() {
      return new DeflateDecompressor();
    }

  }

  private static class DeflateCompressor extends Compressor {

    final Deflater compressor;
    byte[] compressed;

    DeflateCompressor(int level) {
      compressor = new Deflater(level, true);
      compressed = new byte[64];
    }

    @Override
    public void compress(byte[] bytes, int off, int len, DataOutput out) throws IOException {
      compressor.reset();
      compressor.setInput(bytes, off, len);
      compressor.finish();

      if (compressor.needsInput()) {
        // no output
        assert len == 0 : len;
        out.writeVInt(0);
        return;
      }

      int totalCount = 0;
      for (;;) {
        final int count = compressor.deflate(compressed, totalCount, compressed.length - totalCount);
        totalCount += count;
        assert totalCount <= compressed.length;
        if (compressor.finished()) {
          break;
        } else {
          compressed = ArrayUtil.grow(compressed);
        }
      }

      out.writeVInt(totalCount);
      out.writeBytes(compressed, totalCount);
    }

  }

}
