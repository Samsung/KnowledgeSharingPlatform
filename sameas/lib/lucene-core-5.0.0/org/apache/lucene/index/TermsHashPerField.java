package org.apache.lucene.index;

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

import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.ByteBlockPool;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;
import org.apache.lucene.util.Counter;
import org.apache.lucene.util.IntBlockPool;
import org.apache.lucene.util.BytesRefHash.BytesStartArray;

abstract class TermsHashPerField implements Comparable<TermsHashPerField> {
  private static final int HASH_INIT_SIZE = 4;

  final TermsHash termsHash;

  final TermsHashPerField nextPerField;
  protected final DocumentsWriterPerThread.DocState docState;
  protected final FieldInvertState fieldState;
  TermToBytesRefAttribute termAtt;
  BytesRef termBytesRef;

  // Copied from our perThread
  final IntBlockPool intPool;
  final ByteBlockPool bytePool;
  final ByteBlockPool termBytePool;

  final int streamCount;
  final int numPostingInt;

  protected final FieldInfo fieldInfo;

  final BytesRefHash bytesHash;

  ParallelPostingsArray postingsArray;
  private final Counter bytesUsed;

  /** streamCount: how many streams this field stores per term.
   * E.g. doc(+freq) is 1 stream, prox+offset is a second. */

  public TermsHashPerField(int streamCount, FieldInvertState fieldState, TermsHash termsHash, TermsHashPerField nextPerField, FieldInfo fieldInfo) {
    intPool = termsHash.intPool;
    bytePool = termsHash.bytePool;
    termBytePool = termsHash.termBytePool;
    docState = termsHash.docState;
    this.termsHash = termsHash;
    bytesUsed = termsHash.bytesUsed;
    this.fieldState = fieldState;
    this.streamCount = streamCount;
    numPostingInt = 2*streamCount;
    this.fieldInfo = fieldInfo;
    this.nextPerField = nextPerField;
    PostingsBytesStartArray byteStarts = new PostingsBytesStartArray(this, bytesUsed);
    bytesHash = new BytesRefHash(termBytePool, HASH_INIT_SIZE, byteStarts);
  }

  void reset() {
    bytesHash.clear(false);
    if (nextPerField != null) {
      nextPerField.reset();
    }
  }

  public void initReader(ByteSliceReader reader, int termID, int stream) {
    assert stream < streamCount;
    int intStart = postingsArray.intStarts[termID];
    final int[] ints = intPool.buffers[intStart >> IntBlockPool.INT_BLOCK_SHIFT];
    final int upto = intStart & IntBlockPool.INT_BLOCK_MASK;
    reader.init(bytePool,
                postingsArray.byteStarts[termID]+stream*ByteBlockPool.FIRST_LEVEL_SIZE,
                ints[upto+stream]);
  }

  int[] sortedTermIDs;

  /** Collapse the hash table and sort in-place; also sets
   * this.sortedTermIDs to the results */
  public int[] sortPostings() {
    sortedTermIDs = bytesHash.sort(BytesRef.getUTF8SortedAsUnicodeComparator());
    return sortedTermIDs;
  }

  private boolean doNextCall;

  // Secondary entry point (for 2nd & subsequent TermsHash),
  // because token text has already been "interned" into
  // textStart, so we hash by textStart.  term vectors use
  // this API.
  public void add(int textStart) throws IOException {
    int termID = bytesHash.addByPoolOffset(textStart);
    if (termID >= 0) {      // New posting
      // First time we are seeing this token since we last
      // flushed the hash.
      // Init stream slices
      if (numPostingInt + intPool.intUpto > IntBlockPool.INT_BLOCK_SIZE) {
        intPool.nextBuffer();
      }

      if (ByteBlockPool.BYTE_BLOCK_SIZE - bytePool.byteUpto < numPostingInt*ByteBlockPool.FIRST_LEVEL_SIZE) {
        bytePool.nextBuffer();
      }

      intUptos = intPool.buffer;
      intUptoStart = intPool.intUpto;
      intPool.intUpto += streamCount;

      postingsArray.intStarts[termID] = intUptoStart + intPool.intOffset;

      for(int i=0;i<streamCount;i++) {
        final int upto = bytePool.newSlice(ByteBlockPool.FIRST_LEVEL_SIZE);
        intUptos[intUptoStart+i] = upto + bytePool.byteOffset;
      }
      postingsArray.byteStarts[termID] = intUptos[intUptoStart];

      newTerm(termID);

    } else {
      termID = (-termID)-1;
      int intStart = postingsArray.intStarts[termID];
      intUptos = intPool.buffers[intStart >> IntBlockPool.INT_BLOCK_SHIFT];
      intUptoStart = intStart & IntBlockPool.INT_BLOCK_MASK;
      addTerm(termID);
    }
  }

  /** Called once per inverted token.  This is the primary
   *  entry point (for first TermsHash); postings use this
   *  API. */
  void add() throws IOException {

    termAtt.fillBytesRef();

    // We are first in the chain so we must "intern" the
    // term text into textStart address
    // Get the text & hash of this term.
    int termID = bytesHash.add(termBytesRef);
      
    //System.out.println("add term=" + termBytesRef.utf8ToString() + " doc=" + docState.docID + " termID=" + termID);

    if (termID >= 0) {// New posting
      bytesHash.byteStart(termID);
      // Init stream slices
      if (numPostingInt + intPool.intUpto > IntBlockPool.INT_BLOCK_SIZE) {
        intPool.nextBuffer();
      }

      if (ByteBlockPool.BYTE_BLOCK_SIZE - bytePool.byteUpto < numPostingInt*ByteBlockPool.FIRST_LEVEL_SIZE) {
        bytePool.nextBuffer();
      }

      intUptos = intPool.buffer;
      intUptoStart = intPool.intUpto;
      intPool.intUpto += streamCount;

      postingsArray.intStarts[termID] = intUptoStart + intPool.intOffset;

      for(int i=0;i<streamCount;i++) {
        final int upto = bytePool.newSlice(ByteBlockPool.FIRST_LEVEL_SIZE);
        intUptos[intUptoStart+i] = upto + bytePool.byteOffset;
      }
      postingsArray.byteStarts[termID] = intUptos[intUptoStart];

      newTerm(termID);

    } else {
      termID = (-termID)-1;
      int intStart = postingsArray.intStarts[termID];
      intUptos = intPool.buffers[intStart >> IntBlockPool.INT_BLOCK_SHIFT];
      intUptoStart = intStart & IntBlockPool.INT_BLOCK_MASK;
      addTerm(termID);
    }

    if (doNextCall) {
      nextPerField.add(postingsArray.textStarts[termID]);
    }
  }

  int[] intUptos;
  int intUptoStart;

  void writeByte(int stream, byte b) {
    int upto = intUptos[intUptoStart+stream];
    byte[] bytes = bytePool.buffers[upto >> ByteBlockPool.BYTE_BLOCK_SHIFT];
    assert bytes != null;
    int offset = upto & ByteBlockPool.BYTE_BLOCK_MASK;
    if (bytes[offset] != 0) {
      // End of slice; allocate a new one
      offset = bytePool.allocSlice(bytes, offset);
      bytes = bytePool.buffer;
      intUptos[intUptoStart+stream] = offset + bytePool.byteOffset;
    }
    bytes[offset] = b;
    (intUptos[intUptoStart+stream])++;
  }

  public void writeBytes(int stream, byte[] b, int offset, int len) {
    // TODO: optimize
    final int end = offset + len;
    for(int i=offset;i<end;i++)
      writeByte(stream, b[i]);
  }

  void writeVInt(int stream, int i) {
    assert stream < streamCount;
    while ((i & ~0x7F) != 0) {
      writeByte(stream, (byte)((i & 0x7f) | 0x80));
      i >>>= 7;
    }
    writeByte(stream, (byte) i);
  }

  private static final class PostingsBytesStartArray extends BytesStartArray {

    private final TermsHashPerField perField;
    private final Counter bytesUsed;

    private PostingsBytesStartArray(
        TermsHashPerField perField, Counter bytesUsed) {
      this.perField = perField;
      this.bytesUsed = bytesUsed;
    }

    @Override
    public int[] init() {
      if (perField.postingsArray == null) {
        perField.postingsArray = perField.createPostingsArray(2);
        perField.newPostingsArray();
        bytesUsed.addAndGet(perField.postingsArray.size * perField.postingsArray.bytesPerPosting());
      }
      return perField.postingsArray.textStarts;
    }

    @Override
    public int[] grow() {
      ParallelPostingsArray postingsArray = perField.postingsArray;
      final int oldSize = perField.postingsArray.size;
      postingsArray = perField.postingsArray = postingsArray.grow();
      perField.newPostingsArray();
      bytesUsed.addAndGet((postingsArray.bytesPerPosting() * (postingsArray.size - oldSize)));
      return postingsArray.textStarts;
    }

    @Override
    public int[] clear() {
      if (perField.postingsArray != null) {
        bytesUsed.addAndGet(-(perField.postingsArray.size * perField.postingsArray.bytesPerPosting()));
        perField.postingsArray = null;
        perField.newPostingsArray();
      }
      return null;
    }

    @Override
    public Counter bytesUsed() {
      return bytesUsed;
    }
  }

  @Override
  public int compareTo(TermsHashPerField other) {
    return fieldInfo.name.compareTo(other.fieldInfo.name);
  }

  /** Finish adding all instances of this field to the
   *  current document. */
  void finish() throws IOException {
    if (nextPerField != null) {
      nextPerField.finish();
    }
  }

  /** Start adding a new field instance; first is true if
   *  this is the first time this field name was seen in the
   *  document. */
  boolean start(IndexableField field, boolean first) {
    termAtt = fieldState.termAttribute;
    // EmptyTokenStream can have null term att
    if (termAtt != null) {
      termBytesRef = termAtt.getBytesRef();
    }
    if (nextPerField != null) {
      doNextCall = nextPerField.start(field, first);
    }

    return true;
  }

  /** Called when a term is seen for the first time. */
  abstract void newTerm(int termID) throws IOException;

  /** Called when a previously seen term is seen again. */
  abstract void addTerm(int termID) throws IOException;

  /** Called when the postings array is initialized or
   *  resized. */
  abstract void newPostingsArray();

  /** Creates a new postings array of the specified size. */
  abstract ParallelPostingsArray createPostingsArray(int size);
}
