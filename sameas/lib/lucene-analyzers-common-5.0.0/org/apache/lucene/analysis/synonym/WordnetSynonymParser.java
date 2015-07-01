package org.apache.lucene.analysis.synonym;

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
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

/**
 * Parser for wordnet prolog format
 * <p>
 * See http://wordnet.princeton.edu/man/prologdb.5WN.html for a description of the format.
 * @lucene.experimental
 */
// TODO: allow you to specify syntactic categories (e.g. just nouns, etc)
public class WordnetSynonymParser extends SynonymMap.Parser {
  private final boolean expand;
  
  public WordnetSynonymParser(boolean dedup, boolean expand, Analyzer analyzer) {
    super(dedup, analyzer);
    this.expand = expand;
  }

  @Override
  public void parse(Reader in) throws IOException, ParseException {
    LineNumberReader br = new LineNumberReader(in);
    try {
      String line = null;
      String lastSynSetID = "";
      CharsRef synset[] = new CharsRef[8];
      int synsetSize = 0;
      
      while ((line = br.readLine()) != null) {
        String synSetID = line.substring(2, 11);

        if (!synSetID.equals(lastSynSetID)) {
          addInternal(synset, synsetSize);
          synsetSize = 0;
        }

        if (synset.length <= synsetSize+1) {
          synset = Arrays.copyOf(synset, synset.length * 2);
        }
        
        synset[synsetSize] = parseSynonym(line, new CharsRefBuilder());
        synsetSize++;
        lastSynSetID = synSetID;
      }
      
      // final synset in the file
      addInternal(synset, synsetSize);
    } catch (IllegalArgumentException e) {
      ParseException ex = new ParseException("Invalid synonym rule at line " + br.getLineNumber(), 0);
      ex.initCause(e);
      throw ex;
    } finally {
      br.close();
    }
  }
 
  private CharsRef parseSynonym(String line, CharsRefBuilder reuse) throws IOException {
    if (reuse == null) {
      reuse = new CharsRefBuilder();
    }
    
    int start = line.indexOf('\'')+1;
    int end = line.lastIndexOf('\'');
    
    String text = line.substring(start, end).replace("''", "'");
    return analyze(text, reuse);
  }
  
  private void addInternal(CharsRef synset[], int size) {
    if (size <= 1) {
      return; // nothing to do
    }
    
    if (expand) {
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
          add(synset[i], synset[j], false);
        }
      }
    } else {
      for (int i = 0; i < size; i++) {
        add(synset[i], synset[0], false);
      }
    }
  }
}
