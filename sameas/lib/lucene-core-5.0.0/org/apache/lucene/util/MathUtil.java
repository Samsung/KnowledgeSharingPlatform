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

import java.math.BigInteger;

/**
 * Math static utility methods.
 */
public final class MathUtil {

  // No instance:
  private MathUtil() {
  }

  /**
   * Returns {@code x <= 0 ? 0 : Math.floor(Math.log(x) / Math.log(base))}
   * @param base must be {@code > 1}
   */
  public static int log(long x, int base) {
    if (base <= 1) {
      throw new IllegalArgumentException("base must be > 1");
    }
    int ret = 0;
    while (x >= base) {
      x /= base;
      ret++;
    }
    return ret;
  }

  /**
   * Calculates logarithm in a given base with doubles.
   */
  public static double log(double base, double x) {
    return Math.log(x) / Math.log(base);
  }

  /** Return the greatest common divisor of <code>a</code> and <code>b</code>,
   *  consistently with {@link BigInteger#gcd(BigInteger)}.
   *  <p><b>NOTE</b>: A greatest common divisor must be positive, but
   *  <code>2^64</code> cannot be expressed as a long although it
   *  is the GCD of {@link Long#MIN_VALUE} and <code>0</code> and the GCD of
   *  {@link Long#MIN_VALUE} and {@link Long#MIN_VALUE}. So in these 2 cases,
   *  and only them, this method will return {@link Long#MIN_VALUE}. */
  // see http://en.wikipedia.org/wiki/Binary_GCD_algorithm#Iterative_version_in_C.2B.2B_using_ctz_.28count_trailing_zeros.29
  public static long gcd(long a, long b) {
    a = Math.abs(a);
    b = Math.abs(b);
    if (a == 0) {
      return b;
    } else if (b == 0) {
      return a;
    }
    final int commonTrailingZeros = Long.numberOfTrailingZeros(a | b);
    a >>>= Long.numberOfTrailingZeros(a);
    while (true) {
      b >>>= Long.numberOfTrailingZeros(b);
      if (a == b) {
        break;
      } else if (a > b || a == Long.MIN_VALUE) { // MIN_VALUE is treated as 2^64
        final long tmp = a;
        a = b;
        b = tmp;
      }
      if (a == 1) {
        break;
      }
      b -= a;
    }
    return a << commonTrailingZeros;
  }


  /**
   * Calculates inverse hyperbolic sine of a {@code double} value.
   * <p>
   * Special cases:
   * <ul>
   *    <li>If the argument is NaN, then the result is NaN.
   *    <li>If the argument is zero, then the result is a zero with the same sign as the argument.
   *    <li>If the argument is infinite, then the result is infinity with the same sign as the argument.
   * </ul>
   */
  public static double asinh(double a) {
    final double sign;
    // check the sign bit of the raw representation to handle -0
    if (Double.doubleToRawLongBits(a) < 0) {
      a = Math.abs(a);
      sign = -1.0d;
    } else {
      sign = 1.0d;
    }

    return sign * Math.log(Math.sqrt(a * a + 1.0d) + a);
  }

  /**
   * Calculates inverse hyperbolic cosine of a {@code double} value.
   * <p>
   * Special cases:
   * <ul>
   *    <li>If the argument is NaN, then the result is NaN.
   *    <li>If the argument is +1, then the result is a zero.
   *    <li>If the argument is positive infinity, then the result is positive infinity.
   *    <li>If the argument is less than 1, then the result is NaN.
   * </ul>
   */
  public static double acosh(double a) {
    return Math.log(Math.sqrt(a * a - 1.0d) + a);
  }

  /**
   * Calculates inverse hyperbolic tangent of a {@code double} value.
   * <p>
   * Special cases:
   * <ul>
   *    <li>If the argument is NaN, then the result is NaN.
   *    <li>If the argument is zero, then the result is a zero with the same sign as the argument.
   *    <li>If the argument is +1, then the result is positive infinity.
   *    <li>If the argument is -1, then the result is negative infinity.
   *    <li>If the argument's absolute value is greater than 1, then the result is NaN.
   * </ul>
   */
  public static double atanh(double a) {
    final double mult;
    // check the sign bit of the raw representation to handle -0
    if (Double.doubleToRawLongBits(a) < 0) {
      a = Math.abs(a);
      mult = -0.5d;
    } else {
      mult = 0.5d;
    }
    return mult * Math.log((1.0d + a) / (1.0d - a));
  }


}
