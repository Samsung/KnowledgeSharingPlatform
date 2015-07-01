package org.apache.lucene.util.automaton;

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

/**
 * Automaton representation for matching char[].
 */
public class CharacterRunAutomaton extends RunAutomaton {
  /**
   * Construct with a default number of maxDeterminizedStates.
   */
  public CharacterRunAutomaton(Automaton a) {
    this(a, Operations.DEFAULT_MAX_DETERMINIZED_STATES);
  }

  /**
   * Construct specifying maxDeterminizedStates.
   * @param a Automaton to match
   * @param maxDeterminizedStates maximum number of states that the automataon
   *   can have once determinized.  If more states are required to determinize
   *   it then a TooComplexToDeterminizeException is thrown.
   */ 
  public CharacterRunAutomaton(Automaton a, int maxDeterminizedStates) {
    super(a, Character.MAX_CODE_POINT, false, maxDeterminizedStates);
  }

  /**
   * Returns true if the given string is accepted by this automaton.
   */
  public boolean run(String s) {
    int p = initial;
    int l = s.length();
    for (int i = 0, cp = 0; i < l; i += Character.charCount(cp)) {
      p = step(p, cp = s.codePointAt(i));
      if (p == -1) return false;
    }
    return accept[p];
  }
  
  /**
   * Returns true if the given string is accepted by this automaton
   */
  public boolean run(char[] s, int offset, int length) {
    int p = initial;
    int l = offset + length;
    for (int i = offset, cp = 0; i < l; i += Character.charCount(cp)) {
      p = step(p, cp = Character.codePointAt(s, i, l));
      if (p == -1) return false;
    }
    return accept[p];
  }
}
