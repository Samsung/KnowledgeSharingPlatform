package org.apache.lucene.queryparser.flexible.core.config;

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

import java.util.LinkedList;

import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;

/**
 * This class can be used to hold any query configuration and no field
 * configuration. For field configuration, it creates an empty
 * {@link FieldConfig} object and delegate it to field config listeners, 
 * these are responsible for setting up all the field configuration.
 * 
 * {@link QueryConfigHandler} should be extended by classes that intends to
 * provide configuration to {@link QueryNodeProcessor} objects.
 * 
 * The class that extends {@link QueryConfigHandler} should also provide
 * {@link FieldConfig} objects for each collection field.
 * 
 * @see FieldConfig
 * @see FieldConfigListener
 * @see QueryConfigHandler
 */
public abstract class QueryConfigHandler extends AbstractQueryConfig {
  
  final private LinkedList<FieldConfigListener> listeners = new LinkedList<>();

  /**
   * Returns an implementation of
   * {@link FieldConfig} for a specific field name. If the implemented
   * {@link QueryConfigHandler} does not know a specific field name, it may
   * return <code>null</code>, indicating there is no configuration for that
   * field.
   * 
   * @param fieldName
   *          the field name
   * @return a {@link FieldConfig} object containing the field name
   *         configuration or <code>null</code>, if the implemented
   *         {@link QueryConfigHandler} has no configuration for that field
   */
  public FieldConfig getFieldConfig(String fieldName) {
    FieldConfig fieldConfig = new FieldConfig(StringUtils.toString(fieldName));

    for (FieldConfigListener listener : this.listeners) {
      listener.buildFieldConfig(fieldConfig);
    }

    return fieldConfig;

  }

  /**
   * Adds a listener. The added listeners are called in the order they are
   * added.
   * 
   * @param listener
   *          the listener to be added
   */
  public void addFieldConfigListener(FieldConfigListener listener) {
    this.listeners.add(listener);
  }
  
}
