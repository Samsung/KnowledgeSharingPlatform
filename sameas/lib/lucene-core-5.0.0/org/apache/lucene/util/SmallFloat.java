package org.apache.lucene.util;
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


/** Floating point numbers smaller than 32 bits.
 *
 * @lucene.internal
 */
public class SmallFloat {
  
  /** No instance */
  private SmallFloat() {}

  /** Converts a 32 bit float to an 8 bit float.
   * <br>Values less than zero are all mapped to zero.
   * <br>Values are truncated (rounded down) to the nearest 8 bit value.
   * <br>Values between zero and the smallest representable value
   *  are rounded up.
   *
   * @param f the 32 bit float to be converted to an 8 bit float (byte)
   * @param numMantissaBits the number of mantissa bits to use in the byte, with the remainder to be used in the exponent
   * @param zeroExp the zero-point in the range of exponent values
   * @return the 8 bit float representation
   */
  public static byte floatToByte(float f, int numMantissaBits, int zeroExp) {
    // Adjustment from a float zero exponent to our zero exponent,
    // shifted over to our exponent position.
    int fzero = (63-zeroExp)<<numMantissaBits;
    int bits = Float.floatToRawIntBits(f);
    int smallfloat = bits >> (24-numMantissaBits);
    if (smallfloat <= fzero) {
      return (bits<=0) ?
        (byte)0   // negative numbers and zero both map to 0 byte
       :(byte)1;  // underflow is mapped to smallest non-zero number.
    } else if (smallfloat >= fzero + 0x100) {
      return -1;  // overflow maps to largest number
    } else {
      return (byte)(smallfloat - fzero);
    }
  }

  /** Converts an 8 bit float to a 32 bit float. */
  public static float byteToFloat(byte b, int numMantissaBits, int zeroExp) {
    // on Java1.5 & 1.6 JVMs, prebuilding a decoding array and doing a lookup
    // is only a little bit faster (anywhere from 0% to 7%)
    if (b == 0) return 0.0f;
    int bits = (b&0xff) << (24-numMantissaBits);
    bits += (63-zeroExp) << 24;
    return Float.intBitsToFloat(bits);
  }


  //
  // Some specializations of the generic functions follow.
  // The generic functions are just as fast with current (1.5)
  // -server JVMs, but still slower with client JVMs.
  //

  /** floatToByte(b, mantissaBits=3, zeroExponent=15)
   * <br>smallest non-zero value = 5.820766E-10
   * <br>largest value = 7.5161928E9
   * <br>epsilon = 0.125
   */
  public static byte floatToByte315(float f) {
    int bits = Float.floatToRawIntBits(f);
    int smallfloat = bits >> (24-3);
    if (smallfloat <= ((63-15)<<3)) {
      return (bits<=0) ? (byte)0 : (byte)1;
    }
    if (smallfloat >= ((63-15)<<3) + 0x100) {
      return -1;
    }
    return (byte)(smallfloat - ((63-15)<<3));
 }

  /** byteToFloat(b, mantissaBits=3, zeroExponent=15) */
  public static float byte315ToFloat(byte b) {
    // on Java1.5 & 1.6 JVMs, prebuilding a decoding array and doing a lookup
    // is only a little bit faster (anywhere from 0% to 7%)
    if (b == 0) return 0.0f;
    int bits = (b&0xff) << (24-3);
    bits += (63-15) << 24;
    return Float.intBitsToFloat(bits);
  }


  /** floatToByte(b, mantissaBits=5, zeroExponent=2)
   * <br>smallest nonzero value = 0.033203125
   * <br>largest value = 1984.0
   * <br>epsilon = 0.03125
   */
  public static byte floatToByte52(float f) {
    int bits = Float.floatToRawIntBits(f);
    int smallfloat = bits >> (24-5);
    if (smallfloat <= (63-2)<<5) {
      return (bits<=0) ? (byte)0 : (byte)1;
    }
    if (smallfloat >= ((63-2)<<5) + 0x100) {
      return -1;
    }
    return (byte)(smallfloat - ((63-2)<<5));
  }

  /** byteToFloat(b, mantissaBits=5, zeroExponent=2) */
  public static float byte52ToFloat(byte b) {
    // on Java1.5 & 1.6 JVMs, prebuilding a decoding array and doing a lookup
    // is only a little bit faster (anywhere from 0% to 7%)
    if (b == 0) return 0.0f;
    int bits = (b&0xff) << (24-5);
    bits += (63-2) << 24;
    return Float.intBitsToFloat(bits);
  }
}
