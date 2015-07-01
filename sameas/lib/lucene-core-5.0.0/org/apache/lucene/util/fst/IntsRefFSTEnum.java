package org.apache.lucene.util.fst;

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

import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.IntsRef;

import java.io.IOException;

/** Enumerates all input (IntsRef) + output pairs in an
 *  FST.
 *
  * @lucene.experimental
*/

public final class IntsRefFSTEnum<T> extends FSTEnum<T> {
  private final IntsRef current = new IntsRef(10);
  private final InputOutput<T> result = new InputOutput<>();
  private IntsRef target;

  /** Holds a single input (IntsRef) + output pair. */
  public static class InputOutput<T> {
    public IntsRef input;
    public T output;
  }

  /** doFloor controls the behavior of advance: if it's true
   *  doFloor is true, advance positions to the biggest
   *  term before target.  */
  public IntsRefFSTEnum(FST<T> fst) {
    super(fst);
    result.input = current;
    current.offset = 1;
  }

  public InputOutput<T> current() {
    return result;
  }

  public InputOutput<T> next() throws IOException {
    //System.out.println("  enum.next");
    doNext();
    return setResult();
  }

  /** Seeks to smallest term that's &gt;= target. */
  public InputOutput<T> seekCeil(IntsRef target) throws IOException {
    this.target = target;
    targetLength = target.length;
    super.doSeekCeil();
    return setResult();
  }

  /** Seeks to biggest term that's &lt;= target. */
  public InputOutput<T> seekFloor(IntsRef target) throws IOException {
    this.target = target;
    targetLength = target.length;
    super.doSeekFloor();
    return setResult();
  }

  /** Seeks to exactly this term, returning null if the term
   *  doesn't exist.  This is faster than using {@link
   *  #seekFloor} or {@link #seekCeil} because it
   *  short-circuits as soon the match is not found. */
  public InputOutput<T> seekExact(IntsRef target) throws IOException {
    this.target = target;
    targetLength = target.length;
    if (super.doSeekExact()) {
      assert upto == 1+target.length;
      return setResult();
    } else {
      return null;
    }
  }

  @Override
  protected int getTargetLabel() {
    if (upto-1 == target.length) {
      return FST.END_LABEL;
    } else {
      return target.ints[target.offset + upto - 1];
    }
  }

  @Override
  protected int getCurrentLabel() {
    // current.offset fixed at 1
    return current.ints[upto];
  }

  @Override
  protected void setCurrentLabel(int label) {
    current.ints[upto] = label;
  }

  @Override
  protected void grow() {
    current.ints = ArrayUtil.grow(current.ints, upto+1);
  }

  private InputOutput<T> setResult() {
    if (upto == 0) {
      return null;
    } else {
      current.length = upto-1;
      result.output = output[upto];
      return result;
    }
  }
}
