package org.apache.lucene.util;

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


import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Helper class for keeping Lists of Objects associated with keys. <b>WARNING: THIS CLASS IS NOT THREAD SAFE</b>
 * @lucene.internal
 */
public class MapOfSets<K, V> {

  private final Map<K, Set<V>> theMap;

  /**
   * @param m the backing store for this object
   */
  public MapOfSets(Map<K, Set<V>> m) {
    theMap = m;
  }

  /**
   * @return direct access to the map backing this object.
   */
  public Map<K, Set<V>> getMap() {
    return theMap;
  }

  /**
   * Adds val to the Set associated with key in the Map.  If key is not 
   * already in the map, a new Set will first be created.
   * @return the size of the Set associated with key once val is added to it.
   */
  public int put(K key, V val) {
    final Set<V> theSet;
    if (theMap.containsKey(key)) {
      theSet = theMap.get(key);
    } else {
      theSet = new HashSet<>(23);
      theMap.put(key, theSet);
    }
    theSet.add(val);
    return theSet.size();
  }
   /**
   * Adds multiple vals to the Set associated with key in the Map.  
   * If key is not 
   * already in the map, a new Set will first be created.
   * @return the size of the Set associated with key once val is added to it.
   */
  public int putAll(K key, Collection<? extends V> vals) {
    final Set<V> theSet;
    if (theMap.containsKey(key)) {
      theSet = theMap.get(key);
    } else {
      theSet = new HashSet<>(23);
      theMap.put(key, theSet);
    }
    theSet.addAll(vals);
    return theSet.size();
  }
 
}
