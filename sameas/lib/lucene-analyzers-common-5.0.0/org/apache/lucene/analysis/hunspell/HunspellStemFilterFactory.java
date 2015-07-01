package org.apache.lucene.analysis.hunspell;

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
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.IOUtils;

/**
 * TokenFilterFactory that creates instances of {@link HunspellStemFilter}.
 * Example config for British English:
 * <pre class="prettyprint">
 * &lt;filter class=&quot;solr.HunspellStemFilterFactory&quot;
 *         dictionary=&quot;en_GB.dic,my_custom.dic&quot;
 *         affix=&quot;en_GB.aff&quot; 
 *         ignoreCase=&quot;false&quot;
 *         longestOnly=&quot;false&quot; /&gt;</pre>
 * Both parameters dictionary and affix are mandatory.
 * Dictionaries for many languages are available through the OpenOffice project.
 * 
 * See <a href="http://wiki.apache.org/solr/Hunspell">http://wiki.apache.org/solr/Hunspell</a>
 * @lucene.experimental
 */
public class HunspellStemFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
  private static final String PARAM_DICTIONARY    = "dictionary";
  private static final String PARAM_AFFIX         = "affix";
  private static final String PARAM_RECURSION_CAP = "recursionCap";
  private static final String PARAM_IGNORE_CASE   = "ignoreCase";
  private static final String PARAM_LONGEST_ONLY  = "longestOnly";

  private final String dictionaryFiles;
  private final String affixFile;
  private final boolean ignoreCase;
  private final boolean longestOnly;
  private Dictionary dictionary;
  
  /** Creates a new HunspellStemFilterFactory */
  public HunspellStemFilterFactory(Map<String,String> args) {
    super(args);
    dictionaryFiles = require(args, PARAM_DICTIONARY);
    affixFile = get(args, PARAM_AFFIX);
    ignoreCase = getBoolean(args, PARAM_IGNORE_CASE, false);
    longestOnly = getBoolean(args, PARAM_LONGEST_ONLY, false);
    // this isnt necessary: we properly load all dictionaries.
    // but recognize and ignore for back compat
    getBoolean(args, "strictAffixParsing", true);
    // this isn't necessary: multi-stage stripping is fixed and 
    // flags like COMPLEXPREFIXES in the data itself control this.
    // but recognize and ignore for back compat
    getInt(args, "recursionCap", 0);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }

  @Override
  public void inform(ResourceLoader loader) throws IOException {
    String dicts[] = dictionaryFiles.split(",");

    InputStream affix = null;
    List<InputStream> dictionaries = new ArrayList<>();

    try {
      dictionaries = new ArrayList<>();
      for (String file : dicts) {
        dictionaries.add(loader.openResource(file));
      }
      affix = loader.openResource(affixFile);

      this.dictionary = new Dictionary(affix, dictionaries, ignoreCase);
    } catch (ParseException e) {
      throw new IOException("Unable to load hunspell data! [dictionary=" + dictionaries + ",affix=" + affixFile + "]", e);
    } finally {
      IOUtils.closeWhileHandlingException(affix);
      IOUtils.closeWhileHandlingException(dictionaries);
    }
  }

  @Override
  public TokenStream create(TokenStream tokenStream) {
    return new HunspellStemFilter(tokenStream, dictionary, true, longestOnly);
  }
}
