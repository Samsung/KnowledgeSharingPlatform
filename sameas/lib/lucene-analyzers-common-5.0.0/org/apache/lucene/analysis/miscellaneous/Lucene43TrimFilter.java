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

package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;

/**
 * Backcompat TrimFilter for versions 4.3 and before.
 * @deprecated Use {@link org.apache.lucene.analysis.miscellaneous.TrimFilter}
 */
@Deprecated
public final class Lucene43TrimFilter extends TokenFilter {

  final boolean updateOffsets;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);


  public Lucene43TrimFilter(TokenStream in, boolean updateOffsets) {
    super(in);
    this.updateOffsets = updateOffsets;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) return false;

    char[] termBuffer = termAtt.buffer();
    int len = termAtt.length();
    //TODO: Is this the right behavior or should we return false?  Currently, "  ", returns true, so I think this should
    //also return true
    if (len == 0){
      return true;
    }
    int start = 0;
    int end = 0;
    int endOff = 0;

    // eat the first characters
    //QUESTION: Should we use Character.isWhitespace() instead?
    for (start = 0; start < len && termBuffer[start] <= ' '; start++) {
    }
    // eat the end characters
    for (end = len; end >= start && termBuffer[end - 1] <= ' '; end--) {
      endOff++;
    }
    if (start > 0 || end < len) {
      if (start < end) {
        termAtt.copyBuffer(termBuffer, start, (end - start));
      } else {
        termAtt.setEmpty();
      }
      if (updateOffsets && len == offsetAtt.endOffset() - offsetAtt.startOffset()) {
        int newStart = offsetAtt.startOffset()+start;
        int newEnd = offsetAtt.endOffset() - (start<end ? endOff:0);
        offsetAtt.setOffset(newStart, newEnd);
      }
    }

    return true;
  }
}
