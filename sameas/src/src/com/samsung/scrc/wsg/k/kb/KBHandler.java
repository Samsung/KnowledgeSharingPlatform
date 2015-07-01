package com.samsung.scrc.wsg.k.kb;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.samsung.scrc.wsg.k.kb.exception.HandlingFailureException;

/**
 * @author yuxie
 * 
 */
public abstract class KBHandler {

	public abstract boolean init(String host, String port, String user,
			String password, String database);

	/**
	 * Fetch a connection to KB
	 * 
	 * @return
	 * @throws HandlingFailureException
	 */
	public abstract boolean checkConnection();

	/**
	 * Release the KB handler
	 */
	public abstract void release();

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param limit
	 * @param offset
	 * @return
	 * @throws HandlingFailureException
	 */
	public abstract List<Map<String, Object>> fetch(String table,
			String[] fields, String limit, String offset)
			throws HandlingFailureException;

	public abstract List<Map<String, String>> fetchCond(String table,
			String[] fields, String condition, String limit, String offset)
			throws HandlingFailureException;

	public abstract List<String> fetchCond(String table, String field,
			String[] cFields, String[] cValues, String[] cOps, String limit,
			String offset) throws HandlingFailureException;

	public abstract int countCond(String table, String[] cFields,
			String[] cValues, String[] cOps) throws HandlingFailureException;

	public abstract int countCond(String table, String condition)
			throws HandlingFailureException;

	public abstract void delete(String table, String[] cFields, String[] cValues)
			throws HandlingFailureException;

	public abstract void dropTable(String table)
			throws HandlingFailureException;

	public abstract List<Map<String, String>> fetchOrderBy(String table,
			String[] fields, String[] cFields, String[] cValues, String[] cOps,
			String oField, String oOp, String limit, String offset)
			throws HandlingFailureException;

	public abstract void renameTable(String tableOrigin, String table)
			throws HandlingFailureException;

	public abstract void createTableByUnionAllTables(String table,
			String[] tables) throws HandlingFailureException;

	public abstract void createTableByUnionTables(String table, String[] tables)
			throws HandlingFailureException;

	public abstract void mergeTables(String tableSource, String tableTarget,
			String[] cFields, String[] cValues, String[] cOps)
			throws HandlingFailureException;

	public abstract void truncateTable(String table)
			throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public abstract void insert(String table, String[] fields, String[] fValues)
			throws HandlingFailureException;

	/**
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
	public abstract List<Map<String, String>> fetchCond(String table,
			String[] fields, String[] cFields, String[] cValues, String[] cOps,
			String limit, String offset) throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @param field
	 * @param cField
	 * @param cValues
	 * @return
	 * @throws HandlingFailureException
	 */
	public abstract Map<String, List<String>> fetchCondIn(String table,
			String field, String cField, String[] cValues)
			throws HandlingFailureException;

	public abstract Map<String, HashSet<String>> fetchCondBatch(String table,
			String field, String cField, String Op, String[] cValues)
			throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @throws HandlingFailureException
	 */
	public abstract void insertBatch(String table, String[] fields,
			List<? extends Object[]> fValues) throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @return
	 * @throws HandlingFailureException
	 */
	public abstract List<String> fetchPrimaryKeys(String table)
			throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param fTypes
	 * @throws HandlingFailureException
	 */
	public abstract void createTable(String table, String[] fields,
			String[] fTypes) throws HandlingFailureException;

	/**
	 * 
	 * @param query
	 * @throws HandlingFailureException
	 */
	public abstract void createTable(String query)
			throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @return
	 * @throws HandlingFailureException
	 */
	public abstract int count(String table) throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param fValues
	 * @param cFields
	 * @param cValues
	 * @param cOps
	 * @throws HandlingFailureException
	 */
	public abstract void update(String table, String[] fields,
			String[] fValues, String[] cFields, String[] cValues, String[] cOps)
			throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @param fields
	 * @param index
	 * @throws HandlingFailureException
	 */
	public abstract void createIndex(String table, String[] fields, String index)
			throws HandlingFailureException;

	/**
	 * 
	 * @param table
	 * @return
	 */
	public abstract boolean checkTableExist(String table);

}
