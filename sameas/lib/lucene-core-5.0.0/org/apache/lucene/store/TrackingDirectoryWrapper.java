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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** A delegating Directory that records which files were
 *  written to and deleted. */
public final class TrackingDirectoryWrapper extends FilterDirectory {

  private final Set<String> createdFileNames = Collections.synchronizedSet(new HashSet<String>());

  public TrackingDirectoryWrapper(Directory in) {
    super(in);
  }

  @Override
  public void deleteFile(String name) throws IOException {
    in.deleteFile(name);
    createdFileNames.remove(name);
  }

  @Override
  public IndexOutput createOutput(String name, IOContext context) throws IOException {
    IndexOutput output = in.createOutput(name, context);
    createdFileNames.add(name);
    return output;
  }

  @Override
  public void copyFrom(Directory from, String src, String dest, IOContext context) throws IOException {
    in.copyFrom(from, src, dest, context);
    createdFileNames.add(dest);
  }

  @Override
  public void renameFile(String source, String dest) throws IOException {
    in.renameFile(source, dest);
    synchronized (createdFileNames) {
      createdFileNames.add(dest);
      createdFileNames.remove(source);
    }
  }

  // maybe clone before returning.... all callers are
  // cloning anyway....
  public Set<String> getCreatedFiles() {
    return createdFileNames;
  }

}
