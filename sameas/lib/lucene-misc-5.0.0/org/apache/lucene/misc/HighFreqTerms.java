package org.apache.lucene.misc;

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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;

/**
 * <code>HighFreqTerms</code> class extracts the top n most frequent terms
 * (by document frequency) from an existing Lucene index and reports their
 * document frequency.
 * <p>
 * If the -t flag is given, both document frequency and total tf (total
 * number of occurrences) are reported, ordered by descending total tf.
 *
 */
public class HighFreqTerms {
  
  // The top numTerms will be displayed
  public static final int DEFAULT_NUMTERMS = 100;
  
  public static void main(String[] args) throws Exception {
    String field = null;
    int numTerms = DEFAULT_NUMTERMS;
   
    if (args.length == 0 || args.length > 4) {
      usage();
      System.exit(1);
    }     

    Directory dir = FSDirectory.open(Paths.get(args[0]));
    
    Comparator<TermStats> comparator = new DocFreqComparator();
   
    for (int i = 1; i < args.length; i++) {
      if (args[i].equals("-t")) {
        comparator = new TotalTermFreqComparator();
      }
      else{
        try {
          numTerms = Integer.parseInt(args[i]);
        } catch (NumberFormatException e) {
          field=args[i];
        }
      }
    }
    
    IndexReader reader = DirectoryReader.open(dir);
    TermStats[] terms = getHighFreqTerms(reader, numTerms, field, comparator);

    for (int i = 0; i < terms.length; i++) {
      System.out.printf(Locale.ROOT, "%s:%s \t totalTF = %,d \t docFreq = %,d \n",
            terms[i].field, terms[i].termtext.utf8ToString(), terms[i].totalTermFreq, terms[i].docFreq);
    }
    reader.close();
  }
  
  private static void usage() {
    System.out
        .println("\n\n"
            + "java org.apache.lucene.misc.HighFreqTerms <index dir> [-t] [number_terms] [field]\n\t -t: order by totalTermFreq\n\n");
  }
  
  /**
   * Returns TermStats[] ordered by the specified comparator
   */
  public static TermStats[] getHighFreqTerms(IndexReader reader, int numTerms, String field, Comparator<TermStats> comparator) throws Exception {
    TermStatsQueue tiq = null;
    
    if (field != null) {
      Terms terms = MultiFields.getTerms(reader, field);
      if (terms == null) {
        throw new RuntimeException("field " + field + " not found");
      }

      TermsEnum termsEnum = terms.iterator(null);
      tiq = new TermStatsQueue(numTerms, comparator);
      tiq.fill(field, termsEnum);
    } else {
      Fields fields = MultiFields.getFields(reader);
      if (fields.size() == 0) {
        throw new RuntimeException("no fields found for this index");
      }
      tiq = new TermStatsQueue(numTerms, comparator);
      for (String fieldName : fields) {
        Terms terms = fields.terms(fieldName);
        if (terms != null) {
          tiq.fill(fieldName, terms.iterator(null));
        }
      }
    }
    
    TermStats[] result = new TermStats[tiq.size()];
    // we want highest first so we read the queue and populate the array
    // starting at the end and work backwards
    int count = tiq.size() - 1;
    while (tiq.size() != 0) {
      result[count] = tiq.pop();
      count--;
    }
    return result;
  }
  
  /**
   * Compares terms by docTermFreq
   */
  public static final class DocFreqComparator implements Comparator<TermStats> {
    
    @Override
    public int compare(TermStats a, TermStats b) {
      int res = Long.compare(a.docFreq, b.docFreq);
      if (res == 0) {
        res = a.field.compareTo(b.field);
        if (res == 0) {
          res = a.termtext.compareTo(b.termtext);
        }
      }
      return res;
    }
  }

  /**
   * Compares terms by totalTermFreq
   */
  public static final class TotalTermFreqComparator implements Comparator<TermStats> {
    
    @Override
    public int compare(TermStats a, TermStats b) {
      int res = Long.compare(a.totalTermFreq, b.totalTermFreq);
      if (res == 0) {
        res = a.field.compareTo(b.field);
        if (res == 0) {
          res = a.termtext.compareTo(b.termtext);
        }
      }
      return res;
    }
  }
  
  /**
   * Priority queue for TermStats objects
   **/
  static final class TermStatsQueue extends PriorityQueue<TermStats> {
    final Comparator<TermStats> comparator;
    
    TermStatsQueue(int size, Comparator<TermStats> comparator) {
      super(size);
      this.comparator = comparator;
    }
    
    @Override
    protected boolean lessThan(TermStats termInfoA, TermStats termInfoB) {
      return comparator.compare(termInfoA, termInfoB) < 0;
    }
    
    protected void fill(String field, TermsEnum termsEnum) throws IOException {
      BytesRef term = null;
      while ((term = termsEnum.next()) != null) {
        insertWithOverflow(new TermStats(field, term, termsEnum.docFreq(), termsEnum.totalTermFreq()));
      }
    }
  }
}
