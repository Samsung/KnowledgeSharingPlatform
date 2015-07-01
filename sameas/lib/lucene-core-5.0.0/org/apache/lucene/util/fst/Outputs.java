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

import java.io.IOException;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.Accountable;

/**
 * Represents the outputs for an FST, providing the basic
 * algebra required for building and traversing the FST.
 *
 * <p>Note that any operation that returns NO_OUTPUT must
 * return the same singleton object from {@link
 * #getNoOutput}.</p>
 *
 * @lucene.experimental
 */

public abstract class Outputs<T> {

  // TODO: maybe change this API to allow for re-use of the
  // output instances -- this is an insane amount of garbage
  // (new object per byte/char/int) if eg used during
  // analysis

  /** Eg common("foobar", "food") -&gt; "foo" */
  public abstract T common(T output1, T output2);

  /** Eg subtract("foobar", "foo") -&gt; "bar" */
  public abstract T subtract(T output, T inc);

  /** Eg add("foo", "bar") -&gt; "foobar" */
  public abstract T add(T prefix, T output);

  /** Encode an output value into a {@link DataOutput}. */
  public abstract void write(T output, DataOutput out) throws IOException;

  /** Encode an final node output value into a {@link
   *  DataOutput}.  By default this just calls {@link #write(Object,
   *  DataOutput)}. */
  public void writeFinalOutput(T output, DataOutput out) throws IOException {
    write(output, out);
  }

  /** Decode an output value previously written with {@link
   *  #write(Object, DataOutput)}. */
  public abstract T read(DataInput in) throws IOException;

  /** Skip the output; defaults to just calling {@link #read}
   *  and discarding the result. */
  public void skipOutput(DataInput in) throws IOException {
    read(in);
  }

  /** Decode an output value previously written with {@link
   *  #writeFinalOutput(Object, DataOutput)}.  By default this
   *  just calls {@link #read(DataInput)}. */
  public T readFinalOutput(DataInput in) throws IOException {
    return read(in);
  }
  
  /** Skip the output previously written with {@link #writeFinalOutput};
   *  defaults to just calling {@link #readFinalOutput} and discarding
   *  the result. */
  public void skipFinalOutput(DataInput in) throws IOException {
    skipOutput(in);
  }

  /** NOTE: this output is compared with == so you must
   *  ensure that all methods return the single object if
   *  it's really no output */
  public abstract T getNoOutput();

  public abstract String outputToString(T output);

  // TODO: maybe make valid(T output) public...?  for asserts

  public T merge(T first, T second) {
    throw new UnsupportedOperationException();
  }

  /** Return memory usage for the provided output.
   *  @see Accountable */
  public abstract long ramBytesUsed(T output);
}
