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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.RefCount;

/**
 * Manages the {@link DocValuesProducer} held by {@link SegmentReader} and
 * keeps track of their reference counting.
 */
final class SegmentDocValues {

  private final Map<Long,RefCount<DocValuesProducer>> genDVProducers = new HashMap<>();

  private RefCount<DocValuesProducer> newDocValuesProducer(SegmentCommitInfo si, Directory dir, final Long gen, FieldInfos infos) throws IOException {
    Directory dvDir = dir;
    String segmentSuffix = "";
    if (gen.longValue() != -1) {
      dvDir = si.info.dir; // gen'd files are written outside CFS, so use SegInfo directory
      segmentSuffix = Long.toString(gen.longValue(), Character.MAX_RADIX);
    }

    // set SegmentReadState to list only the fields that are relevant to that gen
    SegmentReadState srs = new SegmentReadState(dvDir, si.info, infos, IOContext.READ, segmentSuffix);
    DocValuesFormat dvFormat = si.info.getCodec().docValuesFormat();
    return new RefCount<DocValuesProducer>(dvFormat.fieldsProducer(srs)) {
      @SuppressWarnings("synthetic-access")
      @Override
      protected void release() throws IOException {
        object.close();
        synchronized (SegmentDocValues.this) {
          genDVProducers.remove(gen);
        }
      }
    };
  }

  /** Returns the {@link DocValuesProducer} for the given generation. */
  synchronized DocValuesProducer getDocValuesProducer(long gen, SegmentCommitInfo si, Directory dir, FieldInfos infos) throws IOException {
    RefCount<DocValuesProducer> dvp = genDVProducers.get(gen);
    if (dvp == null) {
      dvp = newDocValuesProducer(si, dir, gen, infos);
      assert dvp != null;
      genDVProducers.put(gen, dvp);
    } else {
      dvp.incRef();
    }
    return dvp.get();
  }
  
  /**
   * Decrement the reference count of the given {@link DocValuesProducer}
   * generations. 
   */
  synchronized void decRef(List<Long> dvProducersGens) throws IOException {
    Throwable t = null;
    for (Long gen : dvProducersGens) {
      RefCount<DocValuesProducer> dvp = genDVProducers.get(gen);
      assert dvp != null : "gen=" + gen;
      try {
        dvp.decRef();
      } catch (Throwable th) {
        if (t != null) {
          t = th;
        }
      }
    }
    if (t != null) {
      IOUtils.reThrow(t);
    }
  }
}
