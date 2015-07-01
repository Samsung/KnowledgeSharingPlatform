package org.apache.lucene.analysis.sinks;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * Attempts to parse the {@link CharTermAttribute#buffer()} as a Date using a {@link java.text.DateFormat}.
 * If the value is a Date, it will add it to the sink.
 * <p/> 
 *
 **/
public class DateRecognizerSinkFilter extends TeeSinkTokenFilter.SinkFilter {
  public static final String DATE_TYPE = "date";

  protected DateFormat dateFormat;
  protected CharTermAttribute termAtt;
  
  /**
   * Uses {@link java.text.DateFormat#getDateInstance(int, Locale)
   * DateFormat#getDateInstance(DateFormat.DEFAULT, Locale.ROOT)} as 
   * the {@link java.text.DateFormat} object.
   */
  public DateRecognizerSinkFilter() {
    this(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ROOT));
  }
  
  public DateRecognizerSinkFilter(DateFormat dateFormat) {
    this.dateFormat = dateFormat; 
  }

  @Override
  public boolean accept(AttributeSource source) {
    if (termAtt == null) {
      termAtt = source.addAttribute(CharTermAttribute.class);
    }
    try {
      Date date = dateFormat.parse(termAtt.toString());//We don't care about the date, just that we can parse it as a date
      if (date != null) {
        return true;
      }
    } catch (ParseException e) {
  
    }
    
    return false;
  }

}
