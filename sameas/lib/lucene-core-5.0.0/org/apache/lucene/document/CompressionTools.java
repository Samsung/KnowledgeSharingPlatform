package org.apache.lucene.document;

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

import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.DataFormatException;
import java.io.ByteArrayOutputStream;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.UnicodeUtil;

/** Simple utility class providing static methods to
 *  compress and decompress binary data for stored fields.
 *  This class uses java.util.zip.Deflater and Inflater
 *  classes to compress and decompress.
 */

public class CompressionTools {

  // Export only static methods
  private CompressionTools() {}

  /** Compresses the specified byte range using the
   *  specified compressionLevel (constants are defined in
   *  java.util.zip.Deflater). */
  public static byte[] compress(byte[] value, int offset, int length, int compressionLevel) {

    /* Create an expandable byte array to hold the compressed data.
     * You cannot use an array that's the same size as the orginal because
     * there is no guarantee that the compressed data will be smaller than
     * the uncompressed data. */
    ByteArrayOutputStream bos = new ByteArrayOutputStream(length);

    Deflater compressor = new Deflater();

    try {
      compressor.setLevel(compressionLevel);
      compressor.setInput(value, offset, length);
      compressor.finish();

      // Compress the data
      final byte[] buf = new byte[1024];
      while (!compressor.finished()) {
        int count = compressor.deflate(buf);
        bos.write(buf, 0, count);
      }
    } finally {
      compressor.end();
    }

    return bos.toByteArray();
  }

  /** Compresses the specified byte range, with default BEST_COMPRESSION level */
  public static byte[] compress(byte[] value, int offset, int length) {
    return compress(value, offset, length, Deflater.BEST_COMPRESSION);
  }
  
  /** Compresses all bytes in the array, with default BEST_COMPRESSION level */
  public static byte[] compress(byte[] value) {
    return compress(value, 0, value.length, Deflater.BEST_COMPRESSION);
  }

  /** Compresses the String value, with default BEST_COMPRESSION level */
  public static byte[] compressString(String value) {
    return compressString(value, Deflater.BEST_COMPRESSION);
  }

  /** Compresses the String value using the specified
   *  compressionLevel (constants are defined in
   *  java.util.zip.Deflater). */
  public static byte[] compressString(String value, int compressionLevel) {
    byte[] b = new byte[UnicodeUtil.MAX_UTF8_BYTES_PER_CHAR * value.length()];
    final int len = UnicodeUtil.UTF16toUTF8(value, 0, value.length(), b);
    return compress(b, 0, len, compressionLevel);
  }

  /** Decompress the byte array previously returned by
   *  compress (referenced by the provided BytesRef) */
  public static byte[] decompress(BytesRef bytes) throws DataFormatException {
    return decompress(bytes.bytes, bytes.offset, bytes.length);
  }

  /** Decompress the byte array previously returned by
   *  compress */
  public static byte[] decompress(byte[] value) throws DataFormatException {
    return decompress(value, 0, value.length);
  }

  /** Decompress the byte array previously returned by
   *  compress */
  public static byte[] decompress(byte[] value, int offset, int length) throws DataFormatException {
    // Create an expandable byte array to hold the decompressed data
    ByteArrayOutputStream bos = new ByteArrayOutputStream(length);

    Inflater decompressor = new Inflater();

    try {
      decompressor.setInput(value, offset, length);

      // Decompress the data
      final byte[] buf = new byte[1024];
      while (!decompressor.finished()) {
        int count = decompressor.inflate(buf);
        bos.write(buf, 0, count);
      }
    } finally {  
      decompressor.end();
    }
    
    return bos.toByteArray();
  }

  /** Decompress the byte array previously returned by
   *  compressString back into a String */
  public static String decompressString(byte[] value) throws DataFormatException {
    return decompressString(value, 0, value.length);
  }

  /** Decompress the byte array previously returned by
   *  compressString back into a String */
  public static String decompressString(byte[] value, int offset, int length) throws DataFormatException {
    final byte[] bytes = decompress(value, offset, length);
    final char[] result = new char[bytes.length];
    final int len = UnicodeUtil.UTF8toUTF16(bytes, 0, bytes.length, result);
    return new String(result, 0, len);
  }

  /** Decompress the byte array (referenced by the provided BytesRef) 
   *  previously returned by compressString back into a String */
  public static String decompressString(BytesRef bytes) throws DataFormatException {
    return decompressString(bytes.bytes, bytes.offset, bytes.length);
  }
}
