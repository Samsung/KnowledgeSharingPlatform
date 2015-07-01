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

import java.util.Map;

import org.apache.lucene.queryparser.flexible.core.config.FieldConfig;
import org.apache.lucene.queryparser.flexible.core.config.FieldConfigListener;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;

/**
 * This listener is used to listen to {@link FieldConfig} requests in
 * {@link QueryConfigHandler} and add {@link ConfigurationKeys#NUMERIC_CONFIG}
 * based on the {@link ConfigurationKeys#NUMERIC_CONFIG_MAP} set in the
 * {@link QueryConfigHandler}.
 * 
 * @see NumericConfig
 * @see QueryConfigHandler
 * @see ConfigurationKeys#NUMERIC_CONFIG
 * @see ConfigurationKeys#NUMERIC_CONFIG_MAP
 */
public class NumericFieldConfigListener implements FieldConfigListener {
  
  final private QueryConfigHandler config;
  
  /**
   * Construcs a {@link NumericFieldConfigListener} object using the given {@link QueryConfigHandler}.
   * 
   * @param config the {@link QueryConfigHandler} it will listen too
   */
  public NumericFieldConfigListener(QueryConfigHandler config) {
    
    if (config == null) {
      throw new IllegalArgumentException("config cannot be null!");
    }
    
    this.config = config;
    
  }
  
  @Override
  public void buildFieldConfig(FieldConfig fieldConfig) {
    Map<String,NumericConfig> numericConfigMap = config
        .get(ConfigurationKeys.NUMERIC_CONFIG_MAP);
    
    if (numericConfigMap != null) {
      NumericConfig numericConfig = numericConfigMap
          .get(fieldConfig.getField());
      
      if (numericConfig != null) {
        fieldConfig.set(ConfigurationKeys.NUMERIC_CONFIG, numericConfig);
      }
      
    }
    
  }
  
}
