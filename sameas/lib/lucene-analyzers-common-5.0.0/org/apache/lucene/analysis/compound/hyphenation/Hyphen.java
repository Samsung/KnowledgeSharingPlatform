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

package org.apache.lucene.analysis.compound.hyphenation;

/**
 * This class represents a hyphen. A 'full' hyphen is made of 3 parts: the
 * pre-break text, post-break text and no-break. If no line-break is generated
 * at this position, the no-break text is used, otherwise, pre-break and
 * post-break are used. Typically, pre-break is equal to the hyphen character
 * and the others are empty. However, this general scheme allows support for
 * cases in some languages where words change spelling if they're split across
 * lines, like german's 'backen' which hyphenates 'bak-ken'. BTW, this comes
 * from TeX.
 * 
 * This class has been taken from the Apache FOP project (http://xmlgraphics.apache.org/fop/). They have been slightly modified. 
 */

public class Hyphen {
  public String preBreak;

  public String noBreak;

  public String postBreak;

  Hyphen(String pre, String no, String post) {
    preBreak = pre;
    noBreak = no;
    postBreak = post;
  }

  Hyphen(String pre) {
    preBreak = pre;
    noBreak = null;
    postBreak = null;
  }

  @Override
  public String toString() {
    if (noBreak == null && postBreak == null && preBreak != null
        && preBreak.equals("-")) {
      return "-";
    }
    StringBuilder res = new StringBuilder("{");
    res.append(preBreak);
    res.append("}{");
    res.append(postBreak);
    res.append("}{");
    res.append(noBreak);
    res.append('}');
    return res.toString();
  }

}
