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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link IndexReaderContext} for {@link CompositeReader} instance.
 */
public final class CompositeReaderContext extends IndexReaderContext {
  private final List<IndexReaderContext> children;
  private final List<LeafReaderContext> leaves;
  private final CompositeReader reader;
  
  static CompositeReaderContext create(CompositeReader reader) {
    return new Builder(reader).build();
  }

  /**
   * Creates a {@link CompositeReaderContext} for intermediate readers that aren't
   * not top-level readers in the current context
   */
  CompositeReaderContext(CompositeReaderContext parent, CompositeReader reader,
      int ordInParent, int docbaseInParent, List<IndexReaderContext> children) {
    this(parent, reader, ordInParent, docbaseInParent, children, null);
  }
  
  /**
   * Creates a {@link CompositeReaderContext} for top-level readers with parent set to <code>null</code>
   */
  CompositeReaderContext(CompositeReader reader, List<IndexReaderContext> children, List<LeafReaderContext> leaves) {
    this(null, reader, 0, 0, children, leaves);
  }
  
  private CompositeReaderContext(CompositeReaderContext parent, CompositeReader reader,
      int ordInParent, int docbaseInParent, List<IndexReaderContext> children,
      List<LeafReaderContext> leaves) {
    super(parent, ordInParent, docbaseInParent);
    this.children = Collections.unmodifiableList(children);
    this.leaves = leaves == null ? null : Collections.unmodifiableList(leaves);
    this.reader = reader;
  }

  @Override
  public List<LeafReaderContext> leaves() throws UnsupportedOperationException {
    if (!isTopLevel)
      throw new UnsupportedOperationException("This is not a top-level context.");
    assert leaves != null;
    return leaves;
  }
  
  
  @Override
  public List<IndexReaderContext> children() {
    return children;
  }
  
  @Override
  public CompositeReader reader() {
    return reader;
  }
  
  private static final class Builder {
    private final CompositeReader reader;
    private final List<LeafReaderContext> leaves = new ArrayList<>();
    private int leafDocBase = 0;
    
    public Builder(CompositeReader reader) {
      this.reader = reader;
    }
    
    public CompositeReaderContext build() {
      return (CompositeReaderContext) build(null, reader, 0, 0);
    }
    
    private IndexReaderContext build(CompositeReaderContext parent, IndexReader reader, int ord, int docBase) {
      if (reader instanceof LeafReader) {
        final LeafReader ar = (LeafReader) reader;
        final LeafReaderContext atomic = new LeafReaderContext(parent, ar, ord, docBase, leaves.size(), leafDocBase);
        leaves.add(atomic);
        leafDocBase += reader.maxDoc();
        return atomic;
      } else {
        final CompositeReader cr = (CompositeReader) reader;
        final List<? extends IndexReader> sequentialSubReaders = cr.getSequentialSubReaders();
        final List<IndexReaderContext> children = Arrays.asList(new IndexReaderContext[sequentialSubReaders.size()]);
        final CompositeReaderContext newParent;
        if (parent == null) {
          newParent = new CompositeReaderContext(cr, children, leaves);
        } else {
          newParent = new CompositeReaderContext(parent, cr, ord, docBase, children);
        }
        int newDocBase = 0;
        for (int i = 0, c = sequentialSubReaders.size(); i < c; i++) {
          final IndexReader r = sequentialSubReaders.get(i);
          children.set(i, build(newParent, r, i, newDocBase));
          newDocBase += r.maxDoc();
        }
        assert newDocBase == cr.maxDoc();
        return newParent;
      }
    }
  }

}