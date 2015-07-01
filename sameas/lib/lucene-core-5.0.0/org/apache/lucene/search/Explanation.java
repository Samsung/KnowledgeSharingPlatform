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

/** Expert: Describes the score computation for document and query. */
public class Explanation {
  private float value;                            // the value of this node
  private String description;                     // what it represents
  private ArrayList<Explanation> details;                      // sub-explanations

  public Explanation() {}

  public Explanation(float value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * Indicates whether or not this Explanation models a good match.
   *
   * <p>
   * By default, an Explanation represents a "match" if the value is positive.
   * </p>
   * @see #getValue
   */
  public boolean isMatch() {
    return (0.0f < getValue());
  }


  
  /** The value assigned to this explanation node. */
  public float getValue() { return value; }
  /** Sets the value assigned to this explanation node. */
  public void setValue(float value) { this.value = value; }

  /** A description of this explanation node. */
  public String getDescription() { return description; }
  /** Sets the description of this explanation node. */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * A short one line summary which should contain all high level
   * information about this Explanation, without the "Details"
   */
  protected String getSummary() {
    return getValue() + " = " + getDescription();
  }
  
  /** The sub-nodes of this explanation node. */
  public Explanation[] getDetails() {
    if (details == null)
      return null;
    return details.toArray(new Explanation[0]);
  }

  /** Adds a sub-node to this explanation node. */
  public void addDetail(Explanation detail) {
    if (details == null)
      details = new ArrayList<>();
    details.add(detail);
  }

  /** Render an explanation as text. */
  @Override
  public String toString() {
    return toString(0);
  }
  protected String toString(int depth) {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      buffer.append("  ");
    }
    buffer.append(getSummary());
    buffer.append("\n");

    Explanation[] details = getDetails();
    if (details != null) {
      for (int i = 0 ; i < details.length; i++) {
        buffer.append(details[i].toString(depth+1));
      }
    }

    return buffer.toString();
  }


  /** Render an explanation as HTML. */
  public String toHtml() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<ul>\n");

    buffer.append("<li>");
    buffer.append(getSummary());
    buffer.append("<br />\n");

    Explanation[] details = getDetails();
    if (details != null) {
      for (int i = 0 ; i < details.length; i++) {
        buffer.append(details[i].toHtml());
      }
    }

    buffer.append("</li>\n");
    buffer.append("</ul>\n");

    return buffer.toString();
  }
}
