package org.apache.lucene.store;

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

/**
 * IOContext holds additional details on the merge/search context. A IOContext
 * object can never be initialized as null as passed as a parameter to either
 * {@link org.apache.lucene.store.Directory#openInput(String, IOContext)} or
 * {@link org.apache.lucene.store.Directory#createOutput(String, IOContext)}
 */
public class IOContext {

  /**
   * Context is a enumerator which specifies the context in which the Directory
   * is being used for.
   */
  public enum Context {
    MERGE, READ, FLUSH, DEFAULT
  };

  /**
   * An object of a enumerator Context type
   */
  public final Context context;

  public final MergeInfo mergeInfo;

  public final FlushInfo flushInfo;

  public final boolean readOnce;

  public static final IOContext DEFAULT = new IOContext(Context.DEFAULT);

  public static final IOContext READONCE = new IOContext(true);

  public static final IOContext READ = new IOContext(false);

  public IOContext() {
    this(false);
  }

  public IOContext(FlushInfo flushInfo) {
    assert flushInfo != null;
    this.context = Context.FLUSH;
    this.mergeInfo = null;
    this.readOnce = false;
    this.flushInfo = flushInfo;
  }

  public IOContext(Context context) {
    this(context, null);
  }

  private IOContext(boolean readOnce) {
    this.context = Context.READ;
    this.mergeInfo = null;
    this.readOnce = readOnce;
    this.flushInfo = null;
  }

  public IOContext(MergeInfo mergeInfo) {
    this(Context.MERGE, mergeInfo);
  }
  
  private IOContext(Context context, MergeInfo mergeInfo) {
    assert context != Context.MERGE || mergeInfo != null : "MergeInfo must not be null if context is MERGE";
    assert context != Context.FLUSH : "Use IOContext(FlushInfo) to create a FLUSH IOContext";
    this.context = context;
    this.readOnce = false;
    this.mergeInfo = mergeInfo;
    this.flushInfo = null;
  }
  
  /**
   * This constructor is used to initialize a {@link IOContext} instance with a new value for the readOnce variable. 
   * @param ctxt {@link IOContext} object whose information is used to create the new instance except the readOnce variable.
   * @param readOnce The new {@link IOContext} object will use this value for readOnce. 
   */
  public IOContext(IOContext ctxt, boolean readOnce) {
    this.context = ctxt.context;
    this.mergeInfo = ctxt.mergeInfo;
    this.flushInfo = ctxt.flushInfo;
    this.readOnce = readOnce;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    result = prime * result + ((flushInfo == null) ? 0 : flushInfo.hashCode());
    result = prime * result + ((mergeInfo == null) ? 0 : mergeInfo.hashCode());
    result = prime * result + (readOnce ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IOContext other = (IOContext) obj;
    if (context != other.context)
      return false;
    if (flushInfo == null) {
      if (other.flushInfo != null)
        return false;
    } else if (!flushInfo.equals(other.flushInfo))
      return false;
    if (mergeInfo == null) {
      if (other.mergeInfo != null)
        return false;
    } else if (!mergeInfo.equals(other.mergeInfo))
      return false;
    if (readOnce != other.readOnce)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IOContext [context=" + context + ", mergeInfo=" + mergeInfo
        + ", flushInfo=" + flushInfo + ", readOnce=" + readOnce + "]";
  }

}