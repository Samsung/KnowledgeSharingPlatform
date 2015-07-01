package org.apache.lucene.analysis.path;
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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 * Tokenizer for path-like hierarchies.
 * <p>
 * Take something like:
 *
 * <pre>
 *  /something/something/else
 * </pre>
 *
 * and make:
 *
 * <pre>
 *  /something
 *  /something/something
 *  /something/something/else
 * </pre>
 */
public class PathHierarchyTokenizer extends Tokenizer {

  public PathHierarchyTokenizer() {
    this( DEFAULT_BUFFER_SIZE, DEFAULT_DELIMITER, DEFAULT_DELIMITER, DEFAULT_SKIP);
  }

  public PathHierarchyTokenizer( int skip) {
    this( DEFAULT_BUFFER_SIZE, DEFAULT_DELIMITER, DEFAULT_DELIMITER, skip);
  }

  public PathHierarchyTokenizer( int bufferSize, char delimiter) {
    this( bufferSize, delimiter, delimiter, DEFAULT_SKIP);
  }

  public PathHierarchyTokenizer(char delimiter, char replacement) {
    this(DEFAULT_BUFFER_SIZE, delimiter, replacement, DEFAULT_SKIP);
  }

  public PathHierarchyTokenizer(char delimiter, char replacement, int skip) {
    this(DEFAULT_BUFFER_SIZE, delimiter, replacement, skip);
  }

  public PathHierarchyTokenizer(AttributeFactory factory, char delimiter, char replacement, int skip) {
    this(factory, DEFAULT_BUFFER_SIZE, delimiter, replacement, skip);
  }

  public PathHierarchyTokenizer(int bufferSize, char delimiter, char replacement, int skip) {
    this(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, bufferSize, delimiter, replacement, skip);
  }

  public PathHierarchyTokenizer
      (AttributeFactory factory, int bufferSize, char delimiter, char replacement, int skip) {
    super(factory);
    if (bufferSize < 0) {
      throw new IllegalArgumentException("bufferSize cannot be negative");
    }
    if (skip < 0) {
      throw new IllegalArgumentException("skip cannot be negative");
    }
    termAtt.resizeBuffer(bufferSize);

    this.delimiter = delimiter;
    this.replacement = replacement;
    this.skip = skip;
    resultToken = new StringBuilder(bufferSize);
  }

  private static final int DEFAULT_BUFFER_SIZE = 1024;
  public static final char DEFAULT_DELIMITER = '/';
  public static final int DEFAULT_SKIP = 0;

  private final char delimiter;
  private final char replacement;
  private final int skip;

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final PositionIncrementAttribute posAtt = addAttribute(PositionIncrementAttribute.class);
  private int startPosition = 0;
  private int skipped = 0;
  private boolean endDelimiter = false;
  private StringBuilder resultToken;
  
  private int charsRead = 0;


  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    termAtt.append( resultToken );
    if(resultToken.length() == 0){
      posAtt.setPositionIncrement(1);
    }
    else{
      posAtt.setPositionIncrement(0);
    }
    int length = 0;
    boolean added = false;
    if( endDelimiter ){
      termAtt.append(replacement);
      length++;
      endDelimiter = false;
      added = true;
    }

    while (true) {
      int c = input.read();
      if (c >= 0) {
        charsRead++;
      } else {
        if( skipped > skip ) {
          length += resultToken.length();
          termAtt.setLength(length);
           offsetAtt.setOffset(correctOffset(startPosition), correctOffset(startPosition + length));
          if( added ){
            resultToken.setLength(0);
            resultToken.append(termAtt.buffer(), 0, length);
          }
          return added;
        }
        else{
          return false;
        }
      }
      if( !added ){
        added = true;
        skipped++;
        if( skipped > skip ){
          termAtt.append(c == delimiter ? replacement : (char)c);
          length++;
        }
        else {
          startPosition++;
        }
      }
      else {
        if( c == delimiter ){
          if( skipped > skip ){
            endDelimiter = true;
            break;
          }
          skipped++;
          if( skipped > skip ){
            termAtt.append(replacement);
            length++;
          }
          else {
            startPosition++;
          }
        }
        else {
          if( skipped > skip ){
            termAtt.append((char)c);
            length++;
          }
          else {
            startPosition++;
          }
        }
      }
    }
    length += resultToken.length();
    termAtt.setLength(length);
    offsetAtt.setOffset(correctOffset(startPosition), correctOffset(startPosition+length));
    resultToken.setLength(0);
    resultToken.append(termAtt.buffer(), 0, length);
    return true;
  }

  @Override
  public final void end() throws IOException {
    super.end();
    // set final offset
    int finalOffset = correctOffset(charsRead);
    offsetAtt.setOffset(finalOffset, finalOffset);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    resultToken.setLength(0);
    charsRead = 0;
    endDelimiter = false;
    skipped = 0;
    startPosition = 0;
  }
}
