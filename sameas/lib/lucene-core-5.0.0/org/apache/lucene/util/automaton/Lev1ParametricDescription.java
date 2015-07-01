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

// The following code was generated with the moman/finenight pkg
// This package is available under the MIT License, see NOTICE.txt
// for more details.

import org.apache.lucene.util.automaton.LevenshteinAutomata.ParametricDescription;

/** Parametric description for generating a Levenshtein automaton of degree 1 */
class Lev1ParametricDescription extends ParametricDescription {
  
  @Override
  int transition(int absState, int position, int vector) {
    // null absState should never be passed in
    assert absState != -1;
    
    // decode absState -> state, offset
    int state = absState/(w+1);
    int offset = absState%(w+1);
    assert offset >= 0;
    
    if (position == w) {
      if (state < 2) {
        final int loc = vector * 2 + state;
        offset += unpack(offsetIncrs0, loc, 1);
        state = unpack(toStates0, loc, 2)-1;
      }
    } else if (position == w-1) {
      if (state < 3) {
        final int loc = vector * 3 + state;
        offset += unpack(offsetIncrs1, loc, 1);
        state = unpack(toStates1, loc, 2)-1;
      }
    } else if (position == w-2) {
      if (state < 5) {
        final int loc = vector * 5 + state;
        offset += unpack(offsetIncrs2, loc, 2);
        state = unpack(toStates2, loc, 3)-1;
      }
    } else {
      if (state < 5) {
        final int loc = vector * 5 + state;
        offset += unpack(offsetIncrs3, loc, 2);
        state = unpack(toStates3, loc, 3)-1;
      }
    }
    
    if (state == -1) {
      // null state
      return -1;
    } else {
      // translate back to abs
      return state*(w+1)+offset;
    }
  }
    
  // 1 vectors; 2 states per vector; array length = 2
  private final static long[] toStates0 = new long[] /*2 bits per value */ {
    0x2L
  };
  private final static long[] offsetIncrs0 = new long[] /*1 bits per value */ {
    0x0L
  };
    
  // 2 vectors; 3 states per vector; array length = 6
  private final static long[] toStates1 = new long[] /*2 bits per value */ {
    0xa43L
  };
  private final static long[] offsetIncrs1 = new long[] /*1 bits per value */ {
    0x38L
  };
    
  // 4 vectors; 5 states per vector; array length = 20
  private final static long[] toStates2 = new long[] /*3 bits per value */ {
    0x69a292450428003L
  };
  private final static long[] offsetIncrs2 = new long[] /*2 bits per value */ {
    0x5555588000L
  };
    
  // 8 vectors; 5 states per vector; array length = 40
  private final static long[] toStates3 = new long[] /*3 bits per value */ {
    0x1690a82152018003L,0xb1a2d346448a49L
  };
  private final static long[] offsetIncrs3 = new long[] /*2 bits per value */ {
    0x555555b8220f0000L,0x5555L
  };
  
  // state map
  //   0 -> [(0, 0)]
  //   1 -> [(0, 1)]
  //   2 -> [(0, 1), (1, 1)]
  //   3 -> [(0, 1), (2, 1)]
  //   4 -> [(0, 1), (1, 1), (2, 1)]
  
  
  public Lev1ParametricDescription(int w) {
    super(w, 1, new int[] {0,1,0,-1,-1});
  }
}
