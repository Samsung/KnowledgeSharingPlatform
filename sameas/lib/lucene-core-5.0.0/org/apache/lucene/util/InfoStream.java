package org.apache.lucene.util;

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

import org.apache.lucene.index.IndexWriter; // javadocs
import org.apache.lucene.index.SegmentInfos; // javadocs
import java.io.Closeable;

/** 
 * Debugging API for Lucene classes such as {@link IndexWriter} 
 * and {@link SegmentInfos}.
 * <p>
 * NOTE: Enabling infostreams may cause performance degradation
 * in some components.
 * 
 * @lucene.internal 
 */
public abstract class InfoStream implements Closeable {

  /** Instance of InfoStream that does no logging at all. */
  public static final InfoStream NO_OUTPUT = new NoOutput();
  private static final class NoOutput extends InfoStream {
    @Override
    public void message(String component, String message) {
      assert false: "message() should not be called when isEnabled returns false";
    }
    
    @Override
    public boolean isEnabled(String component) {
      return false;
    }

    @Override
    public void close() {}
  }
  
  /** prints a message */
  public abstract void message(String component, String message);
  
  /** returns true if messages are enabled and should be posted to {@link #message}. */
  public abstract boolean isEnabled(String component);
  
  private static InfoStream defaultInfoStream = NO_OUTPUT;
  
  /** The default {@code InfoStream} used by a newly instantiated classes.
   * @see #setDefault */
  public static synchronized InfoStream getDefault() {
    return defaultInfoStream;
  }
  
  /** Sets the default {@code InfoStream} used
   * by a newly instantiated classes. It cannot be {@code null},
   * to disable logging use {@link #NO_OUTPUT}.
   * @see #getDefault */
  public static synchronized void setDefault(InfoStream infoStream) {
    if (infoStream == null) {
      throw new IllegalArgumentException("Cannot set InfoStream default implementation to null. "+
        "To disable logging use InfoStream.NO_OUTPUT");
    }
    defaultInfoStream = infoStream;
  }
}
