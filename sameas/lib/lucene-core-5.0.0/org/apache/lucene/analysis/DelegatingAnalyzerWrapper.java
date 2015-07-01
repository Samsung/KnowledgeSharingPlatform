package org.apache.lucene.analysis;

import java.io.Reader;

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

/**
 * An analyzer wrapper, that doesn't allow to wrap components or readers.
 * By disallowing it, it means that the thread local resources can be delegated
 * to the delegate analyzer, and not also be allocated on this analyzer.
 * This wrapper class is the base class of all analyzers that just delegate to
 * another analyzer, e.g. per field name.
 * 
 * <p>This solves the problem of per field analyzer wrapper, where it also
 * maintains a thread local per field token stream components, while it can
 * safely delegate those and not also hold these data structures, which can
 * become expensive memory wise.
 * 
 * <p><b>Please note:</b> This analyzer uses a private {@link Analyzer.ReuseStrategy},
 * which is returned by {@link #getReuseStrategy()}. This strategy is used when
 * delegating. If you wrap this analyzer again and reuse this strategy, no
 * delegation is done and the given fallback is used.
 */
public abstract class DelegatingAnalyzerWrapper extends AnalyzerWrapper {
  
  /**
   * Constructor.
   * @param fallbackStrategy is the strategy to use if delegation is not possible
   *  This is to support the common pattern:
   *  {@code new OtherWrapper(thisWrapper.getReuseStrategy())} 
   */
  protected DelegatingAnalyzerWrapper(ReuseStrategy fallbackStrategy) {
    super(new DelegatingReuseStrategy(fallbackStrategy));
    // häckidy-hick-hack, because we cannot call super() with a reference to "this":
    ((DelegatingReuseStrategy) getReuseStrategy()).wrapper = this;
  }
  
  @Override
  protected final TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components) {
    return super.wrapComponents(fieldName, components);
  }
  
  @Override
  protected final Reader wrapReader(String fieldName, Reader reader) {
    return super.wrapReader(fieldName, reader);
  }
  
  private static final class DelegatingReuseStrategy extends ReuseStrategy {
    DelegatingAnalyzerWrapper wrapper;
    private final ReuseStrategy fallbackStrategy;
    
    DelegatingReuseStrategy(ReuseStrategy fallbackStrategy) {
      this.fallbackStrategy = fallbackStrategy;
    }
    
    @Override
    public TokenStreamComponents getReusableComponents(Analyzer analyzer, String fieldName) {
      if (analyzer == wrapper) {
        final Analyzer wrappedAnalyzer = wrapper.getWrappedAnalyzer(fieldName);
        return wrappedAnalyzer.getReuseStrategy().getReusableComponents(wrappedAnalyzer, fieldName);
      } else {
        return fallbackStrategy.getReusableComponents(analyzer, fieldName);
      }
    }

    @Override
    public void setReusableComponents(Analyzer analyzer, String fieldName,  TokenStreamComponents components) {
      if (analyzer == wrapper) {
        final Analyzer wrappedAnalyzer = wrapper.getWrappedAnalyzer(fieldName);
        wrappedAnalyzer.getReuseStrategy().setReusableComponents(wrappedAnalyzer, fieldName, components);
      } else {
        fallbackStrategy.setReusableComponents(analyzer, fieldName, components);
      }
    }
  };
  
}