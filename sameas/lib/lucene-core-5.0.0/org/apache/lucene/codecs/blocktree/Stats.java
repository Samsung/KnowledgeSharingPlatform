package org.apache.lucene.codecs.blocktree;

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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.lucene.codecs.PostingsReaderBase;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;

/**
 * BlockTree statistics for a single field 
 * returned by {@link FieldReader#getStats()}.
 * @lucene.internal
 */
public class Stats {
  /** How many nodes in the index FST. */
  public long indexNodeCount;

  /** How many arcs in the index FST. */
  public long indexArcCount;

  /** Byte size of the index. */
  public long indexNumBytes;

  /** Total number of terms in the field. */
  public long totalTermCount;

  /** Total number of bytes (sum of term lengths) across all terms in the field. */
  public long totalTermBytes;

  /** The number of normal (non-floor) blocks in the terms file. */
  public int nonFloorBlockCount;

  /** The number of floor blocks (meta-blocks larger than the
   *  allowed {@code maxItemsPerBlock}) in the terms file. */
  public int floorBlockCount;
    
  /** The number of sub-blocks within the floor blocks. */
  public int floorSubBlockCount;

  /** The number of "internal" blocks (that have both
   *  terms and sub-blocks). */
  public int mixedBlockCount;

  /** The number of "leaf" blocks (blocks that have only
   *  terms). */
  public int termsOnlyBlockCount;

  /** The number of "internal" blocks that do not contain
   *  terms (have only sub-blocks). */
  public int subBlocksOnlyBlockCount;

  /** Total number of blocks. */
  public int totalBlockCount;

  /** Number of blocks at each prefix depth. */
  public int[] blockCountByPrefixLen = new int[10];
  private int startBlockCount;
  private int endBlockCount;

  /** Total number of bytes used to store term suffixes. */
  public long totalBlockSuffixBytes;

  /** Total number of bytes used to store term stats (not
   *  including what the {@link PostingsReaderBase}
   *  stores. */
  public long totalBlockStatsBytes;

  /** Total bytes stored by the {@link PostingsReaderBase},
   *  plus the other few vInts stored in the frame. */
  public long totalBlockOtherBytes;

  /** Segment name. */
  public final String segment;

  /** Field name. */
  public final String field;

  Stats(String segment, String field) {
    this.segment = segment;
    this.field = field;
  }

  void startBlock(SegmentTermsEnumFrame frame, boolean isFloor) {
    totalBlockCount++;
    if (isFloor) {
      if (frame.fp == frame.fpOrig) {
        floorBlockCount++;
      }
      floorSubBlockCount++;
    } else {
      nonFloorBlockCount++;
    }

    if (blockCountByPrefixLen.length <= frame.prefix) {
      blockCountByPrefixLen = ArrayUtil.grow(blockCountByPrefixLen, 1+frame.prefix);
    }
    blockCountByPrefixLen[frame.prefix]++;
    startBlockCount++;
    totalBlockSuffixBytes += frame.suffixesReader.length();
    totalBlockStatsBytes += frame.statsReader.length();
  }

  void endBlock(SegmentTermsEnumFrame frame) {
    final int termCount = frame.isLeafBlock ? frame.entCount : frame.state.termBlockOrd;
    final int subBlockCount = frame.entCount - termCount;
    totalTermCount += termCount;
    if (termCount != 0 && subBlockCount != 0) {
      mixedBlockCount++;
    } else if (termCount != 0) {
      termsOnlyBlockCount++;
    } else if (subBlockCount != 0) {
      subBlocksOnlyBlockCount++;
    } else {
      throw new IllegalStateException();
    }
    endBlockCount++;
    final long otherBytes = frame.fpEnd - frame.fp - frame.suffixesReader.length() - frame.statsReader.length();
    assert otherBytes > 0 : "otherBytes=" + otherBytes + " frame.fp=" + frame.fp + " frame.fpEnd=" + frame.fpEnd;
    totalBlockOtherBytes += otherBytes;
  }

  void term(BytesRef term) {
    totalTermBytes += term.length;
  }

  void finish() {
    assert startBlockCount == endBlockCount: "startBlockCount=" + startBlockCount + " endBlockCount=" + endBlockCount;
    assert totalBlockCount == floorSubBlockCount + nonFloorBlockCount: "floorSubBlockCount=" + floorSubBlockCount + " nonFloorBlockCount=" + nonFloorBlockCount + " totalBlockCount=" + totalBlockCount;
    assert totalBlockCount == mixedBlockCount + termsOnlyBlockCount + subBlocksOnlyBlockCount: "totalBlockCount=" + totalBlockCount + " mixedBlockCount=" + mixedBlockCount + " subBlocksOnlyBlockCount=" + subBlocksOnlyBlockCount + " termsOnlyBlockCount=" + termsOnlyBlockCount;
  }

  @Override
  public String toString() {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    PrintStream out;
    try {
      out = new PrintStream(bos, false, IOUtils.UTF_8);
    } catch (UnsupportedEncodingException bogus) {
      throw new RuntimeException(bogus);
    }
      
    out.println("  index FST:");
    out.println("    " + indexNodeCount + " nodes");
    out.println("    " + indexArcCount + " arcs");
    out.println("    " + indexNumBytes + " bytes");
    out.println("  terms:");
    out.println("    " + totalTermCount + " terms");
    out.println("    " + totalTermBytes + " bytes" + (totalTermCount != 0 ? " (" + String.format(Locale.ROOT, "%.1f", ((double) totalTermBytes)/totalTermCount) + " bytes/term)" : ""));
    out.println("  blocks:");
    out.println("    " + totalBlockCount + " blocks");
    out.println("    " + termsOnlyBlockCount + " terms-only blocks");
    out.println("    " + subBlocksOnlyBlockCount + " sub-block-only blocks");
    out.println("    " + mixedBlockCount + " mixed blocks");
    out.println("    " + floorBlockCount + " floor blocks");
    out.println("    " + (totalBlockCount-floorSubBlockCount) + " non-floor blocks");
    out.println("    " + floorSubBlockCount + " floor sub-blocks");
    out.println("    " + totalBlockSuffixBytes + " term suffix bytes" + (totalBlockCount != 0 ? " (" + String.format(Locale.ROOT, "%.1f", ((double) totalBlockSuffixBytes)/totalBlockCount) + " suffix-bytes/block)" : ""));
    out.println("    " + totalBlockStatsBytes + " term stats bytes" + (totalBlockCount != 0 ? " (" + String.format(Locale.ROOT, "%.1f", ((double) totalBlockStatsBytes)/totalBlockCount) + " stats-bytes/block)" : ""));
    out.println("    " + totalBlockOtherBytes + " other bytes" + (totalBlockCount != 0 ? " (" + String.format(Locale.ROOT, "%.1f", ((double) totalBlockOtherBytes)/totalBlockCount) + " other-bytes/block)" : ""));
    if (totalBlockCount != 0) {
      out.println("    by prefix length:");
      int total = 0;
      for(int prefix=0;prefix<blockCountByPrefixLen.length;prefix++) {
        final int blockCount = blockCountByPrefixLen[prefix];
        total += blockCount;
        if (blockCount != 0) {
          out.println("      " + String.format(Locale.ROOT, "%2d", prefix) + ": " + blockCount);
        }
      }
      assert totalBlockCount == total;
    }

    try {
      return bos.toString(IOUtils.UTF_8);
    } catch (UnsupportedEncodingException bogus) {
      throw new RuntimeException(bogus);
    }
  }
}
