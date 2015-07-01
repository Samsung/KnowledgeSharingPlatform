#! /usr/bin/env python

# Copyright(c) 2015, Samsung Electronics Co., Ltd.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#      notice, this list of conditions and the following disclaimer in the
#      documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#      names of its contributors may be used to endorse or promote products
#      derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

HEADER="""// This file has been automatically generated, DO NOT EDIT

package org.apache.lucene.util.packed;

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

import org.apache.lucene.store.DataInput;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.IOException;
import java.util.Arrays;

"""

TYPES = {8: "byte", 16: "short", 32: "int", 64: "long"}
MASKS = {8: " & 0xFFL", 16: " & 0xFFFFL", 32: " & 0xFFFFFFFFL", 64: ""}
CASTS = {8: "(byte) ", 16: "(short) ", 32: "(int) ", 64: ""}

if __name__ == '__main__':
  for bpv in TYPES.keys():
    type
    f = open("Direct%d.java" %bpv, 'w')
    f.write(HEADER)
    f.write("""/**
 * Direct wrapping of %d-bits values to a backing array.
 * @lucene.internal
 */\n""" %bpv)
    f.write("final class Direct%d extends PackedInts.MutableImpl {\n" %bpv)
    f.write("  final %s[] values;\n\n" %TYPES[bpv])

    f.write("  Direct%d(int valueCount) {\n" %bpv)
    f.write("    super(valueCount, %d);\n" %bpv)
    f.write("    values = new %s[valueCount];\n" %TYPES[bpv])
    f.write("  }\n\n")

    f.write("  Direct%d(int packedIntsVersion, DataInput in, int valueCount) throws IOException {\n" %bpv)
    f.write("    this(valueCount);\n")
    if bpv == 8:
      f.write("    in.readBytes(values, 0, valueCount);\n")
    else:
      f.write("    for (int i = 0; i < valueCount; ++i) {\n")
      f.write("      values[i] = in.read%s();\n" %TYPES[bpv].title())
      f.write("    }\n")
    if bpv != 64:
      f.write("    // because packed ints have not always been byte-aligned\n")
      f.write("    final int remaining = (int) (PackedInts.Format.PACKED.byteCount(packedIntsVersion, valueCount, %d) - %dL * valueCount);\n" %(bpv, bpv / 8))
      f.write("    for (int i = 0; i < remaining; ++i) {\n")
      f.write("      in.readByte();\n")
      f.write("    }\n")
    f.write("  }\n")

    f.write("""
  @Override
  public long get(final int index) {
    return values[index]%s;
  }

  @Override
  public void set(final int index, final long value) {
    values[index] = %s(value);
  }

  @Override
  public long ramBytesUsed() {
    return RamUsageEstimator.alignObjectSize(
        RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + 2 * RamUsageEstimator.NUM_BYTES_INT     // valueCount,bitsPerValue
        + RamUsageEstimator.NUM_BYTES_OBJECT_REF) // values ref
        + RamUsageEstimator.sizeOf(values);
  }

  @Override
  public void clear() {
    Arrays.fill(values, %s0L);
  }
""" %(MASKS[bpv], CASTS[bpv], CASTS[bpv]))

    if bpv == 64:
      f.write("""
  @Override
  public int get(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int gets = Math.min(valueCount - index, len);
    System.arraycopy(values, index, arr, off, gets);
    return gets;
  }

  @Override
  public int set(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int sets = Math.min(valueCount - index, len);
    System.arraycopy(arr, off, values, index, sets);
    return sets;
  }

  @Override
  public void fill(int fromIndex, int toIndex, long val) {
    Arrays.fill(values, fromIndex, toIndex, val);
  }
""")
    else:
      f.write("""
  @Override
  public int get(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int gets = Math.min(valueCount - index, len);
    for (int i = index, o = off, end = index + gets; i < end; ++i, ++o) {
      arr[o] = values[i]%s;
    }
    return gets;
  }

  @Override
  public int set(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int sets = Math.min(valueCount - index, len);
    for (int i = index, o = off, end = index + sets; i < end; ++i, ++o) {
      values[i] = %sarr[o];
    }
    return sets;
  }

  @Override
  public void fill(int fromIndex, int toIndex, long val) {
    assert val == (val%s);
    Arrays.fill(values, fromIndex, toIndex, %sval);
  }
""" %(MASKS[bpv], CASTS[bpv], MASKS[bpv], CASTS[bpv]))

    f.write("}\n")

    f.close()
