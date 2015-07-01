package org.apache.lucene.queryparser.flexible.standard.processors;

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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.util.UnescapedCharSequence;
import org.apache.lucene.queryparser.flexible.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;
import org.apache.lucene.search.PrefixQuery;

/**
 * The {@link StandardSyntaxParser} creates {@link PrefixWildcardQueryNode} nodes which
 * have values containing the prefixed wildcard. However, Lucene
 * {@link PrefixQuery} cannot contain the prefixed wildcard. So, this processor
 * basically removed the prefixed wildcard from the
 * {@link PrefixWildcardQueryNode} value. <br/>
 * 
 * @see PrefixQuery
 * @see PrefixWildcardQueryNode
 */
public class WildcardQueryNodeProcessor extends QueryNodeProcessorImpl {

  public WildcardQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {

    // the old Lucene Parser ignores FuzzyQueryNode that are also PrefixWildcardQueryNode or WildcardQueryNode
    // we do the same here, also ignore empty terms
    if (node instanceof FieldQueryNode || node instanceof FuzzyQueryNode) {      
      FieldQueryNode fqn = (FieldQueryNode) node;      
      CharSequence text = fqn.getText(); 
      
      // do not process wildcards for TermRangeQueryNode children and 
      // QuotedFieldQueryNode to reproduce the old parser behavior
      if (fqn.getParent() instanceof TermRangeQueryNode 
          || fqn instanceof QuotedFieldQueryNode 
          || text.length() <= 0){
        // Ignore empty terms
        return node;
      }
      
      // Code below simulates the old lucene parser behavior for wildcards
      
      if (isPrefixWildcard(text)) {        
        PrefixWildcardQueryNode prefixWildcardQN = new PrefixWildcardQueryNode(fqn);
        return prefixWildcardQN;
        
      } else if (isWildcard(text)){
        WildcardQueryNode wildcardQN = new WildcardQueryNode(fqn);
        return wildcardQN;
      }
             
    }

    return node;

  }

  private boolean isWildcard(CharSequence text) {
    if (text ==null || text.length() <= 0) return false;
    
    // If a un-escaped '*' or '?' if found return true
    // start at the end since it's more common to put wildcards at the end
    for(int i=text.length()-1; i>=0; i--){
      if ((text.charAt(i) == '*' || text.charAt(i) == '?') && !UnescapedCharSequence.wasEscaped(text, i)){
        return true;
      }
    }
    
    return false;
  }

  private boolean isPrefixWildcard(CharSequence text) {
    if (text == null || text.length() <= 0 || !isWildcard(text)) return false;
    
    // Validate last character is a '*' and was not escaped
    // If single '*' is is a wildcard not prefix to simulate old queryparser
    if (text.charAt(text.length()-1) != '*') return false;
    if (UnescapedCharSequence.wasEscaped(text, text.length()-1)) return false;
    if (text.length() == 1) return false;
      
    // Only make a prefix if there is only one single star at the end and no '?' or '*' characters
    // If single wildcard return false to mimic old queryparser
    for(int i=0; i<text.length(); i++){
      if (text.charAt(i) == '?') return false;
      if (text.charAt(i) == '*' && !UnescapedCharSequence.wasEscaped(text, i)){        
        if (i == text.length()-1) 
          return true;
        else 
          return false;
      }
    }
    
    return false;
  }

  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {

    return node;

  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {

    return children;

  }

}
