package org.apache.lucene.search.similarities;

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

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.AfterEffect.NoAfterEffect;
import org.apache.lucene.search.similarities.Normalization.NoNormalization;

/**
 * Implements the <em>divergence from randomness (DFR)</em> framework
 * introduced in Gianni Amati and Cornelis Joost Van Rijsbergen. 2002.
 * Probabilistic models of information retrieval based on measuring the
 * divergence from randomness. ACM Trans. Inf. Syst. 20, 4 (October 2002),
 * 357-389.
 * <p>The DFR scoring formula is composed of three separate components: the
 * <em>basic model</em>, the <em>aftereffect</em> and an additional
 * <em>normalization</em> component, represented by the classes
 * {@code BasicModel}, {@code AfterEffect} and {@code Normalization},
 * respectively. The names of these classes were chosen to match the names of
 * their counterparts in the Terrier IR engine.</p>
 * <p>To construct a DFRSimilarity, you must specify the implementations for 
 * all three components of DFR:
 * <ol>
 *    <li>{@link BasicModel}: Basic model of information content:
 *        <ul>
 *           <li>{@link BasicModelBE}: Limiting form of Bose-Einstein
 *           <li>{@link BasicModelG}: Geometric approximation of Bose-Einstein
 *           <li>{@link BasicModelP}: Poisson approximation of the Binomial
 *           <li>{@link BasicModelD}: Divergence approximation of the Binomial 
 *           <li>{@link BasicModelIn}: Inverse document frequency
 *           <li>{@link BasicModelIne}: Inverse expected document
 *               frequency [mixture of Poisson and IDF]
 *           <li>{@link BasicModelIF}: Inverse term frequency
 *               [approximation of I(ne)]
 *        </ul>
 *    <li>{@link AfterEffect}: First normalization of information
 *        gain:
 *        <ul>
 *           <li>{@link AfterEffectL}: Laplace's law of succession
 *           <li>{@link AfterEffectB}: Ratio of two Bernoulli processes
 *           <li>{@link NoAfterEffect}: no first normalization
 *        </ul>
 *    <li>{@link Normalization}: Second (length) normalization:
 *        <ul>
 *           <li>{@link NormalizationH1}: Uniform distribution of term
 *               frequency
 *           <li>{@link NormalizationH2}: term frequency density inversely
 *               related to length
 *           <li>{@link NormalizationH3}: term frequency normalization
 *               provided by Dirichlet prior
 *           <li>{@link NormalizationZ}: term frequency normalization provided
 *                by a Zipfian relation
 *           <li>{@link NoNormalization}: no second normalization
 *        </ul>
 * </ol>
 * <p>Note that <em>qtf</em>, the multiplicity of term-occurrence in the query,
 * is not handled by this implementation.</p>
 * @see BasicModel
 * @see AfterEffect
 * @see Normalization
 * @lucene.experimental
 */
public class DFRSimilarity extends SimilarityBase {
  /** The basic model for information content. */
  protected final BasicModel basicModel;
  /** The first normalization of the information content. */
  protected final AfterEffect afterEffect;
  /** The term frequency normalization. */
  protected final Normalization normalization;
  
  /**
   * Creates DFRSimilarity from the three components.
   * <p>
   * Note that <code>null</code> values are not allowed:
   * if you want no normalization or after-effect, instead pass 
   * {@link NoNormalization} or {@link NoAfterEffect} respectively.
   * @param basicModel Basic model of information content
   * @param afterEffect First normalization of information gain
   * @param normalization Second (length) normalization
   */
  public DFRSimilarity(BasicModel basicModel,
                       AfterEffect afterEffect,
                       Normalization normalization) {
    if (basicModel == null || afterEffect == null || normalization == null) {
      throw new NullPointerException("null parameters not allowed.");
    }
    this.basicModel = basicModel;
    this.afterEffect = afterEffect;
    this.normalization = normalization;
  }

  @Override
  protected float score(BasicStats stats, float freq, float docLen) {
    float tfn = normalization.tfn(stats, freq, docLen);
    return stats.getTotalBoost() *
        basicModel.score(stats, tfn) * afterEffect.score(stats, tfn);
  }
  
  @Override
  protected void explain(Explanation expl,
      BasicStats stats, int doc, float freq, float docLen) {
    if (stats.getTotalBoost() != 1.0f) {
      expl.addDetail(new Explanation(stats.getTotalBoost(), "boost"));
    }
    
    Explanation normExpl = normalization.explain(stats, freq, docLen);
    float tfn = normExpl.getValue();
    expl.addDetail(normExpl);
    expl.addDetail(basicModel.explain(stats, tfn));
    expl.addDetail(afterEffect.explain(stats, tfn));
  }

  @Override
  public String toString() {
    return "DFR " + basicModel.toString() + afterEffect.toString()
                  + normalization.toString();
  }
  
  /**
   * Returns the basic model of information content
   */
  public BasicModel getBasicModel() {
    return basicModel;
  }
  
  /**
   * Returns the first normalization
   */
  public AfterEffect getAfterEffect() {
    return afterEffect;
  }
  
  /**
   * Returns the second normalization
   */
  public Normalization getNormalization() {
    return normalization;
  }
}
