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

/** Expert: Describes the score computation for document and query, and
 * can distinguish a match independent of a positive value. */
public class ComplexExplanation extends Explanation {
  private Boolean match;
  
  public ComplexExplanation() {
    super();
  }

  public ComplexExplanation(boolean match, float value, String description) {
    // NOTE: use of "boolean" instead of "Boolean" in params is conscious
    // choice to encourage clients to be specific.
    super(value, description);
    this.match = Boolean.valueOf(match);
  }

  /**
   * The match status of this explanation node.
   * @return May be null if match status is unknown
   */
  public Boolean getMatch() { return match; }
  /**
   * Sets the match status assigned to this explanation node.
   * @param match May be null if match status is unknown
   */
  public void setMatch(Boolean match) { this.match = match; }
  /**
   * Indicates whether or not this Explanation models a good match.
   *
   * <p>
   * If the match status is explicitly set (i.e.: not null) this method
   * uses it; otherwise it defers to the superclass.
   * </p>
   * @see #getMatch
   */
  @Override
  public boolean isMatch() {
    Boolean m = getMatch();
    return (null != m ? m.booleanValue() : super.isMatch());
  }

  @Override
  protected String getSummary() {
    if (null == getMatch())
      return super.getSummary();
    
    return getValue() + " = "
      + (isMatch() ? "(MATCH) " : "(NON-MATCH) ")
      + getDescription();
  }
  
}
