package org.feldspaten.hyperion.persistence;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQL {

	/* Connection parameters */

	/** Hostname of the database */
	private String db_hostname;
	/** Port of the database */
	private int db_port;
	/** Database name for the connection */
	private String db_database;
	/** Username for the database */
	private String db_username;
	/** Password for the connection */
	private String db_password;

	/** Default port for MySQL connections */
	public static final int DEFAULT_PORT = 3306;

	/** Default names encoding used for a statement */
	private String defaultEncoding = "utf8";
	/** Default timezone for a statement. Adapt to your needs */
	private String timezone = "+01:00";
	/** Offset in seconds for the timezone in seconds */
	private long timezoneOffset = 60L * 60L;

	/** JDBC connection */
	private java.sql.Connection conn = null;

	/** {@link SimpleDateFormat} for formatting dates to MySQL date instances */
	static final SimpleDateFormat sqlDateTimeFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/** {@link SimpleDateFormat} for formatting dates to MySQL date instances */
	static final SimpleDateFormat sqlDateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd");

	/** Closed flag. After closing no further connections are established */
	private static boolean closed = false;

	/** Implementation of Map to provide easier access to the values map */
	public static class ValuesMap extends HashMap<String, String> implements
			Map<String, String> {
		/** Serialisation ID */
		private static final long serialVersionUID = -3241025828184612599L;

		@Override
		/**
		 * Escape string and insert it
		 */
		public String put(String key, String value) {
			if (value == null)
				value = "''";
			else
				value = "'" + MySQL.sqlSafeString(value) + "'";
			return super.put(key, value);
		}

		/**
		 * Insert into the values map
		 * 
		 * @param key
		 *            Key of the value
		 * @param value
		 *            Value
		 * @param noEscape
		 *            If true, the given string will not be escaped
		 * @return key of the inserted String
		 */
		public String put(String key, String value, boolean noEscape) {
			if (value == null)
				value = (noEscape ? "" : "''");
			else {
				if (!noEscape)
					value = "'" + MySQL.sqlSafeString(value) + "'";
			}
			return super.put(key, value);
		}

		public String put(String key, long value) {
			return super.put(key, Long.toString(value));
		}

		public String put(String key, float value) {
			return super.put(key, Float.toString(value));
		}

		public String put(String key, double value) {
			return super.put(key, Double.toString(value));
		}

	}

	/**
	 * Helper class for creating queries
	 * 
	 * @author phoenix
	 *
	 */
	public static class Query {
		/** Underlying statement */
		private final Statement stmt;
		/** {@link ResultSet} when quering data */
		protected ResultSet rs = null;

		/** Set offset for this query */
		private long offset = 0;
		/** Limit for the resulting rows */
		private long limit = 100;
		/** Name of the table the query operates on */
		private String tablename;
		/** Ordering */
		private String order;
		/** True if ascending order, false if descending order */
		private boolean orderAscending;
		/** Created query string */
		private String query = null;
		/** Enclose query in transaction */
		private boolean transaction = false;

		/** Where clauses */
		private final List<String> whereClauses = new ArrayList<>(10);
		/** Connector of the where clauses */
		private String whereClauseConnector = "AND";

		/** Optional ON DUPLICATE KEY statement for insert queries */
		private String insertOnDuplicateKeyStatement = null;

		public Query(final MySQL mysql) throws SQLException {
			this.stmt = mysql.createStatement();
		}

		public String getLastQuery() {
			return query;
		}

		public String getOrder() {
			return order;
		}

		public void setOrder(String order) {
			this.order = order;
		}

		public boolean isOrderAscending() {
			return orderAscending;
		}

		public void setOrderAscending(boolean orderAscending) {
			this.orderAscending = orderAscending;
		}

		public String getTablename() {
			return tablename;
		}

		public void setTablename(String tablename) {
			this.tablename = tablename;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public long getLimit() {
			return limit;
		}

		public void setLimit(long limit) {
			this.limit = limit;
		}

		public String getWhereClause() {
			synchronized (whereClauses) {
				if (whereClauses.isEmpty())
					return "";

				final StringBuffer buffer = new StringBuffer();
				boolean first = true;
				final String connector = " " + this.whereClauseConnector + " ";
				for (final String clause : whereClauses) {
					if (first)
						first = false;
					else
						buffer.append(connector);
					buffer.append('(');
					buffer.append(clause);
					buffer.append(')');
				}
				return buffer.toString();
			}

		}

		/**
		 * Set the connecto between where clauses. Acceptable connectors are
		 * <b>AND</b> or <b>OR</b>
		 * 
		 * @param connector
		 *            Connector between where clauses
		 */
		public void setWhereClauseConnector(String connector) {
			connector = connector.trim().toUpperCase();
			if (connector.equals("AND"))
				this.whereClauseConnector = "AND";
			else if (connector.equals("OR"))
				this.whereClauseConnector = "OR";
			else
				throw new IllegalArgumentException(
						"Illegal where clause connector: " + connector);
		}

		/**
		 * Replaces ALL where clauses by this one clause
		 * 
		 * @param clause
		 *            to be set
		 */
		public void setWhereClause(String clause) {
			synchronized (whereClauses) {
				this.whereClauses.clear();
				if (clause == null || clause.isEmpty())
					return;
				else
					this.whereClauses.add(clause);
			}
		}

		public void addWhereClause(final String clause) {
			if (clause == null || clause.isEmpty())
				return;

			synchronized (whereClauses) {
				this.whereClauses.add(clause);
			}
		}

		public void clearWhereClauses() {
			synchronized (whereClauses) {
				this.whereClauses.clear();
			}
		}

		public synchronized void cleanup() throws SQLException {
			if (rs != null)
				rs.close();
			rs = null;
		}

		protected synchronized ResultSet executeQuery(final String sql)
				throws SQLException {
			cleanup();
			this.query = sql;
			this.rs = stmt.executeQuery(sql);
			return this.rs;
		}

		protected synchronized ResultSet selectQuery() throws SQLException {
			return this.selectQuery("*");
		}

		protected synchronized ResultSet selectQuery(final String rows)
				throws SQLException {
			// Generate SELECT query
			final StringBuffer query = new StringBuffer();
			query.append("SELECT " + rows + " FROM `");
			query.append(tablename);
			final String whereClause = getWhereClause();
			if (whereClause != null && whereClause.length() > 0) {
				query.append("` WHERE ");
				query.append(whereClause);
			} else
				query.append('`');
			if (order != null && order.length() > 0) {
				query.append(" ORDER BY ");
				query.append(MySQL.sqlSafeString(order));
				if (isOrderAscending())
					query.append(" ASC ");
				else
					query.append(" DESC ");
			}
			if (limit > 0) {
				query.append(" LIMIT ");
				query.append(limit);
				if (offset > 0) {
					query.append(" OFFSET ");
					query.append(offset);
				}
			}
			query.append(';');
			return executeQuery(query.toString());
		}

		public synchronized ResultSet getResultSet() {
			return this.rs;
		}

		public synchronized void close() {
			if (transaction)
				try {
					endTransaction();
				} catch (SQLException e1) {
				}
			try {

				cleanup();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public synchronized boolean next() throws SQLException {
			if (rs == null)
				return false;
			return rs.next();
		}

		public synchronized boolean execute(String sql) throws SQLException {
			cleanup();
			this.query = sql;
			return stmt.execute(sql);
		}

		public synchronized int executeUpdate(String sql) throws SQLException {
			cleanup();
			this.query = sql;
			return stmt.executeUpdate(sql);
		}

		public synchronized ResultSetMetaData getResultSetMetaData()
				throws SQLException {
			if (rs == null)
				throw new IllegalStateException(
						"Cannot get ResultSetMetData when no ResultSet is available");
			return rs.getMetaData();
		}

		public synchronized int insert(Map<String, String> values)
				throws SQLException {
			return this.insertStatement(this.tablename, values, false);
		}

		public synchronized int insert(String tablename,
				Map<String, String> values) throws SQLException {
			return this.insertStatement(tablename, values, false);
		}

		public synchronized int insertIgnore(Map<String, String> values)
				throws SQLException {
			return this.insertStatement(this.tablename, values, true);
		}

		public synchronized int insertIgnore(String tablename,
				Map<String, String> values) throws SQLException {
			return this.insertStatement(this.tablename, values, true);
		}

		protected synchronized int insertStatement(String tablename,
				Map<String, String> values, final boolean ignore)
				throws SQLException {
			if (tablename.isEmpty() || values.isEmpty())
				return 0;

			final StringBuffer buffer = new StringBuffer();
			if (ignore)
				buffer.append("INSERT IGNORE INTO `");
			else
				buffer.append("INSERT INTO `");
			buffer.append(tablename);
			buffer.append("` (");
			{
				final StringBuffer valuesString = new StringBuffer();
				boolean first = true;
				for (final String key : values.keySet()) {
					if (first)
						first = false;
					else {
						buffer.append(", ");
						valuesString.append(", ");
					}

					buffer.append('`');
					buffer.append(key);
					buffer.append('`');
					valuesString.append(values.get(key));
				}
				buffer.append(") VALUES (");
				buffer.append(valuesString.toString());
			}

			if (this.insertOnDuplicateKeyStatement != null) {
				buffer.append("ON DUPLICATE KEY ");
				buffer.append(this.insertOnDuplicateKeyStatement);
			}

			buffer.append(");");

			return this.executeUpdate(buffer.toString());
		}

		public void setOnDuplicateKeyStatement(final String statement) {
			this.insertOnDuplicateKeyStatement = statement;
		}

		public synchronized int update(Map<String, String> values)
				throws SQLException {
			return this.update(this.tablename, values);
		}

		public synchronized int update(String tablename,
				Map<String, String> values) throws SQLException {
			if (tablename.isEmpty() || values.isEmpty())
				return 0;

			final StringBuffer buffer = new StringBuffer();
			buffer.append("UPDATE `");
			buffer.append(tablename);
			buffer.append("` SET ");

			boolean first = true;
			for (final String key : values.keySet()) {
				if (first)
					first = false;
				else {
					buffer.append(", ");
				}

				buffer.append('`');
				buffer.append(key);
				buffer.append("` = ");
				buffer.append(values.get(key));
			}

			buffer.append(" WHERE " + getWhereClause());

			buffer.append(";");

			return this.executeUpdate(buffer.toString());
		}

		public synchronized void insertOrUpdate(ValuesMap values)
				throws SQLException {
			this.insertOrUpdate(this.tablename, values);
		}

		public synchronized void insertOrUpdate(String tableName,
				ValuesMap values) throws SQLException {
			if (tablename.isEmpty() || values.isEmpty())
				return;

			final StringBuffer buffer = new StringBuffer();
			buffer.append("INSERT INTO `");
			buffer.append(tablename);
			buffer.append("` (");
			{
				final StringBuffer valuesString = new StringBuffer();
				boolean first = true;
				for (final String key : values.keySet()) {
					if (first)
						first = false;
					else {
						buffer.append(", ");
						valuesString.append(", ");
					}

					buffer.append('`');
					buffer.append(key);
					buffer.append('`');
					valuesString.append("'");
					valuesString.append(values.get(key));
					valuesString.append("'");
				}
				buffer.append(") VALUES (");
				buffer.append(valuesString.toString());
			}
			buffer.append(") ON DUPLICATE KEY UPDATE ");
			{
				boolean first = true;
				for (final String key : values.keySet()) {
					if (first)
						first = false;
					else
						buffer.append(", ");
					buffer.append('`');
					buffer.append(key);
					buffer.append("` = '");
					buffer.append(values.get(key));
					buffer.append("'");
				}
			}

			this.executeUpdate(buffer.toString());
		}

		public synchronized void startTransaction() throws SQLException {
			if (transaction)
				return;
			else {
				transaction = true;
				this.execute("START TRANSACTION;");
			}
		}

		public synchronized void endTransaction() throws SQLException {
			if (!transaction)
				return;
			else {
				this.execute("COMMIT;");
				transaction = false;
			}
		}

		/**
		 * Delete from the table with the given where clause
		 */
		public synchronized void delete() throws SQLException {
			final StringBuffer buffer = new StringBuffer();
			buffer.append("DELETE FROM `");
			buffer.append(tablename);
			buffer.append("`");
			final String whereClause = getWhereClause();
			if (whereClause.length() > 0) {
				buffer.append(" WHERE ");
				buffer.append(whereClause);
			}
			buffer.append(';');
			execute(buffer.toString());
		}

		/**
		 * Clear the whole table
		 */
		public synchronized void clear() throws SQLException {
			final StringBuffer buffer = new StringBuffer();
			buffer.append("DELETE FROM `");
			buffer.append(tablename);
			buffer.append("`");
			buffer.append(';');
			execute(buffer.toString());
		}
	}

	/**
	 * Static class constructor
	 * 
	 * This constructor must initialise the JDBC driver
	 */
	static {
		try {
			// newInstance() fixes some buggy implementations of the JDBC
			// driver

			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (ClassNotFoundException e) {
			System.err
					.println("Could not find jdbc driver. Try to download a valid jdbc driver from http://www.oracle.com!");
			e.printStackTrace(System.err);
		} catch (InstantiationException e) {
			System.err.println("Could not find jdbc driver");
			e.printStackTrace(System.err);
		} catch (IllegalAccessException e) {
			System.err
					.println("Access to JDBC driver denied. Try to download a valid jdbc driver from http://www.oracle.com!");
		}

	}

	public MySQL(String hostname, int port, String database, String username,
			String password) {
		super();
		this.db_hostname = hostname;
		this.db_port = port;
		this.db_database = database;
		this.db_username = username;
		this.db_password = password;
	}

	public MySQL(String hostname, String database, String username,
			String password) {
		this(hostname, DEFAULT_PORT, database, username, password);
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public long getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(long timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	/**
	 * Initialized the connection with the given parameters
	 * 
	 * @throws SQLException
	 *             Thrown if the initial connection fails
	 */
	public void initialize() throws SQLException {
		this.reconnectThrowsException();
	}

	public void execSql(final String sql) throws SQLException {
		final Statement stmt = createStatement();
		try {
			stmt.execute(sql);
		} finally {
			stmt.close();
		}
	}

	/**
	 * Execute the given SQL statements. The statements are executed
	 * sequentially without a transaction
	 * 
	 * @param sqls
	 *            to be executed
	 * @throws SQLException
	 *             Thrown if occurring on database
	 */
	public void execSql(final String[] sqls) throws SQLException {
		execSql(sqls, false);
	}

	/**
	 * Execute the given SQL statements. If transaction is set to true, the
	 * whole sql statements are enclosed within a TRANSACTION
	 * 
	 * @param sqls
	 *            to be executed
	 * @param transaction
	 *            True if the statements should be enclosed within a transaction
	 * @throws SQLException
	 *             Thrown if occurring on database
	 */
	public void execSql(final String[] sqls, final boolean transaction)
			throws SQLException {
		final Statement stmt = createStatement();
		try {
			if (transaction)
				stmt.execute("START TRANSACTION;");

			for (String sql : sqls)
				stmt.execute(sql);
		} finally {
			if (transaction)
				stmt.execute("COMMIT;");
			stmt.close();
		}
	}

	public void executeSql(final String sql) throws SQLException {
		execSql(sql);
	}

	public void executeUpdate(final String sql) throws SQLException {
		final Statement stmt = createStatement();
		try {
			stmt.executeUpdate(sql);
		} finally {
			stmt.close();
		}
	}

	/**
	 * Connects or reconnects the database. Useful, if you changed the database
	 * connection parameters, i.e. hostname, port, database, username and
	 * password
	 */
	public synchronized void connect() {
		try {
			if (conn != null)
				conn.close();
			checkConnection();
		} catch (SQLException e) {
			// Ignore
		}
	}

	/**
	 * Checks current connection and re-connects if the connection is broken
	 * 
	 * @return true if the connection is alive after the call
	 */
	public synchronized boolean checkConnection() {
		if (closed)
			return false;
		final int timeout = 5000;
		try {
			if (conn != null && conn.isValid(timeout))
				return true;
		} catch (SQLException e) {
			// Consider as a broken connection.
		}

		final String url = createJDBCAddress();

		java.sql.Connection con = conn;
		try {
			con = DriverManager.getConnection(url);

			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.err.println("Error closing existing JDBC: "
						+ e.getLocalizedMessage());
				con.close();
				return false;
			}

			// Set new connection
			conn = con;
			return true;

		} catch (SQLException e) {
			System.err.println("New JDBC Connection failed: "
					+ e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @return created JDBC address string
	 */
	protected String createJDBCAddress() {
		String url_ = "jdbc:mysql://" + db_hostname + ":" + db_port + "/"
				+ db_database + "?user=" + db_username
				+ "&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8";
		if (db_password != null && db_password.length() > 0)
			url_ += "&password=" + db_password;
		return url_;
	}

	/**
	 * (Re)connects the database and throws an {@link SQLException} if failed
	 */
	protected synchronized void reconnectThrowsException() throws SQLException {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			// Ignore
		}

		final String url;
		{
			String url_ = "jdbc:mysql://" + db_hostname + ":" + db_port + "/"
					+ db_database + "?user=" + db_username;
			if (db_password != null && db_password.length() > 0)
				url_ += "&password=" + db_password;
			url = url_;
		}

		java.sql.Connection con = conn;
		try {
			con = DriverManager.getConnection(url);

			// Set new connection
			conn = con;

		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * Make a string SQL-Safe, i.e. replace all <i>'</i> with <i>\'</i>
	 * 
	 * @param string
	 *            to be handled
	 * @return SQL-Safe string
	 */
	public static String sqlSafeString(final String string) {
		final StringBuffer buffer = new StringBuffer();
		boolean escaped = false;
		for (final char ch : string.toCharArray()) {
			if (ch == '\\')
				escaped = true;
			else {
				if (ch == '\'') {
					if (!escaped)
						buffer.append('\\');
				}
				escaped = false;
			}
			buffer.append(ch);
		}

		return buffer.toString();
	}

	/**
	 * Create SQL date out of ad {@link Date} instance
	 * 
	 * @param date
	 *            to be converted
	 * @return SQL date string
	 */
	public static String sqlDateTime(final Date date) {
		if (date == null)
			return "NULL";
		return sqlDateTimeFormatter.format(date);
	}

	/**
	 * Create SQL date out of ad {@link Date} instance
	 * 
	 * @param date
	 *            to be converted
	 * @return SQL date string
	 */
	public static String sqlDate(final Date date) {
		if (date == null)
			return "NULL";
		return sqlDateFormatter.format(date);
	}

	/**
	 * Checks the connection and creates a new statement.
	 * 
	 * @return {@link Statement} for the connection
	 * @throws SQLException
	 *             Packet {@link SQLException} if occurring
	 */
	public synchronized Statement createStatement() throws SQLException {
		checkConnection();
		if (conn == null)
			throw new SQLException("Error setting up SQL connection");
		final Statement statement = conn.createStatement();
		statement.execute("SET NAMES " + defaultEncoding + ";");
		statement.execute("SET time_zone = '" + timezone + "';");
		return statement;

	}

	/**
	 * Checks the connection and creates a new preapared statement.
	 * 
	 * @return {@link Statement} for the connection
	 * @throws SQLException
	 *             Packet {@link SQLException} if occurring
	 */
	public PreparedStatement createPreparedStatement(final String sql)
			throws SQLException {
		checkConnection();
		if (conn == null)
			throw new SQLException("Error setting up SQL connection");
		final PreparedStatement statement = conn.prepareStatement(sql);
		statement.execute("SET NAMES " + defaultEncoding + ";");
		statement.execute("SET time_zone = '" + timezone + "';");
		return statement;
	}

	/**
	 * Close SQL connection
	 */
	public void close() {
		closed = true;
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.err.println("Error closing SQL connection: "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Get the Version of the database management system, i.e. the version of
	 * the MySQL server instance
	 * 
	 * @return MySQL server instance version
	 * @throws SQLException
	 *             Thrown if occurring while querying
	 */
	public String getDBMSVersion() throws SQLException {
		final java.sql.Statement stmt = createStatement();
		try {

			stmt.executeQuery("SELECT version();");

			ResultSet rs = stmt.getResultSet();
			try {
				if (!rs.next())
					return "";
				return rs.getString(1);
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}

	}

	public Date getSqlDate(final ResultSet rs, final String columnName)
			throws SQLException {
		final Timestamp timestamp = rs.getTimestamp(columnName);
		if (timestamp == null)
			return new Date();
		else {
			final long time = timestamp.getTime() + timezoneOffset;
			return new Date(time);
		}
	}

	/**
	 * Espaced (including "'" characters) safe SQL string
	 * 
	 * @param string
	 *            to be processed
	 * @return
	 */
	public static String escapeString(String string) {
		return "'" + sqlSafeString(string) + "'";
	}
}
