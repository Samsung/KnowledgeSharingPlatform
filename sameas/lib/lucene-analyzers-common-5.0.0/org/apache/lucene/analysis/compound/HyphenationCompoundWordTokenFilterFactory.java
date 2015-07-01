package org.apache.lucene.analysis.compound;

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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.IOUtils;

import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.util.Version;
import org.xml.sax.InputSource;

/**
 * Factory for {@link HyphenationCompoundWordTokenFilter}.
 * <p>
 * This factory accepts the following parameters:
 * <ul>
 *  <li><code>hyphenator</code> (mandatory): path to the FOP xml hyphenation pattern. 
 *  See <a href="http://offo.sourceforge.net/hyphenation/">http://offo.sourceforge.net/hyphenation/</a>.
 *  <li><code>encoding</code> (optional): encoding of the xml hyphenation file. defaults to UTF-8.
 *  <li><code>dictionary</code> (optional): dictionary of words. defaults to no dictionary.
 *  <li><code>minWordSize</code> (optional): minimal word length that gets decomposed. defaults to 5.
 *  <li><code>minSubwordSize</code> (optional): minimum length of subwords. defaults to 2.
 *  <li><code>maxSubwordSize</code> (optional): maximum length of subwords. defaults to 15.
 *  <li><code>onlyLongestMatch</code> (optional): if true, adds only the longest matching subword 
 *    to the stream. defaults to false.
 * </ul>
 * <p>
 * <pre class="prettyprint">
 * &lt;fieldType name="text_hyphncomp" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.WhitespaceTokenizerFactory"/&gt;
 *     &lt;filter class="solr.HyphenationCompoundWordTokenFilterFactory" hyphenator="hyphenator.xml" encoding="UTF-8"
 *         dictionary="dictionary.txt" minWordSize="5" minSubwordSize="2" maxSubwordSize="15" onlyLongestMatch="false"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 *
 * @see HyphenationCompoundWordTokenFilter
 */
public class HyphenationCompoundWordTokenFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
  private CharArraySet dictionary;
  private HyphenationTree hyphenator;
  private final String dictFile;
  private final String hypFile;
  private final String encoding;
  private final int minWordSize;
  private final int minSubwordSize;
  private final int maxSubwordSize;
  private final boolean onlyLongestMatch;
  
  /** Creates a new HyphenationCompoundWordTokenFilterFactory */
  public HyphenationCompoundWordTokenFilterFactory(Map<String, String> args) {
    super(args);
    dictFile = get(args, "dictionary");
    encoding = get(args, "encoding");
    hypFile = require(args, "hyphenator");
    minWordSize = getInt(args, "minWordSize", CompoundWordTokenFilterBase.DEFAULT_MIN_WORD_SIZE);
    minSubwordSize = getInt(args, "minSubwordSize", CompoundWordTokenFilterBase.DEFAULT_MIN_SUBWORD_SIZE);
    maxSubwordSize = getInt(args, "maxSubwordSize", CompoundWordTokenFilterBase.DEFAULT_MAX_SUBWORD_SIZE);
    onlyLongestMatch = getBoolean(args, "onlyLongestMatch", false);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }
  
  @Override
  public void inform(ResourceLoader loader) throws IOException {
    InputStream stream = null;
    try {
      if (dictFile != null) // the dictionary can be empty.
        dictionary = getWordSet(loader, dictFile, false);
      // TODO: Broken, because we cannot resolve real system id
      // ResourceLoader should also supply method like ClassLoader to get resource URL
      stream = loader.openResource(hypFile);
      final InputSource is = new InputSource(stream);
      is.setEncoding(encoding); // if it's null let xml parser decide
      is.setSystemId(hypFile);
      if (luceneMatchVersion.onOrAfter(Version.LUCENE_4_4_0)) {
        hyphenator = HyphenationCompoundWordTokenFilter.getHyphenationTree(is);
      } else {
        hyphenator = Lucene43HyphenationCompoundWordTokenFilter.getHyphenationTree(is);
      }
    } finally {
      IOUtils.closeWhileHandlingException(stream);
    }
  }
  
  @Override
  public TokenFilter create(TokenStream input) {
    if (luceneMatchVersion.onOrAfter(Version.LUCENE_4_4_0)) {
      return new HyphenationCompoundWordTokenFilter(input, hyphenator, dictionary, minWordSize, minSubwordSize, maxSubwordSize, onlyLongestMatch);
    }
    return new Lucene43HyphenationCompoundWordTokenFilter(input, hyphenator, dictionary, minWordSize, minSubwordSize, maxSubwordSize, onlyLongestMatch);
  }
}
