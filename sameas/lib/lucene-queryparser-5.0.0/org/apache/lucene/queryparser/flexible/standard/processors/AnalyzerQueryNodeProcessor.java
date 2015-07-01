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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode.Modifier;
import org.apache.lucene.queryparser.flexible.core.nodes.NoTokenFoundQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TextableQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.StandardBooleanQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;

/**
 * This processor verifies if {@link ConfigurationKeys#ANALYZER}
 * is defined in the {@link QueryConfigHandler}. If it is and the analyzer is
 * not <code>null</code>, it looks for every {@link FieldQueryNode} that is not
 * {@link WildcardQueryNode}, {@link FuzzyQueryNode} or
 * {@link RangeQueryNode} contained in the query node tree, then it applies
 * the analyzer to that {@link FieldQueryNode} object. <br/>
 * <br/>
 * If the analyzer return only one term, the returned term is set to the
 * {@link FieldQueryNode} and it's returned. <br/>
 * <br/>
 * If the analyzer return more than one term, a {@link TokenizedPhraseQueryNode}
 * or {@link MultiPhraseQueryNode} is created, whether there is one or more
 * terms at the same position, and it's returned. <br/>
 * <br/>
 * If no term is returned by the analyzer a {@link NoTokenFoundQueryNode} object
 * is returned. <br/>
 * 
 * @see ConfigurationKeys#ANALYZER
 * @see Analyzer
 * @see TokenStream
 */
public class AnalyzerQueryNodeProcessor extends QueryNodeProcessorImpl {

  private Analyzer analyzer;

  private boolean positionIncrementsEnabled;
  
  private Operator defaultOperator;

  public AnalyzerQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    Analyzer analyzer = getQueryConfigHandler().get(ConfigurationKeys.ANALYZER);
    
    if (analyzer != null) {
      this.analyzer = analyzer;
      this.positionIncrementsEnabled = false;
      Boolean positionIncrementsEnabled = getQueryConfigHandler().get(ConfigurationKeys.ENABLE_POSITION_INCREMENTS);
      Operator defaultOperator = getQueryConfigHandler().get(ConfigurationKeys.DEFAULT_OPERATOR);
      this.defaultOperator = defaultOperator != null ? defaultOperator : Operator.OR;
      
      if (positionIncrementsEnabled != null) {
          this.positionIncrementsEnabled = positionIncrementsEnabled;
      }

      if (this.analyzer != null) {
        return super.process(queryTree);
      }
    }

    return queryTree;

  }

  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {

    if (node instanceof TextableQueryNode
        && !(node instanceof WildcardQueryNode)
        && !(node instanceof FuzzyQueryNode)
        && !(node instanceof RegexpQueryNode)
        && !(node.getParent() instanceof RangeQueryNode)) {

      FieldQueryNode fieldNode = ((FieldQueryNode) node);
      String text = fieldNode.getTextAsString();
      String field = fieldNode.getFieldAsString();

      CachingTokenFilter buffer = null;
      PositionIncrementAttribute posIncrAtt = null;
      int numTokens = 0;
      int positionCount = 0;
      boolean severalTokensAtSamePosition = false;
      
      try {
        try (TokenStream source = this.analyzer.tokenStream(field, text)) {
          buffer = new CachingTokenFilter(source);
          buffer.reset();

          if (buffer.hasAttribute(PositionIncrementAttribute.class)) {
            posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);
          }
  
          try {
  
            while (buffer.incrementToken()) {
              numTokens++;
              int positionIncrement = (posIncrAtt != null) ? posIncrAtt
                  .getPositionIncrement() : 1;
              if (positionIncrement != 0) {
                positionCount += positionIncrement;
  
              } else {
                severalTokensAtSamePosition = true;
              }
  
            }
  
          } catch (IOException e) {
            // ignore
          }

          // rewind the buffer stream
          buffer.reset();//will never through on subsequent reset calls
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        if (!buffer.hasAttribute(CharTermAttribute.class)) {
          return new NoTokenFoundQueryNode();
        }
  
        CharTermAttribute termAtt = buffer.getAttribute(CharTermAttribute.class);
  
        if (numTokens == 0) {
          return new NoTokenFoundQueryNode();
  
        } else if (numTokens == 1) {
          String term = null;
          try {
            boolean hasNext;
            hasNext = buffer.incrementToken();
            assert hasNext == true;
            term = termAtt.toString();
  
          } catch (IOException e) {
            // safe to ignore, because we know the number of tokens
          }
  
          fieldNode.setText(term);
  
          return fieldNode;
  
        } else if (severalTokensAtSamePosition || !(node instanceof QuotedFieldQueryNode)) {
          if (positionCount == 1 || !(node instanceof QuotedFieldQueryNode)) {
            // no phrase query:
            
            if (positionCount == 1) {
              // simple case: only one position, with synonyms
              LinkedList<QueryNode> children = new LinkedList<>();
              
              for (int i = 0; i < numTokens; i++) {
                String term = null;
                try {
                  boolean hasNext = buffer.incrementToken();
                  assert hasNext == true;
                  term = termAtt.toString();
                  
                } catch (IOException e) {
                  // safe to ignore, because we know the number of tokens
                }
                
                children.add(new FieldQueryNode(field, term, -1, -1));
                
              }
              return new GroupQueryNode(
                  new StandardBooleanQueryNode(children, positionCount==1));
            } else {
              // multiple positions
              QueryNode q = new StandardBooleanQueryNode(Collections.<QueryNode>emptyList(),false);
              QueryNode currentQuery = null;
              for (int i = 0; i < numTokens; i++) {
                String term = null;
                try {
                  boolean hasNext = buffer.incrementToken();
                  assert hasNext == true;
                  term = termAtt.toString();
                } catch (IOException e) {
                  // safe to ignore, because we know the number of tokens
                }
                if (posIncrAtt != null && posIncrAtt.getPositionIncrement() == 0) {
                  if (!(currentQuery instanceof BooleanQueryNode)) {
                    QueryNode t = currentQuery;
                    currentQuery = new StandardBooleanQueryNode(Collections.<QueryNode>emptyList(), true);
                    ((BooleanQueryNode)currentQuery).add(t);
                  }
                  ((BooleanQueryNode)currentQuery).add(new FieldQueryNode(field, term, -1, -1));
                } else {
                  if (currentQuery != null) {
                    if (this.defaultOperator == Operator.OR) {
                      q.add(currentQuery);
                    } else {
                      q.add(new ModifierQueryNode(currentQuery, Modifier.MOD_REQ));
                    }
                  }
                  currentQuery = new FieldQueryNode(field, term, -1, -1);
                }
              }
              if (this.defaultOperator == Operator.OR) {
                q.add(currentQuery);
              } else {
                q.add(new ModifierQueryNode(currentQuery, Modifier.MOD_REQ));
              }
              
              if (q instanceof BooleanQueryNode) {
                q = new GroupQueryNode(q);
              }
              return q;
            }
          } else {
            // phrase query:
            MultiPhraseQueryNode mpq = new MultiPhraseQueryNode();
  
            List<FieldQueryNode> multiTerms = new ArrayList<>();
            int position = -1;
            int i = 0;
            int termGroupCount = 0;
            for (; i < numTokens; i++) {
              String term = null;
              int positionIncrement = 1;
              try {
                boolean hasNext = buffer.incrementToken();
                assert hasNext == true;
                term = termAtt.toString();
                if (posIncrAtt != null) {
                  positionIncrement = posIncrAtt.getPositionIncrement();
                }
  
              } catch (IOException e) {
                // safe to ignore, because we know the number of tokens
              }
  
              if (positionIncrement > 0 && multiTerms.size() > 0) {
  
                for (FieldQueryNode termNode : multiTerms) {
  
                  if (this.positionIncrementsEnabled) {
                    termNode.setPositionIncrement(position);
                  } else {
                    termNode.setPositionIncrement(termGroupCount);
                  }
  
                  mpq.add(termNode);
  
                }
  
                // Only increment once for each "group" of
                // terms that were in the same position:
                termGroupCount++;
  
                multiTerms.clear();
  
              }
  
              position += positionIncrement;
              multiTerms.add(new FieldQueryNode(field, term, -1, -1));
  
            }
  
            for (FieldQueryNode termNode : multiTerms) {
  
              if (this.positionIncrementsEnabled) {
                termNode.setPositionIncrement(position);
  
              } else {
                termNode.setPositionIncrement(termGroupCount);
              }
  
              mpq.add(termNode);
  
            }
  
            return mpq;
  
          }
  
        } else {
  
          TokenizedPhraseQueryNode pq = new TokenizedPhraseQueryNode();
  
          int position = -1;
  
          for (int i = 0; i < numTokens; i++) {
            String term = null;
            int positionIncrement = 1;
  
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.toString();
  
              if (posIncrAtt != null) {
                positionIncrement = posIncrAtt.getPositionIncrement();
              }
  
            } catch (IOException e) {
              // safe to ignore, because we know the number of tokens
            }
  
            FieldQueryNode newFieldNode = new FieldQueryNode(field, term, -1, -1);
  
            if (this.positionIncrementsEnabled) {
              position += positionIncrement;
              newFieldNode.setPositionIncrement(position);
  
            } else {
              newFieldNode.setPositionIncrement(i);
            }
  
            pq.add(newFieldNode);
  
          }
  
          return pq;
  
        }
      } finally {
        if (buffer != null) {
          try {
            buffer.close();
          } catch (IOException e) {
            // safe to ignore
          }
        }
      }
    }

    return node;
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
