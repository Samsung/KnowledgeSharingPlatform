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

import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.AutomatonProvider;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;

/**
 * A fast regular expression query based on the
 * {@link org.apache.lucene.util.automaton} package.
 * <ul>
 * <li>Comparisons are <a
 * href="http://tusker.org/regex/regex_benchmark.html">fast</a>
 * <li>The term dictionary is enumerated in an intelligent way, to avoid
 * comparisons. See {@link AutomatonQuery} for more details.
 * </ul>
 * <p>
 * The supported syntax is documented in the {@link RegExp} class.
 * Note this might be different than other regular expression implementations.
 * For some alternatives with different syntax, look under the sandbox.
 * </p>
 * <p>
 * Note this query can be slow, as it needs to iterate over many terms. In order
 * to prevent extremely slow RegexpQueries, a Regexp term should not start with
 * the expression <code>.*</code>
 * 
 * @see RegExp
 * @lucene.experimental
 */
public class RegexpQuery extends AutomatonQuery {
  /**
   * A provider that provides no named automata
   */
  private static AutomatonProvider defaultProvider = new AutomatonProvider() {
    @Override
    public Automaton getAutomaton(String name) {
      return null;
    }
  };
  
  /**
   * Constructs a query for terms matching <code>term</code>.
   * <p>
   * By default, all regular expression features are enabled.
   * </p>
   * 
   * @param term regular expression.
   */
  public RegexpQuery(Term term) {
    this(term, RegExp.ALL);
  }
  
  /**
   * Constructs a query for terms matching <code>term</code>.
   * 
   * @param term regular expression.
   * @param flags optional RegExp features from {@link RegExp}
   */
  public RegexpQuery(Term term, int flags) {
    this(term, flags, defaultProvider,
      Operations.DEFAULT_MAX_DETERMINIZED_STATES);
  }

  /**
   * Constructs a query for terms matching <code>term</code>.
   * 
   * @param term regular expression.
   * @param flags optional RegExp features from {@link RegExp}
   * @param maxDeterminizedStates maximum number of states that compiling the
   *  automaton for the regexp can result in.  Set higher to allow more complex
   *  queries and lower to prevent memory exhaustion.
   */
  public RegexpQuery(Term term, int flags, int maxDeterminizedStates) {
    this(term, flags, defaultProvider, maxDeterminizedStates);
  }

  /**
   * Constructs a query for terms matching <code>term</code>.
   * 
   * @param term regular expression.
   * @param flags optional RegExp features from {@link RegExp}
   * @param provider custom AutomatonProvider for named automata
   * @param maxDeterminizedStates maximum number of states that compiling the
   *  automaton for the regexp can result in.  Set higher to allow more complex
   *  queries and lower to prevent memory exhaustion.
   */
  public RegexpQuery(Term term, int flags, AutomatonProvider provider,
      int maxDeterminizedStates) {
    super(term,
          new RegExp(term.text(), flags).toAutomaton(
                       provider, maxDeterminizedStates), maxDeterminizedStates);
  }
  
  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!term.field().equals(field)) {
      buffer.append(term.field());
      buffer.append(":");
    }
    buffer.append('/');
    buffer.append(term.text());
    buffer.append('/');
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
}
