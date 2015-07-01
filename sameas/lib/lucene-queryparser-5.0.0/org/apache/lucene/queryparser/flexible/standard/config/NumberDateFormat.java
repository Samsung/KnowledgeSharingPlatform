package org.apache.lucene.queryparser.flexible.standard.config;

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
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;

/**
 * This {@link Format} parses {@link Long} into date strings and vice-versa. It
 * uses the given {@link DateFormat} to parse and format dates, but before, it
 * converts {@link Long} to {@link Date} objects or vice-versa.
 */
public class NumberDateFormat extends NumberFormat {
  
  private static final long serialVersionUID = 964823936071308283L;
  
  final private DateFormat dateFormat;
  
  /**
   * Constructs a {@link NumberDateFormat} object using the given {@link DateFormat}.
   * 
   * @param dateFormat {@link DateFormat} used to parse and format dates
   */
  public NumberDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }
  
  @Override
  public StringBuffer format(double number, StringBuffer toAppendTo,
      FieldPosition pos) {
    return dateFormat.format(new Date((long) number), toAppendTo, pos);
  }
  
  @Override
  public StringBuffer format(long number, StringBuffer toAppendTo,
      FieldPosition pos) {
    return dateFormat.format(new Date(number), toAppendTo, pos);
  }
  
  @Override
  public Number parse(String source, ParsePosition parsePosition) {
    final Date date = dateFormat.parse(source, parsePosition);
    return (date == null) ? null : date.getTime();
  }
  
  @Override
  public StringBuffer format(Object number, StringBuffer toAppendTo,
      FieldPosition pos) {
    return dateFormat.format(number, toAppendTo, pos);
  }
  
}
