package org.apache.lucene.queryparser.flexible.standard.processors;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.FieldConfig;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;

/**
 * This processors process {@link TermRangeQueryNode}s. It reads the lower and
 * upper bounds value from the {@link TermRangeQueryNode} object and try
 * to parse their values using a {@link DateFormat}. If the values cannot be
 * parsed to a date value, it will only create the {@link TermRangeQueryNode}
 * using the non-parsed values. <br/>
 * <br/>
 * If a {@link ConfigurationKeys#LOCALE} is defined in the
 * {@link QueryConfigHandler} it will be used to parse the date, otherwise
 * {@link Locale#getDefault()} will be used. <br/>
 * <br/>
 * If a {@link ConfigurationKeys#DATE_RESOLUTION} is defined and the
 * {@link Resolution} is not <code>null</code> it will also be used to parse the
 * date value. <br/>
 * <br/>
 * 
 * @see ConfigurationKeys#DATE_RESOLUTION
 * @see ConfigurationKeys#LOCALE
 * @see TermRangeQueryNode
 */
public class TermRangeQueryNodeProcessor extends QueryNodeProcessorImpl {
  
  public TermRangeQueryNodeProcessor() {
  // empty constructor
  }
  
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    
    if (node instanceof TermRangeQueryNode) {
      TermRangeQueryNode termRangeNode = (TermRangeQueryNode) node;
      FieldQueryNode upper = termRangeNode.getUpperBound();
      FieldQueryNode lower = termRangeNode.getLowerBound();
      
      DateTools.Resolution dateRes = null;
      boolean inclusive = false;
      Locale locale = getQueryConfigHandler().get(ConfigurationKeys.LOCALE);
      
      if (locale == null) {
        locale = Locale.getDefault();
      }
      
      TimeZone timeZone = getQueryConfigHandler().get(ConfigurationKeys.TIMEZONE);
      
      if (timeZone == null) {
        timeZone = TimeZone.getDefault();
      }
      
      CharSequence field = termRangeNode.getField();
      String fieldStr = null;
      
      if (field != null) {
        fieldStr = field.toString();
      }
      
      FieldConfig fieldConfig = getQueryConfigHandler()
          .getFieldConfig(fieldStr);
      
      if (fieldConfig != null) {
        dateRes = fieldConfig.get(ConfigurationKeys.DATE_RESOLUTION);
      }
      
      if (termRangeNode.isUpperInclusive()) {
        inclusive = true;
      }
      
      String part1 = lower.getTextAsString();
      String part2 = upper.getTextAsString();
      
      try {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        df.setLenient(true);
        
        if (part1.length() > 0) {
          Date d1 = df.parse(part1);
          part1 = DateTools.dateToString(d1, dateRes);
          lower.setText(part1);
        }
        
        if (part2.length() > 0) {
          Date d2 = df.parse(part2);
          if (inclusive) {
            // The user can only specify the date, not the time, so make sure
            // the time is set to the latest possible time of that date to
            // really
            // include all documents:
            Calendar cal = Calendar.getInstance(timeZone, locale);
            cal.setTime(d2);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            d2 = cal.getTime();
          }
          
          part2 = DateTools.dateToString(d2, dateRes);
          upper.setText(part2);
          
        }
        
      } catch (Exception e) {
        // do nothing
      }
      
    }
    
    return node;
    
  }
  
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    
    return node;
    
  }
  
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    
    return children;
    
  }
  
}
