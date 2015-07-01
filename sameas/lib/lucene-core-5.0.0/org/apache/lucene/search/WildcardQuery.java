package org.apache.lucene.search;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.Operations;

/** Implements the wildcard search query. Supported wildcards are <code>*</code>, which
 * matches any character sequence (including the empty one), and <code>?</code>,
 * which matches any single character. '\' is the escape character.
 * <p>
 * Note this query can be slow, as it
 * needs to iterate over many terms. In order to prevent extremely slow WildcardQueries,
 * a Wildcard term should not start with the wildcard <code>*</code>
 * 
 * <p>This query uses the {@link
 * MultiTermQuery#CONSTANT_SCORE_FILTER_REWRITE}
 * rewrite method.
 *
 * @see AutomatonQuery
 */
public class WildcardQuery extends AutomatonQuery {
  /** String equality with support for wildcards */
  public static final char WILDCARD_STRING = '*';

  /** Char equality with support for wildcards */
  public static final char WILDCARD_CHAR = '?';

  /** Escape character */
  public static final char WILDCARD_ESCAPE = '\\';
  
  /**
   * Constructs a query for terms matching <code>term</code>. 
   */
  public WildcardQuery(Term term) {
    super(term, toAutomaton(term));
  }
  
  /**
   * Constructs a query for terms matching <code>term</code>.
   * @param maxDeterminizedStates maximum number of states in the resulting
   *   automata.  If the automata would need more than this many states
   *   TooComplextToDeterminizeException is thrown.  Higher number require more
   *   space but can process more complex automata.
   */
  public WildcardQuery(Term term, int maxDeterminizedStates) {
    super(term, toAutomaton(term), maxDeterminizedStates);
  }

  /**
   * Convert Lucene wildcard syntax into an automaton.
   * @lucene.internal
   */
  @SuppressWarnings("fallthrough")
  public static Automaton toAutomaton(Term wildcardquery) {
    List<Automaton> automata = new ArrayList<>();
    
    String wildcardText = wildcardquery.text();
    
    for (int i = 0; i < wildcardText.length();) {
      final int c = wildcardText.codePointAt(i);
      int length = Character.charCount(c);
      switch(c) {
        case WILDCARD_STRING: 
          automata.add(Automata.makeAnyString());
          break;
        case WILDCARD_CHAR:
          automata.add(Automata.makeAnyChar());
          break;
        case WILDCARD_ESCAPE:
          // add the next codepoint instead, if it exists
          if (i + length < wildcardText.length()) {
            final int nextChar = wildcardText.codePointAt(i + length);
            length += Character.charCount(nextChar);
            automata.add(Automata.makeChar(nextChar));
            break;
          } // else fallthru, lenient parsing with a trailing \
        default:
          automata.add(Automata.makeChar(c));
      }
      i += length;
    }
    
    return Operations.concatenate(automata);
  }
  
  /**
   * Returns the pattern term.
   */
  public Term getTerm() {
    return term;
  }
  
  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!getField().equals(field)) {
      buffer.append(getField());
      buffer.append(":");
    }
    buffer.append(term.text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
}
