/**
 * ResOutput.java
 */
package com.samsung.scrc.wsg.k.sa.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.core.SASearcher;

/**
 * @author yuxie
 * 
 * @date Mar 23, 2015
 * 
 */
public class ResOutput implements Runnable {
//	private static Logger log = LogManager.getLogger(ResOutput.class.getName());
	private SASearcher searcher;
	private File outputFile;

	public ResOutput(String saIndexFile, String file) {
		searcher = new SASearcher(saIndexFile);
		outputFile = new File(file);
		if (outputFile.exists()) {
			outputFile.delete();
		} else {
			try {
				outputFile.getParentFile().mkdirs();
				outputFile.createNewFile();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();

			}
		}
	}

	@Override
	public void run() {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			fos = new FileOutputStream(outputFile);
			osw = new OutputStreamWriter(fos, "UTF-8");
			String[][] pairs = searcher.fetchPairs(true);
			while (pairs != null && pairs.length != 0) {
				for (int i = 0; i < pairs.length; i++) {
					String line = pairs[i][0] + "\t" + pairs[i][1] + "\n";
					try {
						osw.write(line);
					} catch (IOException ioe) {
						// TODO Auto-generated catch block
//						log.error(this, ioe);
//						log.error(line);
						System.err.println(ioe);
						System.err.println(line);
					}
				}
				pairs = searcher.fetchPairs(false);
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
//			log.error(this, e);
			System.err.println(e);
		} finally {
			if (osw != null) {
				try {
					osw.flush();
					osw.close();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
//					log.warn(this, ioe);
					System.err.println(ioe);
				}
			}
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
//					log.warn(this, ioe);
					System.err.println(ioe);
				}
			}
		}
	}

	public void close() {
		searcher.close();
	}
}
