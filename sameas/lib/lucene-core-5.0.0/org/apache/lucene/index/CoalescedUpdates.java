package org.apache.lucene.index;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.BufferedUpdatesStream.QueryAndLimit;
import org.apache.lucene.index.DocValuesUpdate.BinaryDocValuesUpdate;
import org.apache.lucene.index.DocValuesUpdate.NumericDocValuesUpdate;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.MergedIterator;

class CoalescedUpdates {
  final Map<Query,Integer> queries = new HashMap<>();
  final List<Iterable<Term>> iterables = new ArrayList<>();
  final List<NumericDocValuesUpdate> numericDVUpdates = new ArrayList<>();
  final List<BinaryDocValuesUpdate> binaryDVUpdates = new ArrayList<>();
  int totalTermCount;
  
  @Override
  public String toString() {
    // note: we could add/collect more debugging information
    return "CoalescedUpdates(termSets=" + iterables.size()
      + ",totalTermCount=" + totalTermCount
      + ",queries=" + queries.size() + ",numericDVUpdates=" + numericDVUpdates.size()
      + ",binaryDVUpdates=" + binaryDVUpdates.size() + ")";
  }

  void update(FrozenBufferedUpdates in) {
    totalTermCount += in.termCount;
    iterables.add(in.termsIterable());

    for (int queryIdx = 0; queryIdx < in.queries.length; queryIdx++) {
      final Query query = in.queries[queryIdx];
      queries.put(query, BufferedUpdates.MAX_INT);
    }
    
    for (NumericDocValuesUpdate nu : in.numericDVUpdates) {
      NumericDocValuesUpdate clone = new NumericDocValuesUpdate(nu.term, nu.field, (Long) nu.value);
      clone.docIDUpto = Integer.MAX_VALUE;
      numericDVUpdates.add(clone);
    }
    
    for (BinaryDocValuesUpdate bu : in.binaryDVUpdates) {
      BinaryDocValuesUpdate clone = new BinaryDocValuesUpdate(bu.term, bu.field, (BytesRef) bu.value);
      clone.docIDUpto = Integer.MAX_VALUE;
      binaryDVUpdates.add(clone);
    }
  }

 public Iterable<Term> termsIterable() {
   return new Iterable<Term>() {
     @SuppressWarnings({"unchecked","rawtypes"})
     @Override
     public Iterator<Term> iterator() {
       Iterator<Term> subs[] = new Iterator[iterables.size()];
       for (int i = 0; i < iterables.size(); i++) {
         subs[i] = iterables.get(i).iterator();
       }
       return new MergedIterator<>(subs);
     }
   };
  }

  public Iterable<QueryAndLimit> queriesIterable() {
    return new Iterable<QueryAndLimit>() {
      
      @Override
      public Iterator<QueryAndLimit> iterator() {
        return new Iterator<QueryAndLimit>() {
          private final Iterator<Map.Entry<Query,Integer>> iter = queries.entrySet().iterator();

          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }

          @Override
          public QueryAndLimit next() {
            final Map.Entry<Query,Integer> ent = iter.next();
            return new QueryAndLimit(ent.getKey(), ent.getValue());
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
