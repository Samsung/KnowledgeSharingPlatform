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

import java.util.HashMap;

/**
 * <p>
 * This class is the base of {@link QueryConfigHandler} and {@link FieldConfig}.
 * It has operations to set, unset and get configuration values.
 * </p>
 * <p>
 * Each configuration is is a key-&gt;value pair. The key should be an unique
 * {@link ConfigurationKey} instance and it also holds the value's type.
 * </p>
 * 
 * @see ConfigurationKey
 */
public abstract class AbstractQueryConfig {
  
  final private HashMap<ConfigurationKey<?>, Object> configMap = new HashMap<>();
  
  AbstractQueryConfig() {
    // although this class is public, it can only be constructed from package
  }
  
  /**
   * Returns the value held by the given key.
   * 
   * @param <T> the value's type
   * 
   * @param key the key, cannot be <code>null</code>
   * 
   * @return the value held by the given key
   */
  @SuppressWarnings("unchecked")
  public <T> T get(ConfigurationKey<T> key) {
    
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null!");
    }
    
    return (T) this.configMap.get(key);
    
  }

  /**
   * Returns true if there is a value set with the given key, otherwise false.
   * 
   * @param <T> the value's type
   * @param key the key, cannot be <code>null</code>
   * @return true if there is a value set with the given key, otherwise false
   */
  public <T> boolean has(ConfigurationKey<T> key) {
    
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null!");
    }
    
    return this.configMap.containsKey(key);
    
  }
  
  /**
   * Sets a key and its value.
   * 
   * @param <T> the value's type
   * @param key the key, cannot be <code>null</code>
   * @param value value to set
   */
  public <T> void set(ConfigurationKey<T> key, T value) {
    
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null!");
    }
    
    if (value == null) {
      unset(key);
      
    } else {
      this.configMap.put(key, value);
    }
    
  }

  /**
   * Unsets the given key and its value.
   * 
   * @param <T> the value's type
   * @param key the key
   * @return true if the key and value was set and removed, otherwise false
   */
  public <T> boolean unset(ConfigurationKey<T> key) {
    
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null!");
    }
    
    return this.configMap.remove(key) != null;
    
  }

}
