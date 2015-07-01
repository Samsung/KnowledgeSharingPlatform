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
package org.apache.lucene.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.StringHelper;

/**
 * Command-line tool that enables listing segments in an
 * index, copying specific segments to another index, and
 * deleting segments from an index.
 *
 * <p>This tool does file-level copying of segments files.
 * This means it's unable to split apart a single segment
 * into multiple segments.  For example if your index is a
 * single segment, this tool won't help.  Also, it does basic
 * file-level copying (using simple
 * File{In,Out}putStream) so it will not work with non
 * FSDirectory Directory impls.</p>
 *
 * @lucene.experimental You can easily
 * accidentally remove segments from your index so be
 * careful!
 */
public class IndexSplitter {
  public SegmentInfos infos;

  FSDirectory fsDir;

  Path dir;

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err
          .println("Usage: IndexSplitter <srcDir> -l (list the segments and their sizes)");
      System.err.println("IndexSplitter <srcDir> <destDir> <segments>+");
      System.err
          .println("IndexSplitter <srcDir> -d (delete the following segments)");
      return;
    }
    Path srcDir = Paths.get(args[0]);
    IndexSplitter is = new IndexSplitter(srcDir);
    if (!Files.exists(srcDir)) {
      throw new Exception("srcdir:" + srcDir.toAbsolutePath()
          + " doesn't exist");
    }
    if (args[1].equals("-l")) {
      is.listSegments();
    } else if (args[1].equals("-d")) {
      List<String> segs = new ArrayList<>();
      for (int x = 2; x < args.length; x++) {
        segs.add(args[x]);
      }
      is.remove(segs.toArray(new String[0]));
    } else {
      Path targetDir = Paths.get(args[1]);
      List<String> segs = new ArrayList<>();
      for (int x = 2; x < args.length; x++) {
        segs.add(args[x]);
      }
      is.split(targetDir, segs.toArray(new String[0]));
    }
  }
  
  public IndexSplitter(Path dir) throws IOException {
    this.dir = dir;
    fsDir = FSDirectory.open(dir);
    infos = SegmentInfos.readLatestCommit(fsDir);
  }

  public void listSegments() throws IOException {
    DecimalFormat formatter = new DecimalFormat("###,###.###", DecimalFormatSymbols.getInstance(Locale.ROOT));
    for (int x = 0; x < infos.size(); x++) {
      SegmentCommitInfo info = infos.info(x);
      String sizeStr = formatter.format(info.sizeInBytes());
      System.out.println(info.info.name + " " + sizeStr);
    }
  }

  private int getIdx(String name) {
    for (int x = 0; x < infos.size(); x++) {
      if (name.equals(infos.info(x).info.name))
        return x;
    }
    return -1;
  }

  private SegmentCommitInfo getInfo(String name) {
    for (int x = 0; x < infos.size(); x++) {
      if (name.equals(infos.info(x).info.name))
        return infos.info(x);
    }
    return null;
  }

  public void remove(String[] segs) throws IOException {
    for (String n : segs) {
      int idx = getIdx(n);
      infos.remove(idx);
    }
    infos.changed();
    infos.commit(fsDir);
  }

  public void split(Path destDir, String[] segs) throws IOException {
    Files.createDirectories(destDir);
    FSDirectory destFSDir = FSDirectory.open(destDir);
    SegmentInfos destInfos = new SegmentInfos();
    destInfos.counter = infos.counter;
    for (String n : segs) {
      SegmentCommitInfo infoPerCommit = getInfo(n);
      SegmentInfo info = infoPerCommit.info;
      // Same info just changing the dir:
      SegmentInfo newInfo = new SegmentInfo(destFSDir, info.getVersion(), info.name, info.getDocCount(), 
                                            info.getUseCompoundFile(), info.getCodec(), info.getDiagnostics(), info.getId(), new HashMap<String,String>());
      destInfos.add(new SegmentCommitInfo(newInfo, infoPerCommit.getDelCount(),
          infoPerCommit.getDelGen(), infoPerCommit.getFieldInfosGen(),
          infoPerCommit.getDocValuesGen()));
      // now copy files over
      Collection<String> files = infoPerCommit.files();
      for (final String srcName : files) {
        Path srcFile = dir.resolve(srcName);
        Path destFile = destDir.resolve(srcName);
        Files.copy(srcFile, destFile);
      }
    }
    destInfos.changed();
    destInfos.commit(destFSDir);
    // System.out.println("destDir:"+destDir.getAbsolutePath());
  }
}
