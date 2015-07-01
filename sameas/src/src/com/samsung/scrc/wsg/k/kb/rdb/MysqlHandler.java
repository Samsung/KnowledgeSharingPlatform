package com.samsung.scrc.wsg.k.kb.rdb;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.samsung.scrc.wsg.k.kb.KBParam;
import com.samsung.scrc.wsg.k.kb.exception.InitFailureException;

import snaq.db.ConnectionPool;

/**
 * @author yuxie
 * 
 */
public class MysqlHandler extends RDBHandler {
	// MYSQL JDBC prefix
	private static final String PREFIX_URL = "jdbc:mysql://";

	/**
	 */
	public MysqlHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sec.swc.dmc.scrc.sageii.ir.kb.rdb.RDBHandler#initConnectionPool()
	 */
	@Override
	protected void initConnectionPool() throws InitFailureException {
		// TODO Auto-generated method stub
		try {
			Driver driver = (Driver) Class.forName(KBParam.DRIVER_MYSQL)
					.newInstance();
			DriverManager.registerDriver(driver);
			pool = new ConnectionPool(super.POOL_ID, super.MIN_POOL,
					super.MAX_POOL, super.MAX_SIZE, super.IDLE_TIMEOUT,
					PREFIX_URL + super.host + ":" + super.port + "/"
							+ super.database
							+ "?useUnicode=true&characterEncoding=UTF-8",
					super.user, super.password);
		} catch (InstantiationException ie) {
			// TODO Auto-generated catch block
//			log.error(this, ie);
			System.err.println(ie);
			throw new InitFailureException();
		} catch (IllegalAccessException iae) {
			// TODO Auto-generated catch block
//			log.error(this, iae);
			System.err.println(iae);
			throw new InitFailureException();
		} catch (ClassNotFoundException cnfe) {
			// TODO Auto-generated catch block
//			log.error(this, cnfe);
			System.err.println(cnfe);
			throw new InitFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new InitFailureException();
		}
	}
}
