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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.RAMFile;
import org.apache.lucene.store.RAMInputStream;
import org.apache.lucene.store.RAMOutputStream;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;

/**
 * Prefix codes term instances (prefixes are shared)
 * @lucene.experimental
 */
class PrefixCodedTerms implements Iterable<Term>, Accountable {
  final RAMFile buffer;
  
  private PrefixCodedTerms(RAMFile buffer) {
    this.buffer = buffer;
  }

  @Override
  public long ramBytesUsed() {
    return buffer.ramBytesUsed();
  }
  
  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.emptyList();
  }

  /** @return iterator over the bytes */
  @Override
  public Iterator<Term> iterator() {
    return new PrefixCodedTermsIterator();
  }
  
  class PrefixCodedTermsIterator implements Iterator<Term> {
    final IndexInput input;
    String field = "";
    BytesRefBuilder bytes = new BytesRefBuilder();
    Term term = new Term(field, bytes.get());

    PrefixCodedTermsIterator() {
      try {
        input = new RAMInputStream("PrefixCodedTermsIterator", buffer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      return input.getFilePointer() < input.length();
    }
    
    @Override
    public Term next() {
      assert hasNext();
      try {
        int code = input.readVInt();
        if ((code & 1) != 0) {
          // new field
          field = input.readString();
        }
        int prefix = code >>> 1;
        int suffix = input.readVInt();
        bytes.grow(prefix + suffix);
        input.readBytes(bytes.bytes(), prefix, suffix);
        bytes.setLength(prefix + suffix);
        term.set(field, bytes.get());
        return term;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  /** Builds a PrefixCodedTerms: call add repeatedly, then finish. */
  public static class Builder {
    private RAMFile buffer = new RAMFile();
    private RAMOutputStream output = new RAMOutputStream(buffer, false);
    private Term lastTerm = new Term("");
    private BytesRefBuilder lastTermBytes = new BytesRefBuilder();

    /** add a term */
    public void add(Term term) {
      assert lastTerm.equals(new Term("")) || term.compareTo(lastTerm) > 0;

      try {
        int prefix = sharedPrefix(lastTerm.bytes, term.bytes);
        int suffix = term.bytes.length - prefix;
        if (term.field.equals(lastTerm.field)) {
          output.writeVInt(prefix << 1);
        } else {
          output.writeVInt(prefix << 1 | 1);
          output.writeString(term.field);
        }
        output.writeVInt(suffix);
        output.writeBytes(term.bytes.bytes, term.bytes.offset + prefix, suffix);
        lastTermBytes.copyBytes(term.bytes);
        lastTerm.bytes = lastTermBytes.get();
        lastTerm.field = term.field;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    /** return finalized form */
    public PrefixCodedTerms finish() {
      try {
        output.close();
        return new PrefixCodedTerms(buffer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    private int sharedPrefix(BytesRef term1, BytesRef term2) {
      int pos1 = 0;
      int pos1End = pos1 + Math.min(term1.length, term2.length);
      int pos2 = 0;
      while(pos1 < pos1End) {
        if (term1.bytes[term1.offset + pos1] != term2.bytes[term2.offset + pos2]) {
          return pos1;
        }
        pos1++;
        pos2++;
      }
      return pos1;
    }
  }
}
