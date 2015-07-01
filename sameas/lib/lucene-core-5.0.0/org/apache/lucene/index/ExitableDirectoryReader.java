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

import org.apache.lucene.index.FilterLeafReader.FilterFields;
import org.apache.lucene.index.FilterLeafReader.FilterTerms;
import org.apache.lucene.index.FilterLeafReader.FilterTermsEnum;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.CompiledAutomaton;

import java.io.IOException;


/**
 * The {@link ExitableDirectoryReader} wraps a real index {@link DirectoryReader} and
 * allows for a {@link QueryTimeout} implementation object to be checked periodically
 * to see if the thread should exit or not.  If {@link QueryTimeout#shouldExit()}
 * returns true, an {@link ExitingReaderException} is thrown.
 */
public class ExitableDirectoryReader extends FilterDirectoryReader {
  
  private QueryTimeout queryTimeout;

  /**
   * Exception that is thrown to prematurely terminate a term enumeration.
   */
  @SuppressWarnings("serial")
  public static class ExitingReaderException extends RuntimeException {

    /** Constructor **/
    ExitingReaderException(String msg) {
      super(msg);
    }
  }

  /**
   * Wrapper class for a SubReaderWrapper that is used by the ExitableDirectoryReader.
   */
  public static class ExitableSubReaderWrapper extends SubReaderWrapper {
    private QueryTimeout queryTimeout;

    /** Constructor **/
    public ExitableSubReaderWrapper(QueryTimeout queryTimeout) {
      this.queryTimeout = queryTimeout;
    }

    @Override
    public LeafReader wrap(LeafReader reader) {
      return new ExitableFilterAtomicReader(reader, queryTimeout);
    }
  }

  /**
   * Wrapper class for another FilterAtomicReader. This is used by ExitableSubReaderWrapper.
   */
  public static class ExitableFilterAtomicReader extends FilterLeafReader {

    private QueryTimeout queryTimeout;
    
    /** Constructor **/
    public ExitableFilterAtomicReader(LeafReader in, QueryTimeout queryTimeout) {
      super(in);
      this.queryTimeout = queryTimeout;
    }

    @Override
    public Fields fields() throws IOException {
      return new ExitableFields(super.fields(), queryTimeout);
    }
    
    @Override
    public Object getCoreCacheKey() {
      return in.getCoreCacheKey();  
    }
    
    @Override
    public Object getCombinedCoreAndDeletesKey() {
      return in.getCombinedCoreAndDeletesKey();
    }
    
  }

  /**
   * Wrapper class for another Fields implementation that is used by the ExitableFilterAtomicReader.
   */
  public static class ExitableFields extends FilterFields {
    
    private QueryTimeout queryTimeout;

    /** Constructor **/
    public ExitableFields(Fields fields, QueryTimeout queryTimeout) {
      super(fields);
      this.queryTimeout = queryTimeout;
    }

    @Override
    public Terms terms(String field) throws IOException {
      Terms terms = in.terms(field);
      if (terms == null) {
        return null;
      }
      return new ExitableTerms(terms, queryTimeout);
    }
  }

  /**
   * Wrapper class for another Terms implementation that is used by ExitableFields.
   */
  public static class ExitableTerms extends FilterTerms {

    private QueryTimeout queryTimeout;
    
    /** Constructor **/
    public ExitableTerms(Terms terms, QueryTimeout queryTimeout) {
      super(terms);
      this.queryTimeout = queryTimeout;
    }

    @Override
    public TermsEnum intersect(CompiledAutomaton compiled, BytesRef startTerm) throws IOException {
      return new ExitableTermsEnum(in.intersect(compiled, startTerm), queryTimeout);
    }

    @Override
    public TermsEnum iterator(TermsEnum reuse) throws IOException {
      return new ExitableTermsEnum(in.iterator(reuse), queryTimeout);
    }
  }

  /**
   * Wrapper class for TermsEnum that is used by ExitableTerms for implementing an
   * exitable enumeration of terms.
   */
  public static class ExitableTermsEnum extends FilterTermsEnum {

    private QueryTimeout queryTimeout;
    
    /** Constructor **/
    public ExitableTermsEnum(TermsEnum termsEnum, QueryTimeout queryTimeout) {
      super(termsEnum);
      this.queryTimeout = queryTimeout;
      checkAndThrow();
    }

    /**
     * Throws {@link ExitingReaderException} if {@link QueryTimeout#shouldExit()} returns true,
     * or if {@link Thread#interrupted()} returns true.
     */
    private void checkAndThrow() {
      if (queryTimeout.shouldExit()) {
        throw new ExitingReaderException("The request took too long to iterate over terms. Timeout: " 
            + queryTimeout.toString()
            + ", TermsEnum=" + in
        );
      } else if (Thread.interrupted()) {
        throw new ExitingReaderException("Interrupted while iterating over terms. TermsEnum=" + in);
      }
    }

    @Override
    public BytesRef next() throws IOException {
      // Before every iteration, check if the iteration should exit
      checkAndThrow();
      return in.next();
    }
  }

  /**
   * Constructor
   * @param in DirectoryReader that this ExitableDirectoryReader wraps around to make it Exitable.
   * @param queryTimeout The object to periodically check if the query should time out.
   */
  public ExitableDirectoryReader(DirectoryReader in, QueryTimeout queryTimeout) {
    super(in, new ExitableSubReaderWrapper(queryTimeout));
    this.queryTimeout = queryTimeout;
  }

  @Override
  protected DirectoryReader doWrapDirectoryReader(DirectoryReader in) {
    return new ExitableDirectoryReader(in, queryTimeout);
  }

  /**
   * Wraps a provided DirectoryReader. Note that for convenience, the returned reader
   * can be used normally (e.g. passed to {@link DirectoryReader#openIfChanged(DirectoryReader)})
   * and so on.
   */
  public static DirectoryReader wrap(DirectoryReader in, QueryTimeout queryTimeout) {
    return new ExitableDirectoryReader(in, queryTimeout);
  }

  @Override
  public String toString() {
    return "ExitableDirectoryReader(" + in.toString() + ")";
  }
}


