package org.feldspaten.hyperion.persistence;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MySQL {

	private String db_hostname;
	private String db_username;
	private String db_password;
	private String db_database;
	private int db_port = DEFAULT_PORT;

	/** Default port for MySQL connections */
	public static final int DEFAULT_PORT = 3306;

	/** JDBC connection */
	private static java.sql.Connection conn = null;

	/**
	 * {@link SimpleDateFormat} for formatting date and times to MySQL date
	 * instances
	 */
	static final SimpleDateFormat sqlDateTimeFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/** {@link SimpleDateFormat} for formatting dates to MySQL date instances */
	static final SimpleDateFormat sqlDateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd");

	/** Closed flag. After closing no further connections are established */
	private static boolean closed = false;

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

	/**
	 * Helper class for executing nearly all types of queries
	 * 
	 * @author phoenix
	 *
	 */
	public class Query {
		/** Underlying statement for the query */
		protected final Statement stmt;
		/** Underlying {@link ResultSet} for returning results */
		protected ResultSet rs = null;

		/** Offset of the result set */
		private int offset = 0;
		/** Maximum number of result sets */
		private int limit = 100;
		/** The where clasuses */
		private List<String> whereClauses = new LinkedList<>();
		/** Name of the table the query belongs to */
		private String tablename;
		/** Order by */
		private String order;
		/** Ascending or descending order */
		private boolean orderAscending;
		/** SQL query that is executed last */
		private String query = null;
		/** If the query should be enclosed as transaction */
		private boolean transaction = false;
		/** Selection arguments for SELECT queries */
		private List<String> selectionArguments = new LinkedList<>();

		/**
		 * Create new Query instance
		 * 
		 * @throws SQLException
		 *             If thrown while creating a new statement
		 */
		protected Query() throws SQLException {
			this.stmt = createStatement();
		}

		/**
		 * @return the last executed query
		 */
		public String getLastQuery() {
			return query;
		}

		/**
		 * @return the order of the query, if set
		 */
		public String getOrder() {
			return order;
		}

		/**
		 * Set the order by directive
		 * 
		 * @param order
		 *            to be set
		 */
		public void setOrder(String order) {
			this.order = order;
		}

		/**
		 * @return true if ascending order is set
		 */
		public boolean isOrderAscending() {
			return orderAscending;
		}

		/**
		 * Set ascending or descending order
		 * 
		 * @param orderAscending
		 *            true if ascending, false if descending order
		 */
		public void setOrderAscending(boolean orderAscending) {
			this.orderAscending = orderAscending;
		}

		/**
		 * Set ascending order
		 */
		public void setOrderAscending() {
			this.orderAscending = true;
		}

		/**
		 * Set descending order
		 */
		public void setOrderDecending() {
			this.orderAscending = false;
		}

		/**
		 * 
		 * @return the name of the table the query belongs to
		 */
		public String getTablename() {
			return tablename;
		}

		/**
		 * Set the name of the table the query is executed on
		 * 
		 * @param tablename
		 *            name of the table
		 */
		public void setTablename(String tablename) {
			this.tablename = tablename;
		}

		/**
		 * @return offset of the result set
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * Set the offset of the resulting set
		 * 
		 * @param offset
		 *            to be set. Must be larger or equal zero, otherwise the
		 *            query will fail
		 */
		public void setOffset(int offset) {
			this.offset = offset;
		}

		/**
		 * @return the maximum number of elements the query returns
		 */
		public int getLimit() {
			return limit;
		}

		/**
		 * Set the maximum number of results the query returns
		 * 
		 * @param limit
		 *            to be set
		 */
		public void setLimit(int limit) {
			this.limit = limit;
		}

		/**
		 * @return Selection arguments for SELECT queries
		 */
		public String getSelectionArguments() {
			if (selectionArguments == null || selectionArguments.isEmpty())
				return "*";
			else {
				final StringBuffer buffer = new StringBuffer();
				boolean first = true;

				for (final String arg : selectionArguments) {
					if (first)
						first = false;
					else
						buffer.append(',');
					buffer.append(arg);
				}

				return buffer.toString();
			}
		}

		/**
		 * Clear all selection arguments and adds the given argument to the
		 * list. If empty or null, then "*" is added
		 * 
		 * @param arg
		 *            to be set as selection argument
		 */
		public void setSelectionArgument(String arg) {
			if (arg == null || arg.isEmpty())
				arg = "*";
			this.selectionArguments.clear();
			this.selectionArguments.add(arg);
		}

		/**
		 * Removes all selection arguments
		 */
		public void clearSelectionArguments() {
			this.selectionArguments.clear();
		}

		/**
		 * Adds the given selection argument for SELECT queries. If null or
		 * empty it is ignred
		 * 
		 * @param arg
		 *            to be added
		 */
		public void addSelectionArgument(final String arg) {
			if (arg == null || arg.isEmpty())
				return;
			this.selectionArguments.add(arg);
		}

		/**
		 * @return mergerd where clause, if set
		 */
		public String getWhereClause() {
			if (whereClauses.isEmpty())
				return "";
			final StringBuffer whereClause = new StringBuffer();
			boolean first = true;
			for (final String clause : whereClauses) {
				if (first)
					first = false;
				else
					whereClause.append(" AND ");
				whereClause.append(clause);
			}
			return whereClause.toString();
		}

		/**
		 * Set the where clause of the statement
		 * 
		 * @param whereClause
		 *            to be set in SQL
		 */
		public void setWhereClause(String whereClause) {
			if (whereClause == null)
				whereClause = "";
			this.whereClauses.clear();
			this.whereClauses.add(whereClause);
		}

		public void addWhereClause(final String whereClause) {
			if (whereClause == null || whereClause.isEmpty())
				return;
			this.whereClauses.add(whereClause);
		}

		/**
		 * Clean query's result set
		 * 
		 * @throws SQLException
		 *             If thrown while cleaning up
		 */
		public synchronized void cleanup() throws SQLException {
			if (rs != null)
				rs.close();
			rs = null;
		}

		/**
		 * Execute the given query
		 * 
		 * @param sql
		 *            to be executed
		 * @return Resulting {@link ResultSet} instance of the query
		 * @throws SQLException
		 *             If thrown while the query is executed
		 */
		public synchronized ResultSet executeQuery(final String sql)
				throws SQLException {
			cleanup();
			this.query = sql;
			this.rs = stmt.executeQuery(sql);
			return this.rs;
		}

		/**
		 * Executes a select query with the parameters of this instance
		 * 
		 * @return Resulting {@link ResultSet} if executed successfully
		 * @throws SQLException
		 *             If the query fails to execute
		 */
		protected synchronized ResultSet selectQuery() throws SQLException {
			// Generate SELECT query
			final StringBuffer query = new StringBuffer();
			query.append("SELECT " + getSelectionArguments() + " FROM ");
			query.append(tablename);
			if (whereClauses.size() > 0) {
				query.append(" WHERE ");
				query.append(getWhereClause());
			}
			if (order != null && order.length() > 0) {
				query.append(" ORDER BY ");
				query.append(sqlSafeString(order));
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

		/**
		 * @return the {@link ResultSet} of the query or null, if not available
		 */
		public synchronized ResultSet getResultSet() {
			return this.rs;
		}

		/**
		 * Ends the transaction and closes the query. This call must be always
		 * called otherwise a <b>memory leak</b> is possible
		 */
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

		/**
		 * Select next row in the resultset
		 * 
		 * @return true if there is a next row, otherwise false
		 * @throws SQLException
		 *             If occurring while reading from the {@link ResultSet}
		 */
		public synchronized boolean next() throws SQLException {
			if (rs == null)
				return false;
			return rs.next();
		}

		/**
		 * Execute the given statement
		 * 
		 * @param sql
		 *            SQL statement to be executed
		 * @return true if the statement executes successfully
		 * @throws SQLException
		 *             If the execution fails
		 */
		public synchronized boolean execute(String sql) throws SQLException {
			cleanup();
			this.query = sql;
			return stmt.execute(sql);
		}

		/**
		 * Execute the given <B<UPDATE or DELETE</b> statement
		 * 
		 * @param sql
		 *            SQL statement to be executed
		 * @return number of affected rows
		 * @throws SQLException
		 *             If the execution fails
		 */
		public synchronized int executeUpdate(String sql) throws SQLException {
			cleanup();
			this.query = sql;
			return stmt.executeUpdate(sql);
		}

		/**
		 * Return the meta-data of the {@link ResultSet}
		 * 
		 * @return {@link ResultSetMetaData} of the query
		 * @throws SQLException
		 *             If thrown while querying
		 */
		public synchronized ResultSetMetaData getResultSetMetaData()
				throws SQLException {
			if (rs == null)
				throw new IllegalStateException(
						"Cannot get ResultSetMetData when no ResultSet is available");
			return rs.getMetaData();
		}

		/**
		 * Inserts the given set of values into the given table
		 * 
		 * @param tablename
		 *            Name of the table the values are inserted
		 * @param values
		 *            {@link Map} with the <b>name</b> (Key) and <b>value</b>
		 *            (Value) for the insert statement
		 * @return number of affected rows of the statement. Should be one if
		 *         the insert is successfull
		 * @throws SQLException
		 *             If the insert statement fails
		 */
		public synchronized int insert(String tablename,
				Map<String, String> values) throws SQLException {
			if (tablename.isEmpty() || values.isEmpty())
				return 0;

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
					valuesString.append('\'');
					valuesString.append(values.get(key));
					valuesString.append('\'');
				}
				buffer.append(") VALUES (");
				buffer.append(valuesString.toString());
			}
			buffer.append(");");

			return this.executeUpdate(buffer.toString());
		}

		/**
		 * Enclose this statement in a transaction
		 * 
		 * @throws SQLException
		 *             Thrown if the Query fails to start a transaction
		 */
		public synchronized void startTransaction() throws SQLException {
			if (transaction)
				return;
			else {
				transaction = true;
				this.execute("START TRANSACTION;");
			}
		}

		/**
		 * Ends a started transaction
		 * 
		 * @throws SQLException
		 *             Thrown if the transaction fails
		 */
		public synchronized void endTransaction() throws SQLException {
			if (!transaction)
				return;
			else {
				this.execute("COMMIT;");
				transaction = false;
			}
		}
	}

	/**
	 * Instanciate a new MySQL connection
	 * 
	 * @param hostname
	 *            Remote host
	 * @param port
	 *            port
	 * @param username
	 *            Username for login
	 * @param password
	 *            Password for login
	 * @param database
	 *            Database to connect
	 * @throws SQLException
	 *             Thrown if the connection failed
	 */
	public MySQL(final String hostname, int port, final String username,
			final String password, final String database) throws SQLException {
		this.db_hostname = hostname;
		this.db_port = port;
		this.db_username = username;
		this.db_password = password;
		this.db_database = database;
		this.reconnectThrowsException();
	}

	protected void execSql(final String sql) throws SQLException {
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
	protected void execSql(final String[] sqls) throws SQLException {
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
	protected void execSql(final String[] sqls, final boolean transaction)
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

	protected void executeSql(final String sql) throws SQLException {
		execSql(sql);
	}

	protected void executeUpdate(final String sql) throws SQLException {
		final Statement stmt = createStatement();
		try {
			stmt.executeUpdate(sql);
		} finally {
			stmt.close();
		}
	}

	/**
	 * Reconnect database. Usefull, if you changed the database connection
	 * parameters, i.e. hostname, port, database, username and password
	 */
	public synchronized void reconnect() {
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
			// Consider as broken connection
		}

		final String url;
		{
			String url_ = "jdbc:mysql://" + db_hostname + ":" + db_port + "/"
					+ db_database + "?user=" + db_username
					+ "&zeroDateTimeBehavior=convertToNull";
			if (db_password != null && db_password.length() > 0)
				url_ += "&password=" + db_password;
			url = url_;
		}

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
	protected static String sqlSafeString(final String string) {
		return string.replace("'", "\\'");
	}

	/**
	 * Make a string SQL-Safe, i.e. replace all <i>'</i> with <i>\'</i> and
	 * enclose it with '. Multiple calls do not alter the string
	 * 
	 * @param string
	 *            to be handled
	 * @return SQL-Safe string
	 */
	protected static String escapeString(final String string) {
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
	protected synchronized Statement createStatement() throws SQLException {
		checkConnection();
		if (conn == null)
			throw new SQLException("Error setting up SQL connection");
		return conn.createStatement();

	}

	/**
	 * Checks the connection and creates a new preapared statement.
	 * 
	 * @return {@link Statement} for the connection
	 * @throws SQLException
	 *             Packet {@link SQLException} if occurring
	 */
	protected PreparedStatement createPreparedStatement(final String sql)
			throws SQLException {
		checkConnection();
		if (conn == null)
			throw new SQLException("Error setting up SQL connection");
		return conn.prepareStatement(sql);
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

		final String query = "SELECT version();";
		final java.sql.Statement stmt = createStatement();

		try {

			stmt.executeQuery(query);

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

	static Date getSqlDate(final ResultSet rs, final String columnName)
			throws SQLException {
		final Timestamp timestamp = rs.getTimestamp(columnName);
		if (timestamp == null)
			return new Date();
		else
			return timestamp;
	}

	Query createQuery() throws SQLException {
		return new Query();
	}
}
