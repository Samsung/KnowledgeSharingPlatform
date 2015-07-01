package org.apache.lucene.util;

import java.util.concurrent.atomic.AtomicBoolean;

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
 * A convenient class which offers a semi-immutable object wrapper
 * implementation which allows one to set the value of an object exactly once,
 * and retrieve it many times. If {@link #set(Object)} is called more than once,
 * {@link AlreadySetException} is thrown and the operation
 * will fail.
 *
 * @lucene.experimental
 */
public final class SetOnce<T> implements Cloneable {

  /** Thrown when {@link SetOnce#set(Object)} is called more than once. */
  public static final class AlreadySetException extends IllegalStateException {
    public AlreadySetException() {
      super("The object cannot be set twice!");
    }
  }
  
  private volatile T obj = null;
  private final AtomicBoolean set;
  
  /**
   * A default constructor which does not set the internal object, and allows
   * setting it by calling {@link #set(Object)}.
   */
  public SetOnce() {
    set = new AtomicBoolean(false);
  }

  /**
   * Creates a new instance with the internal object set to the given object.
   * Note that any calls to {@link #set(Object)} afterwards will result in
   * {@link AlreadySetException}
   *
   * @throws AlreadySetException if called more than once
   * @see #set(Object)
   */
  public SetOnce(T obj) {
    this.obj = obj;
    set = new AtomicBoolean(true);
  }
  
  /** Sets the given object. If the object has already been set, an exception is thrown. */
  public final void set(T obj) {
    if (set.compareAndSet(false, true)) {
      this.obj = obj;
    } else {
      throw new AlreadySetException();
    }
  }
  
  /** Returns the object set by {@link #set(Object)}. */
  public final T get() {
    return obj;
  }
}
