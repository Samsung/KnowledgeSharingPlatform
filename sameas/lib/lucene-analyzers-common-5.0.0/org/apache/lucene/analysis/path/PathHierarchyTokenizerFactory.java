package org.apache.lucene.analysis.path;

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

import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 * Factory for {@link PathHierarchyTokenizer}. 
 * <p>
 * This factory is typically configured for use only in the <code>index</code> 
 * Analyzer (or only in the <code>query</code> Analyzer, but never both).
 * </p>
 * <p>
 * For example, in the configuration below a query for 
 * <code>Books/NonFic</code> will match documents indexed with values like 
 * <code>Books/NonFic</code>, <code>Books/NonFic/Law</code>, 
 * <code>Books/NonFic/Science/Physics</code>, etc. But it will not match 
 * documents indexed with values like <code>Books</code>, or 
 * <code>Books/Fic</code>...
 * </p>
 *
 * <pre class="prettyprint">
 * &lt;fieldType name="descendent_path" class="solr.TextField"&gt;
 *   &lt;analyzer type="index"&gt;
 *     &lt;tokenizer class="solr.PathHierarchyTokenizerFactory" delimiter="/" /&gt;
 *   &lt;/analyzer&gt;
 *   &lt;analyzer type="query"&gt;
 *     &lt;tokenizer class="solr.KeywordTokenizerFactory" /&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;
 * </pre>
 * <p>
 * In this example however we see the oposite configuration, so that a query 
 * for <code>Books/NonFic/Science/Physics</code> would match documents 
 * containing <code>Books/NonFic</code>, <code>Books/NonFic/Science</code>, 
 * or <code>Books/NonFic/Science/Physics</code>, but not 
 * <code>Books/NonFic/Science/Physics/Theory</code> or 
 * <code>Books/NonFic/Law</code>.
 * </p>
 * <pre class="prettyprint">
 * &lt;fieldType name="descendent_path" class="solr.TextField"&gt;
 *   &lt;analyzer type="index"&gt;
 *     &lt;tokenizer class="solr.KeywordTokenizerFactory" /&gt;
 *   &lt;/analyzer&gt;
 *   &lt;analyzer type="query"&gt;
 *     &lt;tokenizer class="solr.PathHierarchyTokenizerFactory" delimiter="/" /&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;
 * </pre>
 */
public class PathHierarchyTokenizerFactory extends TokenizerFactory {
  private final char delimiter;
  private final char replacement;
  private final boolean reverse;
  private final int skip;
  
  /** Creates a new PathHierarchyTokenizerFactory */
  public PathHierarchyTokenizerFactory(Map<String,String> args) {
    super(args);
    delimiter = getChar(args, "delimiter", PathHierarchyTokenizer.DEFAULT_DELIMITER);
    replacement = getChar(args, "replace", delimiter);
    reverse = getBoolean(args, "reverse", false);
    skip = getInt(args, "skip", PathHierarchyTokenizer.DEFAULT_SKIP);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }
  
  @Override
  public Tokenizer create(AttributeFactory factory) {
    if (reverse) {
      return new ReversePathHierarchyTokenizer(factory, delimiter, replacement, skip);
    }
    return new PathHierarchyTokenizer(factory, delimiter, replacement, skip);
  }
}


