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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

/**
 * Parser for the Solr synonyms format.
 * <ol>
 *   <li> Blank lines and lines starting with '#' are comments.
 *   <li> Explicit mappings match any token sequence on the LHS of "=&gt;"
 *        and replace with all alternatives on the RHS.  These types of mappings
 *        ignore the expand parameter in the constructor.
 *        Example:
 *        <blockquote>i-pod, i pod =&gt; ipod</blockquote>
 *   <li> Equivalent synonyms may be separated with commas and give
 *        no explicit mapping.  In this case the mapping behavior will
 *        be taken from the expand parameter in the constructor.  This allows
 *        the same synonym file to be used in different synonym handling strategies.
 *        Example:
 *        <blockquote>ipod, i-pod, i pod</blockquote>
 * 
 *   <li> Multiple synonym mapping entries are merged.
 *        Example:
 *        <blockquote>
 *         foo =&gt; foo bar<br>
 *         foo =&gt; baz<br><br>
 *         is equivalent to<br><br>
 *         foo =&gt; foo bar, baz
 *        </blockquote>
 *  </ol>
 * @lucene.experimental
 */
public class SolrSynonymParser extends SynonymMap.Parser {
  private final boolean expand;
  
  public SolrSynonymParser(boolean dedup, boolean expand, Analyzer analyzer) {
    super(dedup, analyzer);
    this.expand = expand;
  }

  @Override
  public void parse(Reader in) throws IOException, ParseException {
    LineNumberReader br = new LineNumberReader(in);
    try {
      addInternal(br);
    } catch (IllegalArgumentException e) {
      ParseException ex = new ParseException("Invalid synonym rule at line " + br.getLineNumber(), 0);
      ex.initCause(e);
      throw ex;
    } finally {
      br.close();
    }
  }
  
  private void addInternal(BufferedReader in) throws IOException {
    String line = null;
    while ((line = in.readLine()) != null) {
      if (line.length() == 0 || line.charAt(0) == '#') {
        continue; // ignore empty lines and comments
      }
      
      CharsRef inputs[];
      CharsRef outputs[];
      
      // TODO: we could process this more efficiently.
      String sides[] = split(line, "=>");
      if (sides.length > 1) { // explicit mapping
        if (sides.length != 2) {
          throw new IllegalArgumentException("more than one explicit mapping specified on the same line");
        }
        String inputStrings[] = split(sides[0], ",");
        inputs = new CharsRef[inputStrings.length];
        for (int i = 0; i < inputs.length; i++) {
          inputs[i] = analyze(unescape(inputStrings[i]).trim(), new CharsRefBuilder());
        }
        
        String outputStrings[] = split(sides[1], ",");
        outputs = new CharsRef[outputStrings.length];
        for (int i = 0; i < outputs.length; i++) {
          outputs[i] = analyze(unescape(outputStrings[i]).trim(), new CharsRefBuilder());
        }
      } else {
        String inputStrings[] = split(line, ",");
        inputs = new CharsRef[inputStrings.length];
        for (int i = 0; i < inputs.length; i++) {
          inputs[i] = analyze(unescape(inputStrings[i]).trim(), new CharsRefBuilder());
        }
        if (expand) {
          outputs = inputs;
        } else {
          outputs = new CharsRef[1];
          outputs[0] = inputs[0];
        }
      }
      
      // currently we include the term itself in the map,
      // and use includeOrig = false always.
      // this is how the existing filter does it, but it's actually a bug,
      // especially if combined with ignoreCase = true
      for (int i = 0; i < inputs.length; i++) {
        for (int j = 0; j < outputs.length; j++) {
          add(inputs[i], outputs[j], false);
        }
      }
    }
  }
  
  private static String[] split(String s, String separator) {
    ArrayList<String> list = new ArrayList<>(2);
    StringBuilder sb = new StringBuilder();
    int pos=0, end=s.length();
    while (pos < end) {
      if (s.startsWith(separator,pos)) {
        if (sb.length() > 0) {
          list.add(sb.toString());
          sb=new StringBuilder();
        }
        pos+=separator.length();
        continue;
      }

      char ch = s.charAt(pos++);
      if (ch=='\\') {
        sb.append(ch);
        if (pos>=end) break;  // ERROR, or let it go?
        ch = s.charAt(pos++);
      }

      sb.append(ch);
    }

    if (sb.length() > 0) {
      list.add(sb.toString());
    }

    return list.toArray(new String[list.size()]);
  }
  
  private String unescape(String s) {
    if (s.indexOf("\\") >= 0) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (ch == '\\' && i < s.length() - 1) {
          sb.append(s.charAt(++i));
        } else {
          sb.append(ch);
        }
      }
      return sb.toString();
    }
    return s;
  }
}
