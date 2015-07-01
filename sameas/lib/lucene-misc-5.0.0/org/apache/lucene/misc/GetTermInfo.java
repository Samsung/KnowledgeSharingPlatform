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

import java.nio.file.Paths;
import java.util.Locale;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

/**
 * Utility to get document frequency and total number of occurrences (sum of the tf for each doc)  of a term. 
 */
public class GetTermInfo {
  
  public static void main(String[] args) throws Exception {
    
    FSDirectory dir = null;
    String inputStr = null;
    String field = null;
    
    if (args.length == 3) {
      dir = FSDirectory.open(Paths.get(args[0]));
      field = args[1];
      inputStr = args[2];
    } else {
      usage();
      System.exit(1);
    }
      
    getTermInfo(dir,new Term(field, inputStr));
  }
  
  public static void getTermInfo(Directory dir, Term term) throws Exception {
    IndexReader reader = DirectoryReader.open(dir);
    System.out.printf(Locale.ROOT, "%s:%s \t totalTF = %,d \t doc freq = %,d \n",
         term.field(), term.text(), reader.totalTermFreq(term), reader.docFreq(term)); 
  }
   
  private static void usage() {
    System.out
        .println("\n\nusage:\n\t"
            + "java " + GetTermInfo.class.getName() + " <index dir> field term \n\n");
  }
}
