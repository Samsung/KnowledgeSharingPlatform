package org.apache.lucene.codecs.lucene50;

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

import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.util.IOUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class for accessing a compound stream.
 * This class implements a directory, but is limited to only read operations.
 * Directory methods that would normally modify data throw an exception.
 * @lucene.experimental
 */
final class Lucene50CompoundReader extends Directory {
  
  /** Offset/Length for a slice inside of a compound file */
  public static final class FileEntry {
    long offset;
    long length;
  }
  
  private final Directory directory;
  private final String segmentName;
  private final Map<String,FileEntry> entries;
  private final IndexInput handle;
  private int version;
  
  /**
   * Create a new CompoundFileDirectory.
   */
  // TODO: we should just pre-strip "entries" and append segment name up-front like simpletext?
  // this need not be a "general purpose" directory anymore (it only writes index files)
  public Lucene50CompoundReader(Directory directory, SegmentInfo si, IOContext context) throws IOException {
    this.directory = directory;
    this.segmentName = si.name;
    String dataFileName = IndexFileNames.segmentFileName(segmentName, "", Lucene50CompoundFormat.DATA_EXTENSION);
    String entriesFileName = IndexFileNames.segmentFileName(segmentName, "", Lucene50CompoundFormat.ENTRIES_EXTENSION);
    this.entries = readEntries(si.getId(), directory, entriesFileName);
    boolean success = false;
    handle = directory.openInput(dataFileName, context);
    try {
      CodecUtil.checkIndexHeader(handle, Lucene50CompoundFormat.DATA_CODEC, version, version, si.getId(), "");
      
      // NOTE: data file is too costly to verify checksum against all the bytes on open,
      // but for now we at least verify proper structure of the checksum footer: which looks
      // for FOOTER_MAGIC + algorithmID. This is cheap and can detect some forms of corruption
      // such as file truncation.
      CodecUtil.retrieveChecksum(handle);
      success = true;
    } finally {
      if (!success) {
        IOUtils.closeWhileHandlingException(handle);
      }
    }
  }

  /** Helper method that reads CFS entries from an input stream */
  private final Map<String, FileEntry> readEntries(byte[] segmentID, Directory dir, String entriesFileName) throws IOException {
    Map<String,FileEntry> mapping = null;
    try (ChecksumIndexInput entriesStream = dir.openChecksumInput(entriesFileName, IOContext.READONCE)) {
      Throwable priorE = null;
      try {
        version = CodecUtil.checkIndexHeader(entriesStream, Lucene50CompoundFormat.ENTRY_CODEC, 
                                                              Lucene50CompoundFormat.VERSION_START, 
                                                              Lucene50CompoundFormat.VERSION_CURRENT, segmentID, "");
        final int numEntries = entriesStream.readVInt();
        mapping = new HashMap<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
          final FileEntry fileEntry = new FileEntry();
          final String id = entriesStream.readString();
          FileEntry previous = mapping.put(id, fileEntry);
          if (previous != null) {
            throw new CorruptIndexException("Duplicate cfs entry id=" + id + " in CFS ", entriesStream);
          }
          fileEntry.offset = entriesStream.readLong();
          fileEntry.length = entriesStream.readLong();
        }
      } catch (Throwable exception) {
        priorE = exception;
      } finally {
        CodecUtil.checkFooter(entriesStream, priorE);
      }
    }
    return Collections.unmodifiableMap(mapping);
  }
  
  @Override
  public void close() throws IOException {
    IOUtils.close(handle);
  }
  
  @Override
  public IndexInput openInput(String name, IOContext context) throws IOException {
    ensureOpen();
    final String id = IndexFileNames.stripSegmentName(name);
    final FileEntry entry = entries.get(id);
    if (entry == null) {
      throw new FileNotFoundException("No sub-file with id " + id + " found (fileName=" + name + " files: " + entries.keySet() + ")");
    }
    return handle.slice(name, entry.offset, entry.length);
  }
  
  /** Returns an array of strings, one for each file in the directory. */
  @Override
  public String[] listAll() {
    ensureOpen();
    String[] res = entries.keySet().toArray(new String[entries.size()]);
    
    // Add the segment name
    for (int i = 0; i < res.length; i++) {
      res[i] = segmentName + res[i];
    }
    return res;
  }
  
  /** Not implemented
   * @throws UnsupportedOperationException always: not supported by CFS */
  @Override
  public void deleteFile(String name) {
    throw new UnsupportedOperationException();
  }
  
  /** Not implemented
   * @throws UnsupportedOperationException always: not supported by CFS */
  public void renameFile(String from, String to) {
    throw new UnsupportedOperationException();
  }
  
  /** Returns the length of a file in the directory.
   * @throws IOException if the file does not exist */
  @Override
  public long fileLength(String name) throws IOException {
    ensureOpen();
    FileEntry e = entries.get(IndexFileNames.stripSegmentName(name));
    if (e == null)
      throw new FileNotFoundException(name);
    return e.length;
  }
  
  @Override
  public IndexOutput createOutput(String name, IOContext context) throws IOException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void sync(Collection<String> names) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Lock makeLock(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "CompoundFileDirectory(segment=\"" + segmentName + "\" in dir=" + directory + ")";
  }
}
