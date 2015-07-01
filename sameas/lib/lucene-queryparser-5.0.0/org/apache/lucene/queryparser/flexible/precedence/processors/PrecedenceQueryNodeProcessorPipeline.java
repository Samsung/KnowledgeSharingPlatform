package org.apache.lucene.queryparser.flexible.precedence.processors;

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

import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.queryparser.flexible.standard.processors.BooleanQuery2ModifierNodeProcessor;
import org.apache.lucene.queryparser.flexible.standard.processors.GroupQueryNodeProcessor;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;

/**
 * <p>
 * This processor pipeline extends {@link StandardQueryNodeProcessorPipeline} and enables
 * boolean precedence on it.
 * </p>
 * <p>
 * EXPERT: the precedence is enabled by removing {@link GroupQueryNodeProcessor} from the
 * {@link StandardQueryNodeProcessorPipeline} and appending {@link BooleanModifiersQueryNodeProcessor}
 * to the pipeline.
 * </p>
 * 
 * @see PrecedenceQueryParser
 *  @see StandardQueryNodeProcessorPipeline
 */
public class PrecedenceQueryNodeProcessorPipeline extends StandardQueryNodeProcessorPipeline {

  /**
   * @see StandardQueryNodeProcessorPipeline#StandardQueryNodeProcessorPipeline(QueryConfigHandler)
   */
  public PrecedenceQueryNodeProcessorPipeline(QueryConfigHandler queryConfig) {
    super(queryConfig);
    
    for (int i = 0 ; i < size() ; i++) {
      
      if (get(i).getClass().equals(BooleanQuery2ModifierNodeProcessor.class)) {
        remove(i--);
      }
      
    }
    
    add(new BooleanModifiersQueryNodeProcessor());
    
  }

}
