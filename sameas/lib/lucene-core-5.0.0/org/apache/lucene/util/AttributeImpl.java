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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Base class for Attributes that can be added to a 
 * {@link org.apache.lucene.util.AttributeSource}.
 * <p>
 * Attributes are used to add data in a dynamic, yet type-safe way to a source
 * of usually streamed objects, e. g. a {@link org.apache.lucene.analysis.TokenStream}.
 */
public abstract class AttributeImpl implements Cloneable, Attribute {  
  /**
   * Clears the values in this AttributeImpl and resets it to its 
   * default value. If this implementation implements more than one Attribute interface
   * it clears all.
   */
  public abstract void clear();
  
  /**
   * This method returns the current attribute values as a string in the following format
   * by calling the {@link #reflectWith(AttributeReflector)} method:
   * 
   * <ul>
   * <li><em>iff {@code prependAttClass=true}:</em> {@code "AttributeClass#key=value,AttributeClass#key=value"}
   * <li><em>iff {@code prependAttClass=false}:</em> {@code "key=value,key=value"}
   * </ul>
   *
   * @see #reflectWith(AttributeReflector)
   */
  public final String reflectAsString(final boolean prependAttClass) {
    final StringBuilder buffer = new StringBuilder();
    reflectWith(new AttributeReflector() {
      @Override
      public void reflect(Class<? extends Attribute> attClass, String key, Object value) {
        if (buffer.length() > 0) {
          buffer.append(',');
        }
        if (prependAttClass) {
          buffer.append(attClass.getName()).append('#');
        }
        buffer.append(key).append('=').append((value == null) ? "null" : value);
      }
    });
    return buffer.toString();
  }
  
  /**
   * This method is for introspection of attributes, it should simply
   * add the key/values this attribute holds to the given {@link AttributeReflector}.
   *
   * <p>The default implementation calls {@link AttributeReflector#reflect} for all
   * non-static fields from the implementing class, using the field name as key
   * and the field value as value. The Attribute class is also determined by reflection.
   * Please note that the default implementation can only handle single-Attribute
   * implementations.
   *
   * <p>Custom implementations look like this (e.g. for a combined attribute implementation):
   * <pre class="prettyprint">
   *   public void reflectWith(AttributeReflector reflector) {
   *     reflector.reflect(CharTermAttribute.class, "term", term());
   *     reflector.reflect(PositionIncrementAttribute.class, "positionIncrement", getPositionIncrement());
   *   }
   * </pre>
   *
   * <p>If you implement this method, make sure that for each invocation, the same set of {@link Attribute}
   * interfaces and keys are passed to {@link AttributeReflector#reflect} in the same order, but possibly
   * different values. So don't automatically exclude e.g. {@code null} properties!
   *
   * @see #reflectAsString(boolean)
   */
  public void reflectWith(AttributeReflector reflector) {
    final Class<? extends AttributeImpl> clazz = this.getClass();
    final Class<? extends Attribute>[] interfaces = AttributeSource.getAttributeInterfaces(clazz);
    if (interfaces.length != 1) {
      throw new UnsupportedOperationException(clazz.getName() +
        " implements more than one Attribute interface, the default reflectWith() implementation cannot handle this.");
    }
    final Class<? extends Attribute> interf = interfaces[0];
    final Field[] fields = clazz.getDeclaredFields();
    try {
      for (int i = 0; i < fields.length; i++) {
        final Field f = fields[i];
        if (Modifier.isStatic(f.getModifiers())) continue;
        f.setAccessible(true);
        reflector.reflect(interf, f.getName(), f.get(this));
      }
    } catch (IllegalAccessException e) {
      // this should never happen, because we're just accessing fields
      // from 'this'
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Copies the values from this Attribute into the passed-in
   * target attribute. The target implementation must support all the
   * Attributes this implementation supports.
   */
  public abstract void copyTo(AttributeImpl target);

  /**
   * In most cases the clone is, and should be, deep in order to be able to
   * properly capture the state of all attributes.
   */
  @Override
  public AttributeImpl clone() {
    AttributeImpl clone = null;
    try {
      clone = (AttributeImpl)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);  // shouldn't happen
    }
    return clone;
  }
}
