package org.apache.lucene.index;

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

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.CommandLineUtil;
import org.apache.lucene.util.InfoStream;
import org.apache.lucene.util.PrintStreamInfoStream;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Collection;

/**
  * This is an easy-to-use tool that upgrades all segments of an index from previous Lucene versions
  * to the current segment file format. It can be used from command line:
  * <pre>
  *  java -cp lucene-core.jar org.apache.lucene.index.IndexUpgrader [-delete-prior-commits] [-verbose] indexDir
  * </pre>
  * Alternatively this class can be instantiated and {@link #upgrade} invoked. It uses {@link UpgradeIndexMergePolicy}
  * and triggers the upgrade via an forceMerge request to {@link IndexWriter}.
  * <p>This tool keeps only the last commit in an index; for this
  * reason, if the incoming index has more than one commit, the tool
  * refuses to run by default. Specify {@code -delete-prior-commits}
  * to override this, allowing the tool to delete all but the last commit.
  * From Java code this can be enabled by passing {@code true} to
  * {@link #IndexUpgrader(Directory,InfoStream,boolean)}.
  * <p><b>Warning:</b> This tool may reorder documents if the index was partially
  * upgraded before execution (e.g., documents were added). If your application relies
  * on &quot;monotonicity&quot; of doc IDs (which means that the order in which the documents
  * were added to the index is preserved), do a full forceMerge instead.
  * The {@link MergePolicy} set by {@link IndexWriterConfig} may also reorder
  * documents.
  */
public final class IndexUpgrader {

  private static void printUsage() {
    System.err.println("Upgrades an index so all segments created with a previous Lucene version are rewritten.");
    System.err.println("Usage:");
    System.err.println("  java " + IndexUpgrader.class.getName() + " [-delete-prior-commits] [-verbose] [-dir-impl X] indexDir");
    System.err.println("This tool keeps only the last commit in an index; for this");
    System.err.println("reason, if the incoming index has more than one commit, the tool");
    System.err.println("refuses to run by default. Specify -delete-prior-commits to override");
    System.err.println("this, allowing the tool to delete all but the last commit.");
    System.err.println("Specify a " + FSDirectory.class.getSimpleName() + 
        " implementation through the -dir-impl option to force its use. If no package is specified the " 
        + FSDirectory.class.getPackage().getName() + " package will be used.");
    System.err.println("WARNING: This tool may reorder document IDs!");
    System.exit(1);
  }

  /** Main method to run {code IndexUpgrader} from the
   *  command-line. */
  @SuppressWarnings("deprecation")
  public static void main(String[] args) throws IOException {
    parseArgs(args).upgrade();
  }
  static IndexUpgrader parseArgs(String[] args) throws IOException {
    String path = null;
    boolean deletePriorCommits = false;
    InfoStream out = null;
    String dirImpl = null;
    int i = 0;
    while (i<args.length) {
      String arg = args[i];
      if ("-delete-prior-commits".equals(arg)) {
        deletePriorCommits = true;
      } else if ("-verbose".equals(arg)) {
        out = new PrintStreamInfoStream(System.out);
      } else if ("-dir-impl".equals(arg)) {
        if (i == args.length - 1) {
          System.out.println("ERROR: missing value for -dir-impl option");
          System.exit(1);
        }
        i++;
        dirImpl = args[i];
      } else if (path == null) {
        path = arg;
      }else {
        printUsage();
      }
      i++;
    }
    if (path == null) {
      printUsage();
    }
    
    Path p = Paths.get(path);
    Directory dir = null;
    if (dirImpl == null) {
      dir = FSDirectory.open(p);
    } else {
      dir = CommandLineUtil.newFSDirectory(dirImpl, p);
    }
    return new IndexUpgrader(dir, out, deletePriorCommits);
  }
  
  private final Directory dir;
  private final IndexWriterConfig iwc;
  private final boolean deletePriorCommits;
  
  /** Creates index upgrader on the given directory, using an {@link IndexWriter} using the given
   * {@code matchVersion}. The tool refuses to upgrade indexes with multiple commit points. */
  public IndexUpgrader(Directory dir) {
    this(dir, new IndexWriterConfig(null), false);
  }
  
  /** Creates index upgrader on the given directory, using an {@link IndexWriter} using the given
   * {@code matchVersion}. You have the possibility to upgrade indexes with multiple commit points by removing
   * all older ones. If {@code infoStream} is not {@code null}, all logging output will be sent to this stream. */
  public IndexUpgrader(Directory dir, InfoStream infoStream, boolean deletePriorCommits) {
    this(dir, new IndexWriterConfig(null), deletePriorCommits);
    if (null != infoStream) {
      this.iwc.setInfoStream(infoStream);
    }
  }
  
  /** Creates index upgrader on the given directory, using an {@link IndexWriter} using the given
   * config. You have the possibility to upgrade indexes with multiple commit points by removing
   * all older ones. */
  public IndexUpgrader(Directory dir, IndexWriterConfig iwc, boolean deletePriorCommits) {
    this.dir = dir;
    this.iwc = iwc;
    this.deletePriorCommits = deletePriorCommits;
  }

  /** Perform the upgrade. */
  public void upgrade() throws IOException {
    if (!DirectoryReader.indexExists(dir)) {
      throw new IndexNotFoundException(dir.toString());
    }
  
    if (!deletePriorCommits) {
      final Collection<IndexCommit> commits = DirectoryReader.listCommits(dir);
      if (commits.size() > 1) {
        throw new IllegalArgumentException("This tool was invoked to not delete prior commit points, but the following commits were found: " + commits);
      }
    }
    
    iwc.setMergePolicy(new UpgradeIndexMergePolicy(iwc.getMergePolicy()));
    iwc.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
    
    final IndexWriter w = new IndexWriter(dir, iwc);
    try {
      InfoStream infoStream = iwc.getInfoStream();
      if (infoStream.isEnabled("IndexUpgrader")) {
        infoStream.message("IndexUpgrader", "Upgrading all pre-" + Version.LATEST + " segments of index directory '" + dir + "' to version " + Version.LATEST + "...");
      }
      w.forceMerge(1);
      if (infoStream.isEnabled("IndexUpgrader")) {
        infoStream.message("IndexUpgrader", "All segments upgraded to version " + Version.LATEST);
      }
    } finally {
      w.close();
    }
  }
  
}
