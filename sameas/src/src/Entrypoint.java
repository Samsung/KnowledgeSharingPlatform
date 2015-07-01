import com.samsung.scrc.wsg.k.sa.core.SAEngine;

/**
 * Entrypoint.java
 */

/**
 * @author yuxie
 * 
 * @date Nov 28, 2014
 * 
 */
public class Entrypoint {
	public static void main(String[] args) {
		System.out.println("Start...");
		SAEngine saEngine = new SAEngine();
		saEngine.run();
		System.out.println("End...");
	}
}
