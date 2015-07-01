package org.apache.lucene.analysis.payloads;
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
 * Utility methods for encoding payloads.
 *
 **/
public class PayloadHelper {

  public static byte[] encodeFloat(float payload) {
    return encodeFloat(payload, new byte[4], 0);
  }

  public static byte[] encodeFloat(float payload, byte[] data, int offset){
    return encodeInt(Float.floatToIntBits(payload), data, offset);
  }

  public static byte[] encodeInt(int payload){
    return encodeInt(payload, new byte[4], 0);
  }

  public static byte[] encodeInt(int payload, byte[] data, int offset){
    data[offset] = (byte)(payload >> 24);
    data[offset + 1] = (byte)(payload >> 16);
    data[offset + 2] = (byte)(payload >>  8);
    data[offset + 3] = (byte) payload;
    return data;
  }

  /**
   * @see #decodeFloat(byte[], int)
   * @see #encodeFloat(float)
   * @return the decoded float
   */
  public static float decodeFloat(byte [] bytes){
    return decodeFloat(bytes, 0);
  }

  /**
   * Decode the payload that was encoded using {@link #encodeFloat(float)}.
   * NOTE: the length of the array must be at least offset + 4 long.
   * @param bytes The bytes to decode
   * @param offset The offset into the array.
   * @return The float that was encoded
   *
   * @see #encodeFloat(float)
   */
  public static final float decodeFloat(byte [] bytes, int offset){

    return Float.intBitsToFloat(decodeInt(bytes, offset));
  }

  public static final int decodeInt(byte [] bytes, int offset){
    return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
         | ((bytes[offset + 2] & 0xFF) <<  8) |  (bytes[offset + 3] & 0xFF);
  }
}
