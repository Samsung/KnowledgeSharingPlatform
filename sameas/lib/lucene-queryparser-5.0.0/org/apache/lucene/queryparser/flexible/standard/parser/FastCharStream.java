// FastCharStream.java
package org.apache.lucene.queryparser.flexible.standard.parser;

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

import java.io.*;

/** An efficient implementation of JavaCC's CharStream interface.  <p>Note that
 * this does not do line-number counting, but instead keeps track of the
 * character position of the token in the input, as required by Lucene's {@link
 * org.apache.lucene.analysis.Token} API. 
 * */
public final class FastCharStream implements CharStream {
  char[] buffer = null;

  int bufferLength = 0;          // end of valid chars
  int bufferPosition = 0;        // next char to read

  int tokenStart = 0;          // offset in buffer
  int bufferStart = 0;          // position in file of buffer

  Reader input;            // source of chars

  /** Constructs from a Reader. */
  public FastCharStream(Reader r) {
    input = r;
  }

  @Override
  public final char readChar() throws IOException {
    if (bufferPosition >= bufferLength)
      refill();
    return buffer[bufferPosition++];
  }

  private final void refill() throws IOException {
    int newPosition = bufferLength - tokenStart;

    if (tokenStart == 0) {        // token won't fit in buffer
      if (buffer == null) {        // first time: alloc buffer
        buffer = new char[2048];
      } else if (bufferLength == buffer.length) { // grow buffer
        char[] newBuffer = new char[buffer.length * 2];
        System.arraycopy(buffer, 0, newBuffer, 0, bufferLength);
        buffer = newBuffer;
      }
    } else {            // shift token to front
      System.arraycopy(buffer, tokenStart, buffer, 0, newPosition);
    }

    bufferLength = newPosition;        // update state
    bufferPosition = newPosition;
    bufferStart += tokenStart;
    tokenStart = 0;

    int charsRead =          // fill space in buffer
      input.read(buffer, newPosition, buffer.length-newPosition);
    if (charsRead == -1)
      throw new IOException("read past eof");
    else
      bufferLength += charsRead;
  }

  @Override
  public final char BeginToken() throws IOException {
    tokenStart = bufferPosition;
    return readChar();
  }

  @Override
  public final void backup(int amount) {
    bufferPosition -= amount;
  }

  @Override
  public final String GetImage() {
    return new String(buffer, tokenStart, bufferPosition - tokenStart);
  }

  @Override
  public final char[] GetSuffix(int len) {
    char[] value = new char[len];
    System.arraycopy(buffer, bufferPosition - len, value, 0, len);
    return value;
  }

  @Override
  public final void Done() {
    try {
      input.close();
    } catch (IOException e) {
    }
  }

  @Override
  public final int getColumn() {
    return bufferStart + bufferPosition;
  }
  @Override
  public final int getLine() {
    return 1;
  }
  @Override
  public final int getEndColumn() {
    return bufferStart + bufferPosition;
  }
  @Override
  public final int getEndLine() {
    return 1;
  }
  @Override
  public final int getBeginColumn() {
    return bufferStart + tokenStart;
  }
  @Override
  public final int getBeginLine() {
    return 1;
  }
}
