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
 * This exception is thrown when determinizing an automaton would result in one
 * has too many states.
 */
public class TooComplexToDeterminizeException extends RuntimeException {
  private final Automaton automaton;
  private final RegExp regExp;
  private final int maxDeterminizedStates;

  /** Use this constructor when the RegExp failed to convert to an automaton. */
  public TooComplexToDeterminizeException(RegExp regExp, TooComplexToDeterminizeException cause) {
    super("Determinizing " + regExp.getOriginalString() + " would result in more than " +
      cause.maxDeterminizedStates + " states.", cause);
    this.regExp = regExp;
    this.automaton = cause.automaton;
    this.maxDeterminizedStates = cause.maxDeterminizedStates;
  }

  /** Use this constructor when the automaton failed to determinize. */
  public TooComplexToDeterminizeException(Automaton automaton, int maxDeterminizedStates) {
    super("Determinizing automaton would result in more than " + maxDeterminizedStates + " states.");
    this.automaton = automaton;
    this.regExp = null;
    this.maxDeterminizedStates = maxDeterminizedStates;
  }

  /** Returns the automaton that caused this exception, if any. */
  public Automaton getAutomaton() {
    return automaton;
  }

  /**
   * Return the RegExp that caused this exception if any.
   */
  public RegExp getRegExp() {
    return regExp;
  }

  /** Get the maximum number of allowed determinized states. */
  public int getMaxDeterminizedStates() {
    return maxDeterminizedStates;
  }
}
