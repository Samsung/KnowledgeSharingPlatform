/**
 * KBFactory.java
 */
package com.samsung.scrc.wsg.k.kb;

//import com.samsung.scrc.wsg.k.kb.rdb.MariaHandler;
import com.samsung.scrc.wsg.k.kb.rdb.MysqlHandler;
//import com.samsung.scrc.wsg.k.kb.rdb.PostgreSQLHandler;

/**
 * @author yuxie
 * 
 * @date Nov 28, 2014
 * 
 */
public class KBFactory {
	public static final String MYSQL = "mysql";
	public static final String PG = "postgres";
	public static final String MARIA = "mariadb";

	public static KBHandler getHandler(String type) {
		switch (type.toLowerCase()) {
		case MYSQL:
			return new MysqlHandler();
//		case PG:
//			return new PostgreSQLHandler();
//		case MARIA:
//			return new MariaHandler();
		default:
			return null;
		}
	}
}
