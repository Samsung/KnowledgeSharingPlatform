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

import java.util.List;

/**
 * A struct like class that represents a hierarchical relationship between
 * {@link IndexReader} instances. 
 */
public abstract class IndexReaderContext {
  /** The reader context for this reader's immediate parent, or null if none */
  public final CompositeReaderContext parent;
  /** <code>true</code> if this context struct represents the top level reader within the hierarchical context */
  public final boolean isTopLevel;
  /** the doc base for this reader in the parent, <tt>0</tt> if parent is null */
  public final int docBaseInParent;
  /** the ord for this reader in the parent, <tt>0</tt> if parent is null */
  public final int ordInParent;
  
  IndexReaderContext(CompositeReaderContext parent, int ordInParent, int docBaseInParent) {
    if (!(this instanceof CompositeReaderContext || this instanceof LeafReaderContext))
      throw new Error("This class should never be extended by custom code!");
    this.parent = parent;
    this.docBaseInParent = docBaseInParent;
    this.ordInParent = ordInParent;
    this.isTopLevel = parent==null;
  }
  
  /** Returns the {@link IndexReader}, this context represents. */
  public abstract IndexReader reader();
  
  /**
   * Returns the context's leaves if this context is a top-level context.
   * For convenience, if this is an {@link LeafReaderContext} this
   * returns itself as the only leaf.
   * <p>Note: this is convenience method since leaves can always be obtained by
   * walking the context tree using {@link #children()}.
   * @throws UnsupportedOperationException if this is not a top-level context.
   * @see #children()
   */
  public abstract List<LeafReaderContext> leaves() throws UnsupportedOperationException;
  
  /**
   * Returns the context's children iff this context is a composite context
   * otherwise <code>null</code>.
   */
  public abstract List<IndexReaderContext> children();
}