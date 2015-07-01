package org.apache.lucene.analysis;

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

import java.io.Reader;

/**
 * Extension to {@link Analyzer} suitable for Analyzers which wrap
 * other Analyzers.
 * 
 * <p>{@link #getWrappedAnalyzer(String)} allows the Analyzer
 * to wrap multiple Analyzers which are selected on a per field basis.
 * 
 * <p>{@link #wrapComponents(String, Analyzer.TokenStreamComponents)} allows the
 * TokenStreamComponents of the wrapped Analyzer to then be wrapped
 * (such as adding a new {@link TokenFilter} to form new TokenStreamComponents.
 *
 * <p>{@link #wrapReader(String, Reader)} allows the Reader of the wrapped
 * Analyzer to then be wrapped (such as adding a new {@link CharFilter}.
 *
 * <p><b>Important:</b> If you do not want to wrap the TokenStream
 * using {@link #wrapComponents(String, Analyzer.TokenStreamComponents)}
 * or the Reader using {@link #wrapReader(String, Reader)} and just delegate
 * to other analyzers (like by field name), use {@link DelegatingAnalyzerWrapper}
 * as superclass!
 *
 * @see DelegatingAnalyzerWrapper
 */
public abstract class AnalyzerWrapper extends Analyzer {

  /**
   * Creates a new AnalyzerWrapper with the given reuse strategy.
   * <p>If you want to wrap a single delegate Analyzer you can probably
   * reuse its strategy when instantiating this subclass:
   * {@code super(delegate.getReuseStrategy());}.
   * <p>If you choose different analyzers per field, use
   * {@link #PER_FIELD_REUSE_STRATEGY}.
   * @see #getReuseStrategy()
   */
  protected AnalyzerWrapper(ReuseStrategy reuseStrategy) {
    super(reuseStrategy);
  }

  /**
   * Retrieves the wrapped Analyzer appropriate for analyzing the field with
   * the given name
   *
   * @param fieldName Name of the field which is to be analyzed
   * @return Analyzer for the field with the given name.  Assumed to be non-null
   */
  protected abstract Analyzer getWrappedAnalyzer(String fieldName);

  /**
   * Wraps / alters the given TokenStreamComponents, taken from the wrapped
   * Analyzer, to form new components. It is through this method that new
   * TokenFilters can be added by AnalyzerWrappers. By default, the given
   * components are returned.
   * 
   * @param fieldName
   *          Name of the field which is to be analyzed
   * @param components
   *          TokenStreamComponents taken from the wrapped Analyzer
   * @return Wrapped / altered TokenStreamComponents.
   */
  protected TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components) {
    return components;
  }

  /**
   * Wraps / alters the given Reader. Through this method AnalyzerWrappers can
   * implement {@link #initReader(String, Reader)}. By default, the given reader
   * is returned.
   * 
   * @param fieldName
   *          name of the field which is to be analyzed
   * @param reader
   *          the reader to wrap
   * @return the wrapped reader
   */
  protected Reader wrapReader(String fieldName, Reader reader) {
    return reader;
  }
  
  @Override
  protected final TokenStreamComponents createComponents(String fieldName) {
    return wrapComponents(fieldName, getWrappedAnalyzer(fieldName).createComponents(fieldName));
  }

  @Override
  public int getPositionIncrementGap(String fieldName) {
    return getWrappedAnalyzer(fieldName).getPositionIncrementGap(fieldName);
  }

  @Override
  public int getOffsetGap(String fieldName) {
    return getWrappedAnalyzer(fieldName).getOffsetGap(fieldName);
  }

  @Override
  public final Reader initReader(String fieldName, Reader reader) {
    return getWrappedAnalyzer(fieldName).initReader(fieldName, wrapReader(fieldName, reader));
  }
}
