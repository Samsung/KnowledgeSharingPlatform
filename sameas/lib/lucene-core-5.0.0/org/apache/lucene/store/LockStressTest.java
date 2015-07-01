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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Simple standalone tool that forever acquires and releases a
 * lock using a specific LockFactory.  Run without any args
 * to see usage.
 *
 * @see VerifyingLockFactory
 * @see LockVerifyServer
 */ 

public class LockStressTest {

  public static void main(String[] args) throws Exception {

    if (args.length != 7) {
      System.out.println("Usage: java org.apache.lucene.store.LockStressTest myID verifierHost verifierPort lockFactoryClassName lockDirName sleepTimeMS count\n" +
                         "\n" +
                         "  myID = int from 0 .. 255 (should be unique for test process)\n" +
                         "  verifierHost = hostname that LockVerifyServer is listening on\n" +
                         "  verifierPort = port that LockVerifyServer is listening on\n" +
                         "  lockFactoryClassName = primary FSLockFactory class that we will use\n" +
                         "  lockDirName = path to the lock directory\n" +
                         "  sleepTimeMS = milliseconds to pause betweeen each lock obtain/release\n" +
                         "  count = number of locking tries\n" +
                         "\n" +
                         "You should run multiple instances of this process, each with its own\n" +
                         "unique ID, and each pointing to the same lock directory, to verify\n" +
                         "that locking is working correctly.\n" +
                         "\n" +
                         "Make sure you are first running LockVerifyServer.");
      System.exit(1);
    }

    int arg = 0;
    final int myID = Integer.parseInt(args[arg++]);

    if (myID < 0 || myID > 255) {
      System.out.println("myID must be a unique int 0..255");
      System.exit(1);
    }

    final String verifierHost = args[arg++];
    final int verifierPort = Integer.parseInt(args[arg++]);
    final String lockFactoryClassName = args[arg++];
    final Path lockDirPath = Paths.get(args[arg++]);
    final int sleepTimeMS = Integer.parseInt(args[arg++]);
    final int count = Integer.parseInt(args[arg++]);

    final LockFactory lockFactory = getNewLockFactory(lockFactoryClassName);
    // we test the lock factory directly, so we don't need it on the directory itsself (the directory is just for testing)
    final FSDirectory lockDir = new SimpleFSDirectory(lockDirPath, NoLockFactory.INSTANCE);
    final InetSocketAddress addr = new InetSocketAddress(verifierHost, verifierPort);
    System.out.println("Connecting to server " + addr +
        " and registering as client " + myID + "...");
    try (Socket socket = new Socket()) {
      socket.setReuseAddress(true);
      socket.connect(addr, 500);
      final OutputStream out = socket.getOutputStream();
      final InputStream in = socket.getInputStream();
      
      out.write(myID);
      out.flush();
      LockFactory verifyLF = new VerifyingLockFactory(lockFactory, in, out);
      Lock l = verifyLF.makeLock(lockDir, "test.lock");
      final Random rnd = new Random();
      
      // wait for starting gun
      if (in.read() != 43) {
        throw new IOException("Protocol violation");
      }
      
      for (int i = 0; i < count; i++) {
        boolean obtained = false;
        try {
          obtained = l.obtain(rnd.nextInt(100) + 10);
        } catch (LockObtainFailedException e) {}
        
        if (obtained) {
          if (rnd.nextInt(10) == 0) {
            if (rnd.nextBoolean()) {
              verifyLF = new VerifyingLockFactory(getNewLockFactory(lockFactoryClassName), in, out);
            }
            final Lock secondLock = verifyLF.makeLock(lockDir, "test.lock");
            if (secondLock.obtain()) {
              throw new IOException("Double Obtain");
            }
          }
          Thread.sleep(sleepTimeMS);
          l.close();
        }
        
        if (i % 500 == 0) {
          System.out.println((i * 100. / count) + "% done.");
        }
        
        Thread.sleep(sleepTimeMS);
      } 
    }
    
    System.out.println("Finished " + count + " tries.");
  }


  private static FSLockFactory getNewLockFactory(String lockFactoryClassName) throws IOException {
    // try to get static INSTANCE field of class
    try {
      return (FSLockFactory) Class.forName(lockFactoryClassName).getField("INSTANCE").get(null);
    } catch (ReflectiveOperationException e) {
      // fall-through
    }
    
    // try to create a new instance
    try {
      return Class.forName(lockFactoryClassName).asSubclass(FSLockFactory.class).newInstance();
    } catch (IllegalAccessException | InstantiationException | ClassCastException | ClassNotFoundException e) {
      // fall-through
    }

    throw new IOException("Cannot get lock factory singleton of " + lockFactoryClassName);
  }
}
