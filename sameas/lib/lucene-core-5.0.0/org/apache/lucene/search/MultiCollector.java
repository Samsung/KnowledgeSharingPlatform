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

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.LeafReaderContext;

/**
 * A {@link Collector} which allows running a search with several
 * {@link Collector}s. It offers a static {@link #wrap} method which accepts a
 * list of collectors and wraps them with {@link MultiCollector}, while
 * filtering out the <code>null</code> null ones.
 */
public class MultiCollector implements Collector {

  /** See {@link #wrap(Iterable)}. */
  public static Collector wrap(Collector... collectors) {
    return wrap(Arrays.asList(collectors));
  }

  /**
   * Wraps a list of {@link Collector}s with a {@link MultiCollector}. This
   * method works as follows:
   * <ul>
   * <li>Filters out the <code>null</code> collectors, so they are not used
   * during search time.
   * <li>If the input contains 1 real collector (i.e. non-<code>null</code> ),
   * it is returned.
   * <li>Otherwise the method returns a {@link MultiCollector} which wraps the
   * non-<code>null</code> ones.
   * </ul>
   * 
   * @throws IllegalArgumentException
   *           if either 0 collectors were input, or all collectors are
   *           <code>null</code>.
   */
  public static Collector wrap(Iterable<? extends Collector> collectors) {
    // For the user's convenience, we allow null collectors to be passed.
    // However, to improve performance, these null collectors are found
    // and dropped from the array we save for actual collection time.
    int n = 0;
    for (Collector c : collectors) {
      if (c != null) {
        n++;
      }
    }

    if (n == 0) {
      throw new IllegalArgumentException("At least 1 collector must not be null");
    } else if (n == 1) {
      // only 1 Collector - return it.
      Collector col = null;
      for (Collector c : collectors) {
        if (c != null) {
          col = c;
          break;
        }
      }
      return col;
    } else {
      Collector[] colls = new Collector[n];
      n = 0;
      for (Collector c : collectors) {
        if (c != null) {
          colls[n++] = c;
        }
      }
      return new MultiCollector(colls);
    }
  }
  
  private final Collector[] collectors;

  private MultiCollector(Collector... collectors) {
    this.collectors = collectors;
  }

  @Override
  public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
    final LeafCollector[] leafCollectors = new LeafCollector[collectors.length];
    for (int i = 0; i < collectors.length; ++i) {
      leafCollectors[i] = collectors[i].getLeafCollector(context);
    }
    return new MultiLeafCollector(leafCollectors);
  }


  private static class MultiLeafCollector implements LeafCollector {

    private final LeafCollector[] collectors;

    private MultiLeafCollector(LeafCollector[] collectors) {
      this.collectors = collectors;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
      for (LeafCollector c : collectors) {
        c.setScorer(scorer);
      }
    }

    @Override
    public void collect(int doc) throws IOException {
      for (LeafCollector c : collectors) {
        c.collect(doc);
      }
    }

  }

}
