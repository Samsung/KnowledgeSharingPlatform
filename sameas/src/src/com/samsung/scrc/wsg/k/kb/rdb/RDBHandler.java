package com.samsung.scrc.wsg.k.kb.rdb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.kb.KBHandler;
import com.samsung.scrc.wsg.k.kb.exception.HandlingFailureException;
import com.samsung.scrc.wsg.k.kb.exception.InitFailureException;

import snaq.db.ConnectionPool;

/**
 * @author yuxie
 * 
 */
public abstract class RDBHandler extends KBHandler {
	// logger
//	protected static Logger log = LogManager.getLogger(RDBHandler.class
//			.getName());
	// connection to RDB
	protected ConnectionPool pool = null;
	// pre-define RDB pool parameters
	protected final String POOL_ID = "sameasweb";
	protected final int MIN_POOL = 0;
	protected final int MAX_POOL = 80;
	protected final int MAX_SIZE = 100;
	protected final int IDLE_TIMEOUT = 300;
	// rdb connection parameters
	protected String host;
	protected String port;
	protected String user;
	protected String password;
	protected String database;
	// pre-define variables
	protected final String ASTERISK = "*";
	private final String TABLE = "TABLE";
	private final String TABLE_NAME = "TABLE_NAME";
	private final String VIEW = "VIEW";
	private final long TIMEOUT = 3000;
	// batch
	private final int INSERT_BATCH_SIZE = 500000;

	protected RDBHandler() {

	}

	@Override
	public boolean init(String host, String port, String user, String password,
			String database) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.database = database;
		try {
			this.initConnectionPool();
			return true;
		} catch (InitFailureException ife) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	/**
	 * Establish connection to database
	 * 
	 * @throws InitFailureException
	 */
	protected abstract void initConnectionPool() throws InitFailureException;

	public void release() {
		pool.release();
	}

	/**
	 * Close the connection
	 */
	protected void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
//				log.debug("RDB connection is closed");
			} catch (SQLException closee) {
				// TODO Auto-generated catch block
//				log.error(this, closee);
				System.err.println(closee);
			}
		}
	}

	protected Connection fetchConnection() throws HandlingFailureException {
		Connection conn = null;
		try {
			conn = pool.getConnection(TIMEOUT);
			if (conn != null) {
				return conn;
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		}
	}

	public boolean checkConnection() {
		Connection conn = null;
		try {
			conn = this.fetchConnection();
			if (conn != null && !conn.isClosed()) {
				return true;
			} else {
				return false;
			}
		} catch (HandlingFailureException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return false;
		} finally {
			this.closeConnection(conn);
		}
	}

	public Map<String, HashSet<String>> fetchCondBatch(String table,
			String field, String cField, String Op, String[] cValues)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			Map<String, HashSet<String>> res = new HashMap<String, HashSet<String>>();
			// construct select SQL
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ").append(field).append(" FROM ").append(table)
					.append(" WHERE ").append(cField).append(Op).append("?");
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				for (int i = 0; i < cValues.length; i++) {
					ps.setObject(1, cValues[i]);
					rs = ps.executeQuery();
					HashSet<String> values = new HashSet<String>();
					while (rs.next()) {
						// log.debug(field);
						// log.debug(rs.getObject(field));
						values.add(String.valueOf(rs.getObject(field)));
						res.put(cValues[i], values);
					}
					rs.close();
				}
				return res;
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException rse) {
					// TODO Auto-generated catch block
//					log.warn("ResultSet fail to close\n" + rse);
					System.err.println(rse);
				}
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			this.closeConnection(conn);
		}
	}

	/**
	 * Fetch the column number of a given table
	 * 
	 * @param table
	 * @return
	 * @throws HandlingFailureException
	 */
	public int fetchTableColNo(String table) throws HandlingFailureException {
		int colNo = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			String sql = "SELECT * FROM " + table;
			ps = conn.prepareStatement(sql);
			if (ps != null) {
				rs = ps.executeQuery();
				colNo = rs.getMetaData().getColumnCount();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException rsce) {
					// TODO Auto-generated catch block
//					log.warn(this, rsce);
					System.err.println(rsce);
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException psce) {
					// TODO Auto-generated catch block
//					log.warn(this, psce);
					System.err.println(psce);
				}
			}
			this.closeConnection(conn);
		}
		return colNo;
	}

	/**
	 * Fetch Tables
	 * 
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<String> fetchTables() throws HandlingFailureException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			List<String> tables = new ArrayList<String>();
			DatabaseMetaData meta;
			meta = conn.getMetaData();
			rs = meta.getTables(null, null, null, new String[] { TABLE });
			while (rs.next()) {
				tables.add(rs.getString(TABLE_NAME));
			}
			return tables;
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException rsce) {
					// TODO Auto-generated catch block
//					log.warn(this, rsce);
					System.err.println(rsce);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Fetch Tables and Views
	 * 
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<String> fetchTableViews() throws HandlingFailureException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			List<String> tables = new ArrayList<String>();
			DatabaseMetaData meta;
			meta = conn.getMetaData();
			rs = meta.getTables(null, null, null, new String[] { TABLE, VIEW });
			while (rs.next()) {
				tables.add(rs.getString(TABLE_NAME));
			}
			return tables;
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException rsce) {
					// TODO Auto-generated catch block
//					log.warn(this, rsce);
					System.err.println(rsce);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Fetch Views
	 * 
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<String> fetchViews() throws HandlingFailureException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			List<String> tables = new ArrayList<String>();
			DatabaseMetaData meta;
			meta = conn.getMetaData();
			rs = meta.getTables(null, null, null, new String[] { VIEW });
			while (rs.next()) {
				tables.add(rs.getString(TABLE_NAME));
			}
			return tables;
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException rsce) {
					// TODO Auto-generated catch block
//					log.warn(this, rsce);
					System.err.println(rsce);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Fetch Columns by a given table
	 * 
	 * @param table
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<String> fetchCols(String table) throws HandlingFailureException {
		Connection conn = null;
		List<String> columns = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			String query = "SELECT * FROM " + table + " LIMIT 1";
//			log.debug(query);
			ps = conn.prepareStatement(query);
			if (ps != null) {
				rs = ps.executeQuery();
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int numberOfColumns = rsMetaData.getColumnCount();
//				log.debug(numberOfColumns);
				// get the column names; column indexes start from 1
				for (int i = 1; i < numberOfColumns + 1; i++) {
					columns.add(rsMetaData.getColumnName(i));
				}
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.warn("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.warn("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return columns;
	}

	/**
	 * Fetch the primary keys of the given table
	 * 
	 * @param table
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<String> fetchPrimaryKeys(String table)
			throws HandlingFailureException {
		Connection conn = null;
		ResultSet rs = null;
		List<String> pks = new ArrayList<String>();
		try {
			conn = this.fetchConnection();
			rs = conn.getMetaData().getPrimaryKeys(null, null, table);
			while (rs.next()) {
				pks.add(rs.getString("COLUMN_NAME"));
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.warn("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			this.closeConnection(conn);
		}
		return pks;
	}

	/**
	 * Fetch all the entities according to the exact condition in a given table
	 * 
	 * @param table
	 * @param fields
	 * @param cFields
	 * @param cValues
	 * @param limit
	 * @param offset
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<TreeMap<String, String>> fetchExactCond(String table,
			String[] fields, String[] cFields, String[] cValues, String limit,
			String offset) throws HandlingFailureException {
		List<TreeMap<String, String>> res = new ArrayList<TreeMap<String, String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			int i;
			if (fields != null) { // if fields is specified
				i = 0;
				sb.append(fields[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i]);
					i++;
				}
			} else {
				// select *
				sb.append(ASTERISK);
			}
			sb.append(" FROM " + table);
			if (cFields != null && cValues != null) {
				sb.append(" WHERE ");
				i = 0;
				sb.append(cFields[i] + " = " + cValues[i]);
				i++;
				while (i < cFields.length) {
					sb.append(" and " + cFields[i] + " = " + cValues[i]);
					i++;
				}
			}
			// else no condition query
			if (limit != null) {
				sb.append(" LIMIT " + limit);
			}
			if (offset != null) {
				sb.append(" OFFSET " + offset);
			}
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				rs = ps.executeQuery();
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					TreeMap<String, String> map = new TreeMap<String, String>();
					for (i = 0; i < colCount; i++) {
						map.put(rs.getMetaData().getColumnName(i + 1),
								rs.getString(i + 1));
					}
					res.add(map);
				}
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return res;
	}

	/**
	 * Fetch all the entities according to the fuzzy(like) condition in a given
	 * table
	 * 
	 * @param table
	 * @param fields
	 * @param cFields
	 * @param cValues
	 * @param limit
	 * @param offset
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<TreeMap<String, String>> fetchFuzzyCond(String table,
			String[] fields, String[] cFields, String[] cValues, String limit,
			String offset) throws HandlingFailureException {
		List<TreeMap<String, String>> res = new ArrayList<TreeMap<String, String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			int i;
			if (fields != null) { // if fields is specified
				i = 0;
				sb.append(fields[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i]);
					i++;
				}
			} else
				// select *
				sb.append(ASTERISK);
			sb.append(" FROM " + table);
			if (cFields != null && cValues != null) {
				sb.append(" WHERE ");
				i = 0;
				sb.append(cFields[i] + " like " + cValues[i]);
				i++;
				while (i < cFields.length) {
					sb.append(" and " + cFields[i] + " like " + cValues[i]);
					i++;
				}
			}
			// else no condition query
			if (limit != null)
				sb.append(" LIMIT " + limit);
			if (offset != null)
				sb.append(" OFFSET " + offset);
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				rs = ps.executeQuery();
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					TreeMap<String, String> map = new TreeMap<String, String>();
					for (i = 0; i < colCount; i++) {
						map.put(rs.getMetaData().getColumnName(i + 1),
								rs.getString(i + 1));
					}
					res.add(map);
				}
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return res;
	}

	/**
	 * Fetch all the entities according to the condition in a given table
	 * 
	 * @param table
	 * @param fields
	 * @param cFields
	 * @param cValues
	 * @param cOps
	 * @param limit
	 * @param offset
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<Map<String, String>> fetchCond(String table, String[] fields,
			String[] cFields, String[] cValues, String[] cOps, String limit,
			String offset) throws HandlingFailureException {
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			int i;
			if (fields != null) { // if fields is specified
				i = 0;
				sb.append(fields[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i]);
					i++;
				}
			} else
				// select *
				sb.append(ASTERISK);
			sb.append(" FROM " + table);
			if (cFields != null && cValues != null && cOps != null) {
				sb.append(" WHERE ");
				i = 0;
				sb.append(cFields[i]).append(cOps[i]).append("?");
				i++;
				while (i < cFields.length) {
					sb.append(" and ").append(cFields[i]).append(cOps[i])
							.append("?");
					i++;
				}
				// else no condition query
				if (limit != null)
					sb.append(" LIMIT " + limit);
				if (offset != null)
					sb.append(" OFFSET " + offset);
//				log.debug(sb.toString());
				ps = conn.prepareStatement(sb.toString());
				if (ps != null) {
					for (int j = 0; j < cValues.length; j++) {
						ps.setObject(j + 1, cValues[j]);
					}
					rs = ps.executeQuery();
					int colCount = rs.getMetaData().getColumnCount();
					while (rs.next()) {
						Map<String, String> map = new TreeMap<String, String>();
						for (i = 0; i < colCount; i++) {
							map.put(rs.getMetaData().getColumnName(i + 1),
									rs.getString(i + 1));
						}
						res.add(map);
					}
					return res;
				} else {
					throw new HandlingFailureException();
				}
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.warn("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.warn("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	public List<String> fetchCond(String table, String field, String[] cFields,
			String[] cValues, String[] cOps, String limit, String offset)
			throws HandlingFailureException {
		List<String> res = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			if (field != null) { // if fields is specified
				sb.append(field);
			} else {
				throw new HandlingFailureException();
			}
			sb.append(" FROM " + table);
			if (cFields != null && cValues != null && cOps != null) {
				sb.append(" WHERE ");
				int i = 0;
				sb.append(cFields[i]).append(cOps[i]).append("?");
				i++;
				while (i < cFields.length) {
					sb.append(" and ").append(cFields[i]).append(cOps[i])
							.append("?");
					i++;
				}
				// else no condition query
				if (limit != null)
					sb.append(" LIMIT " + limit);
				if (offset != null)
					sb.append(" OFFSET " + offset);
//				log.debug(sb.toString());
				ps = conn.prepareStatement(sb.toString());
				if (ps != null) {
					for (int j = 0; j < cValues.length; j++) {
						ps.setObject(j + 1, cValues[j]);
					}
					rs = ps.executeQuery();
					while (rs.next()) {
						res.add(rs.getString(field));
					}
					return res;
				} else {
					throw new HandlingFailureException();
				}
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.warn("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.warn("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	public Map<String, List<String>> fetchCondIn(String table, String field,
			String cField, String[] cValues) throws HandlingFailureException {
		Map<String, List<String>> res = new TreeMap<String, List<String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ").append(field).append(", ").append(cField);
			sb.append(" FROM " + table);
			sb.append(" WHERE ");
			int i = 0;
			sb.append(cField).append(" in ").append("(").append("?");
			i++;
			while (i < cValues.length) {
				sb.append(" , " + "?");
				i++;
			}
			sb.append(")");
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				for (int j = 0; j < cValues.length; j++) {
					ps.setObject(j + 1, cValues[j]);
				}
				rs = ps.executeQuery();
				while (rs.next()) {
					String key = (String) rs.getObject(cField);
					String value = (String) rs.getObject(field);
					if (res.containsKey(key)) {
						res.get(key).add(value);
					} else {
						List<String> values = new ArrayList<String>();
						values.add(value);
						res.put(key, values);
					}
				}
				return res;
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.warn("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.warn("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Insert record into table by given SQL query
	 * 
	 * @param query
	 * @throws HandlingFailureException
	 * @throws SQLException
	 */
	public void insert(String query) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			ps = conn.prepareStatement(query);
			if (ps != null) {
				ps.executeUpdate();
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException sqle) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + sqle);
					System.err.println(sqle);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Insert record into table
	 * 
	 * @param table
	 * @param records
	 * @throws HandlingFailureException
	 */
	public void insert(String table, Map<String, String> records)
			throws HandlingFailureException {
		String[] fields = new String[records.size()];
		String[] fValues = new String[records.size()];
		Iterator<String> ite = records.keySet().iterator();
		int i = 0;
		while (ite.hasNext()) {
			fields[i] = ite.next();
			fValues[i] = records.get(fields[i]);
			i++;
		}
		this.insert(table, fields, fValues);
	}

	/**
	 * INSERT INTO table_name (column1,column2,column3,...) VALUES
	 * (value1,value2,value3,...)
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public void insert(String table, String[] fields, String[] fValues)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ").append(table);
			if (fields != null) {
				sql.append(" (").append(fields[0]);
				for (int i = 1; i < fields.length; i++) {
					sql.append(" , ").append(fields[i]);
				}
				sql.append(")");
			}
			sql.append(" VALUES (").append("?");
			for (int i = 1; i < fValues.length; i++) {
				sql.append(", ?");
			}
			sql.append(")");
//			log.debug(sql.toString());
			ps = conn.prepareStatement(sql.toString());
			if (ps != null) {
				for (int j = 0; j < fValues.length; j++) {
					ps.setObject(j + 1, fValues[j]);
				}
				ps.executeUpdate();
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException sqlce) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + sqlce);
					System.err.println(sqlce);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public void insertIgnore(String table, String[] fields, String[] fValues)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			// construct insert SQL
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT IGNORE INTO ").append(table);
			if (fields != null) {
				sql.append(" (").append(fields[0]);
				for (int i = 1; i < fields.length; i++) {
					sql.append(" , ").append(fields[i]);
				}
				sql.append(")");
			}
			sql.append(" VALUES (").append("?");
			for (int i = 1; i < fValues.length; i++) {
				sql.append(", ?");
			}
			sql.append(")");
//			log.debug(sql.toString());
			ps = conn.prepareStatement(sql.toString());
			if (ps != null) {
				for (int j = 0; j < fValues.length; j++) {
					ps.setObject(j + 1, fValues[j]);
				}
				ps.executeUpdate();
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException sqlce) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + sqlce);
					System.err.println(sqlce);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Batch insertion
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public void insertBatch(String table, String[] fields,
			List<? extends Object[]> fValues) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			// set autoCommit as false
			conn.setAutoCommit(false);
			// construct insert SQL
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ").append(table);
			if (fields != null) {
				sql.append(" (").append(fields[0]);
				for (int i = 1; i < fields.length; i++) {
					sql.append(" , ").append(fields[i]);
				}
				sql.append(")");
			}
			int colNo = 0;
			if (fields != null) {
				colNo = fields.length;
			} else {
				// get the columns count
				colNo = this.fetchTableColNo(table);
			}
			sql.append(" VALUES (").append("?");
			for (int i = 1; i < colNo; i++) {
				sql.append(", ?");
			}
			sql.append(")");
//			log.debug(sql.toString());
			ps = conn.prepareStatement(sql.toString());
			if (ps != null) {
				if (fValues != null && !fValues.isEmpty()) {
					int fValuesSize = fValues.size();
					int commitCount = 0;
					for (int i = 0; i < fValuesSize; i++) {
						Object[] values = fValues.get(i);
						for (int j = 0; j < colNo; j++) {
							ps.setObject(j + 1, values[j]);
						}
						ps.addBatch();
						commitCount++;
						while (commitCount == INSERT_BATCH_SIZE) {
							ps.executeBatch();
							conn.commit();
							commitCount = 0;
							ps.clearBatch();
						}
					}
					if (commitCount != 0) {
						ps.executeBatch();
						conn.commit();
						ps.clearBatch();
					}
				}
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Batch insertion
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public void insertIgnoreBatch(String table, String[] fields,
			List<String[]> fValues) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			// set autoCommit as false
			conn.setAutoCommit(false);
			// construct insert SQL
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT IGNORE INTO ").append(table);
			if (fields != null) {
				sql.append(" (").append(fields[0]);
				for (int i = 1; i < fields.length; i++) {
					sql.append(" , ").append(fields[i]);
				}
				sql.append(")");
			}
			int colNo = 0;
			if (fields != null) {
				colNo = fields.length;
			} else {
				// get the columns count
				colNo = this.fetchTableColNo(table);
			}
			sql.append(" VALUES (").append("?");
			for (int i = 1; i < colNo; i++) {
				sql.append(", ?");
			}
			sql.append(")");
//			log.debug(sql.toString());
			ps = conn.prepareStatement(sql.toString());
			if (ps != null) {
				if (fValues != null && !fValues.isEmpty()) {
					int fValuesSize = fValues.size();
					int commitCount = 0;
					for (int i = 0; i < fValuesSize; i++) {
						String[] values = fValues.get(i);
						for (int j = 0; j < colNo; j++) {
							ps.setObject(j + 1, values[j]);
						}
						ps.addBatch();
						commitCount++;
						while (commitCount == INSERT_BATCH_SIZE) {
							ps.executeBatch();
							conn.commit();
							commitCount = 0;
							ps.clearBatch();
						}
					}
					if (commitCount != 0) {
						ps.executeBatch();
						conn.commit();
						ps.clearBatch();
					}
				}
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	public void mergeTables(String tableSource, String tableTarget,
			String[] cFields, String[] cValues, String[] cOps)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(tableTarget)
					.append(" SELECT * FROM ").append(tableSource);
			int i = 0;
			if (cFields != null && cValues != null && cOps != null) {
				sb.append(" WHERE ").append(cFields[i]).append(cOps[i])
						.append("?");
				i++;
				while (i < cFields.length) {
					sb.append(" and ").append(cFields[i]).append(cOps[i])
							.append("?");
					i++;
				}
//				log.debug(sb.toString());
				ps = conn.prepareStatement(sb.toString());
				if (ps != null) {
					for (int j = 0; j < cValues.length; j++) {
						ps.setObject(j + 1, cValues[j]);
					}
					ps.executeUpdate();
				} else {
					throw new HandlingFailureException();
				}
			} else {
				throw new HandlingFailureException();
			}
		} catch (HandlingFailureException hfe) {
			// TODO Auto-generated catch block
//			log.error(this, hfe);
			System.err.println(hfe);
			throw new HandlingFailureException();
		} catch (SQLException sqe) {
			// TODO Auto-generated catch block
//			log.error(this, sqe);
			System.err.println(sqe);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Fetch all the entities by a given query
	 * 
	 * @param query
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<TreeMap<String, String>> fetch(String query)
			throws HandlingFailureException {
		List<TreeMap<String, String>> res = new ArrayList<TreeMap<String, String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
//			log.debug(query);
			ps = conn.prepareStatement(query);
			if (ps != null) {
				rs = ps.executeQuery();
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					TreeMap<String, String> map = new TreeMap<String, String>();
					for (int i = 0; i < colCount; i++) {
						map.put(rs.getMetaData().getColumnName(i + 1),
								rs.getString(i + 1));
					}
					res.add(map);
				}
				return res;
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.warn("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.warn("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Fetch all the entities by the given fields
	 * 
	 * @param table
	 * @param fields
	 * @param limit
	 * @param offset
	 * @return
	 * @throws HandlingFailureException
	 */
	public List<Map<String, Object>> fetch(String table, String[] fields,
			String limit, String offset) throws HandlingFailureException {
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			int i;
			if (fields != null) { // if fields is specified
				i = 0;
				sb.append(fields[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i]);
					i++;
				}
			} else {
				// select *
				sb.append(ASTERISK);
			}
			sb.append(" FROM " + table);
			if (limit != null)
				sb.append(" LIMIT " + limit);
			if (offset != null)
				sb.append(" OFFSET " + offset);
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				rs = ps.executeQuery();
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					Map<String, Object> map = new TreeMap<String, Object>();
					for (i = 0; i < colCount; i++) {
						map.put(rs.getMetaData().getColumnName(i + 1),
								rs.getObject(i + 1));
					}
					res.add(map);
				}
				return res;
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	public List<Map<String, String>> fetchCond(String table, String[] fields,
			String condition, String limit, String offset)
			throws HandlingFailureException {
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			int i;
			if (fields != null) { // if fields is specified
				i = 0;
				sb.append(fields[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i]);
					i++;
				}
			} else {
				// select *
				sb.append(ASTERISK);
			}
			sb.append(" FROM " + table);
			if (condition != null)
				sb.append(" WHERE ").append(condition);
			if (limit != null)
				sb.append(" LIMIT " + limit);
			if (offset != null)
				sb.append(" OFFSET " + offset);
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				rs = ps.executeQuery();
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					Map<String, String> map = new TreeMap<String, String>();
					for (i = 0; i < colCount; i++) {
						map.put(rs.getMetaData().getColumnName(i + 1),
								rs.getString(i + 1));
					}
					res.add(map);
				}
				return res;
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	public List<Map<String, String>> fetchOrderBy(String table,
			String[] fields, String[] cFields, String[] cValues, String[] cOps,
			String oField, String oOp, String limit, String offset)
			throws HandlingFailureException {
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			int i;
			if (fields != null) { // if fields is specified
				i = 0;
				sb.append(fields[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i]);
					i++;
				}
			} else {
				// select *
				sb.append(ASTERISK);
			}
			sb.append(" FROM " + table);
			if (cFields != null && cValues != null && cOps != null) {
				sb.append(" WHERE ");
				i = 0;
				sb.append(cFields[i]).append(cOps[i]).append("?");
				i++;
				while (i < cFields.length) {
					sb.append(" and ").append(cFields[i]).append(cOps[i])
							.append("?");
					i++;
				}
			}
			if (oField != null) {
				sb.append(" ORDER BY ").append(oField);
			}
			if (oOp != null) {
				sb.append(" ").append(oOp);
			}
			if (limit != null)
				sb.append(" LIMIT " + limit);
			if (offset != null)
				sb.append(" OFFSET " + offset);
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				if (cFields != null && cValues != null && cOps != null) {
					for (int j = 0; j < cValues.length; j++) {
						ps.setObject(j + 1, cValues[j]);
					}
				}
				rs = ps.executeQuery();
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					Map<String, String> map = new TreeMap<String, String>();
					for (i = 0; i < colCount; i++) {
						map.put(rs.getMetaData().getColumnName(i + 1),
								rs.getString(i + 1));
					}
					res.add(map);
				}
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return res;
	}

	/**
	 * 
	 * @param table
	 * @return
	 * @throws HandlingFailureException
	 */
	public int count(String table) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int total;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT COUNT(*) FROM ").append(table);
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				rs = ps.executeQuery();
				if (rs.next())
					total = rs.getInt(1);
				else
					throw new HandlingFailureException();
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return total;
	}

	public int countCond(String table, String[] cFields, String[] cValues,
			String[] cOps) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int total = 0;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT COUNT(*) FROM ").append(table);
			if (cFields != null && cValues != null && cOps != null) {
				sb.append(" WHERE ");
				int i = 0;
				sb.append(cFields[i]).append(cOps[i]).append("?");
				i++;
				while (i < cFields.length) {
					sb.append(" and ").append(cFields[i]).append(cOps[i])
							.append("?");
					i++;
				}
//				log.debug(sb.toString());
				ps = conn.prepareStatement(sb.toString());
				if (ps != null) {
					for (int j = 0; j < cValues.length; j++) {
						ps.setObject(j + 1, cValues[j]);
					}
					rs = ps.executeQuery();
					if (rs.next()) {
						total = rs.getInt(1);
					} else {
						throw new HandlingFailureException();
					}
				} else {
					throw new HandlingFailureException();
				}
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return total;
	}

	public int countCond(String table, String condition)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int total;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT COUNT(*) FROM ").append(table);
			if (condition != null) {
				sb.append(" WHERE ").append(condition);
			}
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				rs = ps.executeQuery();
				if (rs.next())
					total = rs.getInt(1);
				else
					throw new HandlingFailureException();
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return total;
	}

	/**
	 * Create a table by the given query
	 * 
	 * @param query
	 * @throws HandlingFailureException
	 */
	public void createTable(String query) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
//			log.debug(query);
			ps = conn.prepareStatement(query);
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table is created.");
				System.out.println("Table is created.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			this.closeConnection(conn);
		}
	}

	/**
	 * Create a table by the given table, field name and type
	 * 
	 * @param table
	 * @param fields
	 * @param fTypes
	 * @throws HandlingFailureException
	 */
	public void createTable(String table, String[] fields, String[] fTypes)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS " + table);
			if (fields != null && fTypes != null) {
				int i = 0;
				sb.append(" ( " + fields[i] + " " + fTypes[i]);
				i++;
				while (i < fields.length) {
					sb.append(", " + fields[i] + " " + fTypes[i]);
					i++;
				}
				sb.append(" )");
//				log.debug(sb.toString());
				ps = conn.prepareStatement(sb.toString());
				if (ps != null) {
					ps.executeUpdate();
//					log.info("Table " + table + " is created.");
					System.out.println("Table " + table + " is created.");
				} else
					throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	public void AddICField(String table, String icField, String icfType)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("ALTER TABLE " + table + " MODIFY " + icField + " "
					+ icfType + " AUTO_INCREMENT");
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + table + " is created.");
				System.out.println("Table " + table + " is created.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	public void AddField(String table, String field, String type,
			String defValue) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("ALTER TABLE ").append(table).append(" ADD COLUMN ")
					.append(field).append(" ").append(type);
			if (defValue != null) {
				sb.append(" DEFAULT '").append(defValue).append("'");
			}
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + table + " is created.");
				System.out.println("Table " + table + " is created.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Drop table
	 * 
	 * @param table
	 * @throws HandlingFailureException
	 */
	public void dropTable(String table) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			String query = "DROP TABLE  IF EXISTS " + table;
			ps = conn.prepareStatement(query);
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + table + " is droped.");
				System.out.println("Table " + table + " is droped.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * Delete record from table
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public void delete(String table, String[] cFields, String[] cValues)
			throws HandlingFailureException {
		// check if the table exists
		if (this.checkTableExist(table)) {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = this.fetchConnection();
				StringBuilder sb = new StringBuilder();
				sb.append("DELETE FROM " + table);
				if (cFields != null && cValues != null) {
					int i = 0;
					sb.append(" WHERE ").append(cFields[i]).append(" = ?");
					i++;
					while (i < cFields.length) {
						sb.append(" and ").append(cFields[i]).append(" = ?");
						i++;
					}
//					log.debug(sb.toString());
					ps = conn.prepareStatement(sb.toString());
					if (ps != null) {
						for (int j = 0; j < cValues.length; j++) {
							ps.setObject(j + 1, cValues[j]);
						}
						ps.executeUpdate();
					} else
						throw new HandlingFailureException();
				}
			} catch (SQLException sqle) {
				// TODO Auto-generated catch block
//				log.error(this, sqle);
				System.err.println(sqle);
				throw new HandlingFailureException();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException pse) {
						// TODO Auto-generated catch block
//						log.warn("PreparedStatement fail to close\n" + pse);
						System.err.println(pse);
					}
				}
				this.closeConnection(conn);
			}
		}
	}

	/**
	 * Truncate table
	 * 
	 * @param table
	 * @throws HandlingFailureException
	 */
	public void truncateTable(String table) throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			String query = "TRUNCATE TABLE " + table;
			ps = conn.prepareStatement(query);
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + table + " is truncated.");
				System.out.println("Table " + table + " is truncated.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * 
	 * @param table
	 * @param fields
	 * @throws HandlingFailureException
	 */
	public void addPrimaryKey(String table, String[] fields)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder query = new StringBuilder();
			query.append("ALTER TABLE ").append(table)
					.append(" ADD CONSTRAINT pk");
			for (int i = 0; i < fields.length; i++) {
				query.append("_").append(fields[i]);
			}
			int j = 0;
			query.append(" PRIMARY KEY (").append(fields[j]);
			while (j + 1 < fields.length) {
				query.append(",").append(fields[j + 1]);
				j++;
			}
			query.append(")");
//			log.debug(query.toString());
			ps = conn.prepareStatement(query.toString());
			if (ps != null)
				ps.executeUpdate();
			else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	public void createIndex(String table, String[] fields, String index)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE INDEX ").append(index).append(" ON ")
					.append(table).append("(").append(fields[0]);
			for (int i = 1; i < fields.length; i++) {
				sb.append(",").append(fields[i]);
			}
			sb.append(")");
//			log.debug(sb.toString());
			ps = conn.prepareStatement(sb.toString());
			if (ps != null)
				ps.executeUpdate();
			else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException pse) {
					// TODO Auto-generated catch block
//					log.warn("PreparedStatement fail to close\n" + pse);
					System.err.println(pse);
				}
			}
			this.closeConnection(conn);
		}
	}

	public void update(String table, String[] fields, String[] fValues,
			String[] cFields, String[] cValues, String[] cOps)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = this.fetchConnection();
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE ").append(table);
			int i = 0;
			if (fields != null) { // if fields is specified
				sb.append(" SET ").append(fields[i]).append(" = ?");
				i++;
				while (i < fields.length) {
					sb.append(", ").append(fields[i]).append(" = ?");
					i++;
				}
			}
			if (cFields != null && cValues != null && cOps != null) {
				sb.append(" WHERE ");
				i = 0;
				sb.append(cFields[i]).append(cOps[i]).append("?");
				i++;
				while (i < cFields.length) {
					sb.append(" and ").append(cFields[i]).append(cOps[i])
							.append("?");
					i++;
				}
				// else no condition query
//				log.debug(sb.toString());
				ps = conn.prepareStatement(sb.toString());
				if (ps != null) {
					for (i = 0; i < fValues.length; i++) {
						ps.setObject(i + 1, fValues[i]);
					}
					for (int j = 0; j < cValues.length; j++) {
						ps.setObject(i + j + 1, cValues[j]);
					}
					ps.executeUpdate();
				} else {
					throw new HandlingFailureException();
				}
			} else {
				throw new HandlingFailureException();
			}
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.warn("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	@Override
	public boolean checkTableExist(String table) {
		String query = "SELECT COUNT(*)  FROM information_schema.TABLES WHERE TABLE_NAME=? AND TABLE_SCHEMA=?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean exist = false;
//		log.debug(query);
		try {
			conn = this.fetchConnection();
			ps = conn.prepareStatement(query);
			if (ps != null) {
				ps.setObject(1, table);
				ps.setObject(2, this.database);
				rs = ps.executeQuery();
				if (rs.next()) {
					int num = rs.getInt(1);
					if (num != 0)
						exist = true;
				}
			}
		} catch (HandlingFailureException hfe) {
			// TODO Auto-generated catch block
//			log.error(this, hfe);
			System.err.println(hfe);
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException rse) {
				// TODO Auto-generated catch block
//				log.error("ResultSet fail to close\n" + rse);
				System.err.println(rse);
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
		return exist;
	}

	public void renameTable(String tableOrigin, String table)
			throws HandlingFailureException {
		String query = "RENAME TABLE " + tableOrigin + " TO " + table;
		Connection conn = null;
		PreparedStatement ps = null;
//		log.debug(query);
		try {
			conn = this.fetchConnection();
			ps = conn.prepareStatement(query);
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + tableOrigin + " is renamed to " + table);
				System.out.println("Table " + tableOrigin + " is renamed to " + table);
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	/**
	 * 
	 * @param table
	 * @param tables
	 * @throws HandlingFailureException
	 */
	public void createTableByUnionAllTables(String table, String[] tables)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(table).append(" AS SELECT * FROM ")
				.append(tables[0]);
		for (int i = 1; i < tables.length; i++) {
			sb.append(" UNION ALL SELECT * FROM ").append(tables[i]);
		}
//		log.debug(sb.toString());
		try {
			conn = this.fetchConnection();
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + table + " is created.");
				System.out.println("Table " + table + " is created.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}

	public void createTableByUnionTables(String table, String[] tables)
			throws HandlingFailureException {
		Connection conn = null;
		PreparedStatement ps = null;
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(table).append(" AS SELECT * FROM ")
				.append(tables[0]);
		for (int i = 1; i < tables.length; i++) {
			sb.append(" UNION SELECT * FROM ").append(tables[i]);
		}
//		log.debug(sb.toString());
		try {
			conn = this.fetchConnection();
			ps = conn.prepareStatement(sb.toString());
			if (ps != null) {
				ps.executeUpdate();
//				log.info("Table " + table + " is created.");
				System.out.println("Table " + table + " is created.");
			} else
				throw new HandlingFailureException();
		} catch (SQLException sqle) {
			// TODO Auto-generated catch block
//			log.error(this, sqle);
			System.err.println(sqle);
			throw new HandlingFailureException();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException pse) {
				// TODO Auto-generated catch block
//				log.error("PreparedStatement fail to close\n" + pse);
				System.err.println(pse);
			}
			this.closeConnection(conn);
		}
	}
}
