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

import org.apache.lucene.codecs.MultiLevelSkipListReader;
import org.apache.lucene.store.IndexInput;

/**
 * Implements the skip list reader for block postings format
 * that stores positions and payloads.
 * 
 * Although this skipper uses MultiLevelSkipListReader as an interface, 
 * its definition of skip position will be a little different. 
 *
 * For example, when skipInterval = blockSize = 3, df = 2*skipInterval = 6, 
 * 
 * 0 1 2 3 4 5
 * d d d d d d    (posting list)
 *     ^     ^    (skip point in MultiLeveSkipWriter)
 *       ^        (skip point in Lucene50SkipWriter)
 *
 * In this case, MultiLevelSkipListReader will use the last document as a skip point, 
 * while Lucene50SkipReader should assume no skip point will comes. 
 *
 * If we use the interface directly in Lucene50SkipReader, it may silly try to read 
 * another skip data after the only skip point is loaded. 
 *
 * To illustrate this, we can call skipTo(d[5]), since skip point d[3] has smaller docId,
 * and numSkipped+blockSize== df, the MultiLevelSkipListReader will assume the skip list
 * isn't exhausted yet, and try to load a non-existed skip point
 *
 * Therefore, we'll trim df before passing it to the interface. see trim(int)
 *
 */
final class Lucene50SkipReader extends MultiLevelSkipListReader {
  private final int blockSize;

  private long docPointer[];
  private long posPointer[];
  private long payPointer[];
  private int posBufferUpto[];
  private int payloadByteUpto[];

  private long lastPosPointer;
  private long lastPayPointer;
  private int lastPayloadByteUpto;
  private long lastDocPointer;
  private int lastPosBufferUpto;

  public Lucene50SkipReader(IndexInput skipStream, int maxSkipLevels, int blockSize, boolean hasPos, boolean hasOffsets, boolean hasPayloads) {
    super(skipStream, maxSkipLevels, blockSize, 8);
    this.blockSize = blockSize;
    docPointer = new long[maxSkipLevels];
    if (hasPos) {
      posPointer = new long[maxSkipLevels];
      posBufferUpto = new int[maxSkipLevels];
      if (hasPayloads) {
        payloadByteUpto = new int[maxSkipLevels];
      } else {
        payloadByteUpto = null;
      }
      if (hasOffsets || hasPayloads) {
        payPointer = new long[maxSkipLevels];
      } else {
        payPointer = null;
      }
    } else {
      posPointer = null;
    }
  }

  /**
   * Trim original docFreq to tell skipReader read proper number of skip points.
   *
   * Since our definition in Lucene50Skip* is a little different from MultiLevelSkip*
   * This trimmed docFreq will prevent skipReader from:
   * 1. silly reading a non-existed skip point after the last block boundary
   * 2. moving into the vInt block
   *
   */
  protected int trim(int df) {
    return df % blockSize == 0? df - 1: df;
  }

  public void init(long skipPointer, long docBasePointer, long posBasePointer, long payBasePointer, int df) {
    super.init(skipPointer, trim(df));
    lastDocPointer = docBasePointer;
    lastPosPointer = posBasePointer;
    lastPayPointer = payBasePointer;

    Arrays.fill(docPointer, docBasePointer);
    if (posPointer != null) {
      Arrays.fill(posPointer, posBasePointer);
      if (payPointer != null) {
        Arrays.fill(payPointer, payBasePointer);
      }
    } else {
      assert posBasePointer == 0;
    }
  }

  /** Returns the doc pointer of the doc to which the last call of 
   * {@link MultiLevelSkipListReader#skipTo(int)} has skipped.  */
  public long getDocPointer() {
    return lastDocPointer;
  }

  public long getPosPointer() {
    return lastPosPointer;
  }

  public int getPosBufferUpto() {
    return lastPosBufferUpto;
  }

  public long getPayPointer() {
    return lastPayPointer;
  }

  public int getPayloadByteUpto() {
    return lastPayloadByteUpto;
  }

  public int getNextSkipDoc() {
    return skipDoc[0];
  }

  @Override
  protected void seekChild(int level) throws IOException {
    super.seekChild(level);
    docPointer[level] = lastDocPointer;
    if (posPointer != null) {
      posPointer[level] = lastPosPointer;
      posBufferUpto[level] = lastPosBufferUpto;
      if (payloadByteUpto != null) {
        payloadByteUpto[level] = lastPayloadByteUpto;
      }
      if (payPointer != null) {
        payPointer[level] = lastPayPointer;
      }
    }
  }
  
  @Override
  protected void setLastSkipData(int level) {
    super.setLastSkipData(level);
    lastDocPointer = docPointer[level];

    if (posPointer != null) {
      lastPosPointer = posPointer[level];
      lastPosBufferUpto = posBufferUpto[level];
      if (payPointer != null) {
        lastPayPointer = payPointer[level];
      }
      if (payloadByteUpto != null) {
        lastPayloadByteUpto = payloadByteUpto[level];
      }
    }
  }

  @Override
  protected int readSkipData(int level, IndexInput skipStream) throws IOException {
    int delta = skipStream.readVInt();
    docPointer[level] += skipStream.readVLong();

    if (posPointer != null) {
      posPointer[level] += skipStream.readVLong();
      posBufferUpto[level] = skipStream.readVInt();

      if (payloadByteUpto != null) {
        payloadByteUpto[level] = skipStream.readVInt();
      }

      if (payPointer != null) {
        payPointer[level] += skipStream.readVLong();
      }
    }
    return delta;
  }
}
