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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FSLockFactory;
import org.apache.lucene.store.LockFactory;

/**
 * Class containing some useful methods used by command line tools 
 *
 */
public final class CommandLineUtil {
  
  private CommandLineUtil() {
    
  }
  
  /**
   * Creates a specific FSDirectory instance starting from its class name, using the default lock factory
   * @param clazzName The name of the FSDirectory class to load
   * @param path The path to be used as parameter constructor
   * @return the new FSDirectory instance
   */
  public static FSDirectory newFSDirectory(String clazzName, Path path) {
    return newFSDirectory(clazzName, path, FSLockFactory.getDefault());
  }
  
  /**
   * Creates a specific FSDirectory instance starting from its class name
   * @param clazzName The name of the FSDirectory class to load
   * @param path The path to be used as parameter constructor
   * @param lf The lock factory to be used
   * @return the new FSDirectory instance
   */
  public static FSDirectory newFSDirectory(String clazzName, Path path, LockFactory lf) {
    try {
      final Class<? extends FSDirectory> clazz = loadFSDirectoryClass(clazzName);
      return newFSDirectory(clazz, path, lf);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(FSDirectory.class.getSimpleName()
          + " implementation not found: " + clazzName, e);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(clazzName + " is not a " + FSDirectory.class.getSimpleName()
          + " implementation", e);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(clazzName + " constructor with "
          + Path.class.getSimpleName() + " as parameter not found", e);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error creating " + clazzName + " instance", e);
    }
  }
  
  /**
   * Loads a specific Directory implementation 
   * @param clazzName The name of the Directory class to load
   * @return The Directory class loaded
   * @throws ClassNotFoundException If the specified class cannot be found.
   */
  public static Class<? extends Directory> loadDirectoryClass(String clazzName) 
      throws ClassNotFoundException {
    return Class.forName(adjustDirectoryClassName(clazzName)).asSubclass(Directory.class);
  }
  
  /**
   * Loads a specific FSDirectory implementation
   * @param clazzName The name of the FSDirectory class to load
   * @return The FSDirectory class loaded
   * @throws ClassNotFoundException If the specified class cannot be found.
   */
  public static Class<? extends FSDirectory> loadFSDirectoryClass(String clazzName) 
      throws ClassNotFoundException {
    return Class.forName(adjustDirectoryClassName(clazzName)).asSubclass(FSDirectory.class);
  }
  
  private static String adjustDirectoryClassName(String clazzName) {
    if (clazzName == null || clazzName.trim().length() == 0) {
      throw new IllegalArgumentException("The " + FSDirectory.class.getSimpleName()
          + " implementation cannot be null or empty");
    }
    
    if (clazzName.indexOf(".") == -1) {// if not fully qualified, assume .store
      clazzName = Directory.class.getPackage().getName() + "." + clazzName;
    }
    return clazzName;
  }
  
  /**
   * Creates a new specific FSDirectory instance
   * @param clazz The class of the object to be created
   * @param path The file to be used as parameter constructor
   * @return The new FSDirectory instance
   * @throws NoSuchMethodException If the Directory does not have a constructor that takes <code>Path</code>.
   * @throws InstantiationException If the class is abstract or an interface.
   * @throws IllegalAccessException If the constructor does not have public visibility.
   * @throws InvocationTargetException If the constructor throws an exception
   */
  public static FSDirectory newFSDirectory(Class<? extends FSDirectory> clazz, Path path) 
      throws ReflectiveOperationException {
    return newFSDirectory(clazz, path, FSLockFactory.getDefault());
  }
  
  /**
   * Creates a new specific FSDirectory instance
   * @param clazz The class of the object to be created
   * @param path The file to be used as parameter constructor
   * @param lf The lock factory to be used
   * @return The new FSDirectory instance
   * @throws NoSuchMethodException If the Directory does not have a constructor that takes <code>Path</code>.
   * @throws InstantiationException If the class is abstract or an interface.
   * @throws IllegalAccessException If the constructor does not have public visibility.
   * @throws InvocationTargetException If the constructor throws an exception
   */
  public static FSDirectory newFSDirectory(Class<? extends FSDirectory> clazz, Path path, LockFactory lf) 
      throws ReflectiveOperationException {
    // Assuming every FSDirectory has a ctor(Path):
    Constructor<? extends FSDirectory> ctor = clazz.getConstructor(Path.class, LockFactory.class);
    return ctor.newInstance(path, lf);
  }
  
}
