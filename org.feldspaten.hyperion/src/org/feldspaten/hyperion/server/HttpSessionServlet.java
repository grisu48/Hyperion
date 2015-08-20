package org.feldspaten.hyperion.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.feldspaten.hyperion.html.Page;

/**
 * Extension of {@link HttpServlet} that includes support for {@link Session}
 * instances
 * 
 * If a {@link Servlet} needs {@link Session} support it should include this
 * class instance instead of the pure {@link Servlet} class.
 * 
 * @author phoenix
 * 
 */
public abstract class HttpSessionServlet extends HttpServlet {

	/** Session cookie name */
	private static final String SESSION_COOKIE = "SESSION.COOKIE";

	/** Title of the Page */
	private static final String TITLE = "Hyperion";

	/** Static guest session */
	public final Session staticGuestSession = new Session(this, "");

	private static final int REQUEST_GET = 0x0;
	private static final int REQUEST_POST = 0x1;
	private static final int REQUEST_HEAD = 0x2;
	private static final int REQUEST_DELETE = 0x3;
	private static final int REQUEST_PUT = 0x4;
	private static final int REQUEST_OPTIONS = 0x5;
	private static final int REQUEST_TRACE = 0x6;

	/**
	 * These Strings are accepted as TRUE values when matching to a boolean
	 */
	private static final String[] TRUE_VALUES = new String[] { "1", "true",
			"on", "yes" };
	/**
	 * These Strings are accepted as FALSE values when matching to a boolean
	 */
	private static final String[] FALSE_VALUES = new String[] { "0", "false",
			"off", "no" };

	/**
	 * Request object containing all relevant data
	 * 
	 */
	public class Request implements Closeable {

		/** Request's response */
		final HttpServletResponse response;
		/** Request */
		final HttpServletRequest request;
		/** Session assigned to this request */
		final Session session;

		/**
		 * Whitch request type is this (GET/POST/HEAD/DELETE/PUT/OPTIONS/TRACE)
		 */
		private int requestType;

		/**
		 * Information sink - Either the {@link OutputStream} or a
		 * {@link PrintWriter}
		 */
		private OutputStream outputStream = null;
		/**
		 * Information sink - Either the {@link OutputStream} or a
		 * {@link PrintWriter}
		 */
		private PrintWriter writer = null;

		/** Indicating if the page has been initialized */
		// private boolean pageInitialzed = false;
		/** If page is finalized */
		// private boolean pageFinalized = false;

		private final boolean mobileVersion;

		Request(HttpServletResponse response, HttpServletRequest request,
				Session session) {
			super();
			this.response = response;
			this.request = request;
			this.session = session;
			this.mobileVersion = checkifMobileVersion();
		}

		public int getRequestType() {
			return requestType;
		}

		void setRequestType(int type) {
			this.requestType = type;
		}

		public boolean isPostRequest() {
			return requestType == REQUEST_POST;
		}

		public boolean isGetRequest() {
			return requestType == REQUEST_GET;
		}

		public HttpServletResponse getResponse() {
			return response;
		}

		public HttpServletRequest getRequest() {
			return request;
		}

		/**
		 * @see ServletRequest#getLocale()
		 * @return preferred {@link Locale} based on the Accept-Language header
		 */
		public Locale getLocale() {
			return request.getLocale();
		}

		public Session getSession() {
			if (session == null)
				return staticGuestSession;
			return session;
		}

		/**
		 * Get the remote host of a request
		 * 
		 * @param request
		 * @return
		 */
		public String getRemoteHost() {
			if (request == null)
				return "0.0.0.0";
			return request.getRemoteHost();
		}

		public synchronized PrintWriter getWriter() throws IOException {
			if (writer == null)
				writer = response.getWriter();
			return writer;
		}

		public synchronized OutputStream getOutputStream() throws IOException {
			if (outputStream == null)
				outputStream = response.getOutputStream();
			return outputStream;
		}

		/**
		 * Get a parameter out of a {@link HttpServletRequest}. If the given
		 * parameter is null or empty a default value is returned
		 * 
		 * @param name
		 *            Name of the parameter to fetch
		 * @param request
		 *            Source request
		 * @param defaultValue
		 *            Value, if the given parameter is null or empty
		 * @return parameter or default value, if the parameter is null or empty
		 */
		public String getParameter(final String name, final String defaultValue) {
			final String value = request.getParameter(name);
			if (value == null || value.isEmpty())
				return defaultValue;
			return value;
		}

		/**
		 * Get a parameter out of a {@link HttpServletRequest}. If the given
		 * parameter is null or empty, the given default value is returned. If
		 * the parameter doesn't match a double, the defaultValue is returned
		 * 
		 * @param name
		 *            Name of the parameter to fetch
		 * @param request
		 *            Source request
		 * @param defaultValue
		 *            Default value to return, if the parameter it null, empty
		 *            or invalid
		 * @return parameter or default value, if the parameter is null or empty
		 */
		public double getParameterDouble(final String name,
				final double defaultValue) {
			try {
				return Double.parseDouble(getParameter(name));
			} catch (NumberFormatException e) {
				return defaultValue;
			} catch (IllegalArgumentException e) {
				return defaultValue;
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}

		/**
		 * Get a parameter out of a {@link HttpServletRequest}. If the given
		 * parameter is null or empty, the given default value is returned. If
		 * the parameter doesn't match a integer value, the defaultValue is
		 * returned
		 * 
		 * @param name
		 *            Name of the parameter to fetch
		 * @param request
		 *            Source request
		 * @param defaultValue
		 *            Default value to return, if the parameter it null, empty
		 *            or invalid
		 * @return parameter or default value, if the parameter is null or empty
		 */
		public int getParameterInteger(final String name, final int defaultValue) {
			try {
				return Integer.parseInt(getParameter(name));
			} catch (NumberFormatException e) {
				return defaultValue;
			} catch (IllegalArgumentException e) {
				return defaultValue;
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}

		/**
		 * Get a parameter out of a {@link HttpServletRequest}. If the given
		 * parameter is null or empty, the given default value is returned. If
		 * the parameter doesn't match a long value, the defaultValue is
		 * returned
		 * 
		 * @param name
		 *            Name of the parameter to fetch
		 * @param request
		 *            Source request
		 * @param defaultValue
		 *            Default value to return, if the parameter it null, empty
		 *            or invalid
		 * @return parameter or default value, if the parameter is null or empty
		 */
		public long getParameterLong(String name, long defaultValue) {
			try {
				return Long.parseLong(getParameter(name));
			} catch (NumberFormatException e) {
				return defaultValue;
			} catch (IllegalArgumentException e) {
				return defaultValue;
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}

		/**
		 * Get a parameter out of a {@link HttpServletRequest}. If the given
		 * parameter is null or empty, the given default value is returned. If
		 * the parameter doesn't match a boolean, the defaultValue is returned
		 * 
		 * @param name
		 *            Name of the parameter to fetch
		 * @param request
		 *            Source request
		 * @param defaultValue
		 *            Default value to return, if the parameter it null, empty
		 *            or invalid
		 * @return parameter or default value, if the parameter is null or empty
		 */
		public boolean getParameterBoolean(String name, boolean defaultValue) {
			final String value = getParameter(name,
					defaultValue ? TRUE_VALUES[0] : FALSE_VALUES[0]);

			for (final String check : TRUE_VALUES)
				if (value.equalsIgnoreCase(check))
					return true;
			for (final String check : FALSE_VALUES)
				if (value.equalsIgnoreCase(check))
					return false;

			return defaultValue;

		}

		/**
		 * Get a parameter out of a {@link HttpServletRequest}. If the given
		 * parameter is null or empty, null is returned
		 * 
		 * @param name
		 *            Name of the parameter to fetch
		 * @param request
		 *            Source request
		 * @return parameter or default value, if the parameter is null or empty
		 */
		public String getParameter(final String name) {
			return getParameter(name, null);
		}

		@Override
		public void close() throws IOException {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
		}

		/**
		 * @return true if the session is logged in
		 */
		public boolean isLoggedIn() {
			if (session == null)
				return false;
			return session.isLoggedIn();
		}

		/**
		 * @return true if using secure channel
		 */
		public boolean isSecure() {
			return request.isSecure();
		}

		/**
		 * @return true if mobile version
		 */
		public boolean isMobileVersion() {
			return mobileVersion;
		}

		/**
		 * Checks the User-Agent header field, if matching to a known mobile
		 * browser
		 * 
		 * @return true if accessing from a mobile browser
		 */
		public boolean checkifMobileVersion() {
			final String userAgent = request.getHeader("User-Agent");
			if (userAgent == null || userAgent.isEmpty())
				return false;

			final String mobileBrowsersRegEx = "(android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino";
			if (userAgent.matches(mobileBrowsersRegEx))
				return true;

			try {
				if (userAgent.startsWith("Mozilla/")) {
					String mozillaAgent = userAgent.substring(userAgent
							.indexOf('(') + 1);
					mozillaAgent = mozillaAgent.substring(0,
							mozillaAgent.indexOf(')'));
					String[] agents = mozillaAgent.split(";");
					for (String agent : agents) {
						agent = agent.trim().toLowerCase();
						if (agent.startsWith("android"))
							return true;
					}
				}
			} catch (IndexOutOfBoundsException e) {
				// Just continue. Best efford strategy
			}

			// All matches have failed. Assume not to be mobile version
			return false;
		}

		/**
		 * @param includeParams
		 *            true if also parameter should be included
		 * @return Gets the URI of the request
		 */
		public String getRequestURI(boolean includeParams) {
			// Parse full URI
			String requestURI = request.getRequestURI();
			if (includeParams) {
				boolean hasParameters = false;
				final Enumeration<String> paramNames = request
						.getParameterNames();
				while (paramNames.hasMoreElements()) {
					final String name = paramNames.nextElement();
					if (name.equals("lang"))
						continue;
					if (hasParameters)
						requestURI += "&" + name + "="
								+ request.getParameter(name);
					else {
						requestURI += "?" + name + "="
								+ request.getParameter(name);
						hasParameters = true;
					}

				}
			}
			return requestURI;
		}

		/**
		 * Prepares the request for the download of a new item
		 */
		public void prepareDownload() {
			prepareDownload(null, 0);
		}

		/**
		 * Prepares the request for the download of a new item
		 * 
		 * @param filename
		 *            Filename for the following download
		 */
		public void prepareDownload(final String filename) {
			prepareDownload(filename, 0);
		}

		/**
		 * Prepares the request for the download of a new item
		 * 
		 * @param filename
		 *            Filename for the following download
		 * @param contentLength
		 *            Length of the following stream
		 */
		public void prepareDownload(final String filename,
				final int contentLength) {
			response.setHeader("Content-Encoding", "UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/plain; charset=UTF-8");
			if (filename == null || filename.isEmpty())
				response.setHeader("Content-Disposition", "attachment");
			else
				response.setHeader("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");
			if (contentLength > 0)
				response.setContentLength(contentLength);
		}

		/**
		 * Check if the given parameter exists
		 * 
		 * @param name
		 *            Name of the parameter to check
		 * @return true if set (also if empty) false if not existing
		 */
		public boolean hasParameter(final String name) {
			return request.getParameter(name) != null;
		}

		public void printErrorPage(final String message) throws IOException {
			printErrorPage(message, 500);
		}

		public void printErrorPage(String message, int statusCode)
				throws IOException {
			response.setStatus(statusCode);

			final Page page = initPage();
			page.addHeadline("Error", 2);
			page.addParagraph(message.replace("\n", "<br>"));
			page.print(getWriter());
		}

		/**
		 * Initialize the parent page for a request
		 * 
		 * @return initialized parent page
		 */
		protected Page initPage() {
			return initPage(TITLE);
		}

		/**
		 * Initialize the parent page for a request
		 * 
		 * @param title
		 *            Title of the page
		 * @return initialized parent page
		 */
		protected Page initPage(final String title) {
			final Page page = new Page();
			return page;
		}
	}

	/** Serialisation ID */
	private static final long serialVersionUID = 4112688656845792649L;

	/** Stored sessions */
	private static HashMap<String, Session> sessions = new HashMap<String, Session>();

	/**
	 * Date formatter used for all dates<br>
	 * Display format: <b>yyyy-dd-MM</b>
	 */
	protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-dd-MM");
	/**
	 * Date formatter used for all dates and times<br>
	 * 
	 * Display format: <b>yyyy-dd-MM HH:mm:ss</b>
	 */
	protected static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(
			"yyyy-dd-MM HH:mm:ss");
	/**
	 * Date formatter used for all times<br>
	 * Display format: <b>HH:mm:ss</b>
	 */
	protected static final SimpleDateFormat timeFormatter = new SimpleDateFormat(
			"HH:mm:ss");

	/** Indicating if the servlet required a valid login */
	private boolean requireValidLogin = true;

	public HttpSessionServlet() {
		this(true);
	}

	public HttpSessionServlet(boolean requireValidLogin) {
		this.requireValidLogin = requireValidLogin;
	}

	/**
	 * Get the given pathname (inside the WAR-archive) as {@link InputStream}
	 * 
	 * @param pathname
	 *            Pathname to open
	 * @return {@link InputStream} for the given resource
	 */
	public InputStream getResourceAsStream(final String pathname) {
		return getServletContext().getResourceAsStream(pathname);
	}

	public void setLoginRequired(final boolean enabled) {
		this.requireValidLogin = enabled;
	}

	/**
	 * Gets the session for this servlet object
	 * 
	 * @return the session for this servlet object
	 * @throws IOException
	 *             Thrown from database
	 */
	protected synchronized final Session getSession(
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {

		String sid = null;
		final Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (final Cookie cookie : cookies) {
				if (cookie.getName().equals(SESSION_COOKIE)) {
					sid = cookie.getValue();
					break;
				}
			}

		synchronized (sessions) {
			removeDeadSessions();
			// No session cookie - Create one
			if (sid == null) {
				do {
					sid = createNewSecureSID();
				} while (sessions.containsKey(sid));

				response.addCookie(new Cookie(SESSION_COOKIE, sid));
				final Session session = new Session(this, sid);
				sessions.put(sid, session);
			}

			if (sid == null || sid.isEmpty())
				return staticGuestSession;

			removeDeadSessions();

			Session session = sessions.get(sid);
			if (session == null) {
				// This is a new session. Create it!
				session = new Session(this, sid);
				sessions.put(sid, session);
				// saveSessions();
				return session;

			} else {
				session.doActivity();
				return session;
			}
		}

	}

	/**
	 * Creates new secure SID
	 * 
	 * @return
	 */
	private static String createNewSecureSID() {
		final int size = 1024;

		// Initialize new Random generator each time to increase entropy
		final Random rnd = new Random(System.currentTimeMillis());
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < size; i++) {
			char c = 'a';
			if (rnd.nextBoolean()) {
				c = (char) ('0' + rnd.nextInt(10));
			} else {
				if (rnd.nextBoolean())
					c = 'A';
				c = (char) (c + rnd.nextInt(26));
			}

			buffer.append(c);

		}

		return buffer.toString();
	}

	/**
	 * Removes all dead sessions
	 */
	private static void removeDeadSessions() {
		removeDeadSessions(true);
	}

	/**
	 * Removes all dead sessions
	 * 
	 * @param saveSessions
	 *            true, if when removing a session, the session file is
	 *            refreshed
	 */
	private static void removeDeadSessions(boolean saveSessions) {
		synchronized (sessions) {
			final List<Session> deadSessions = new LinkedList<Session>();
			for (final Session session : sessions.values()) {
				if (session.isExpired())
					deadSessions.add(session);
			}
			for (final Session session : deadSessions) {
				sessions.remove(session.getId());
			}

			if (saveSessions && !deadSessions.isEmpty()) {
				// Write sessions to file
				// saveSessions();
			}
		}
	}

	/**
	 * Checks the request. This method is performed before each request is
	 * redirected to the corresponding subclass.
	 * 
	 * <b>Note</b> Inherit this class, if you need additional checks to be
	 * performed.
	 * 
	 * <b>Important</b>Returns false, if the request is denied. In this case the
	 * method is also responsible for printing a corresponding error message
	 * 
	 * @param request
	 *            to be checked
	 * @throws IOException
	 *             Will be redirected to request
	 * @throws SQLException
	 *             Will be redirected to request
	 * @return true if the login check is positive, false is the session must
	 *         terminate
	 * @throws LoginRequiredException
	 *             Thrown if the request is not logged in but requires a valid
	 *             login
	 */
	protected boolean checkRequest(final Request request) throws IOException,
			SQLException, LoginRequiredException {
		if (request.isLoggedIn())
			return true;

		if (requireValidLogin) {
			throw new LoginRequiredException();
		} else
			return true;
	}

	/**
	 * Report a database exception that has not yet been caught
	 * 
	 * @param cause
	 *            Exception that should be reported
	 */
	protected void reportDatabaseException(final Throwable cause) {
		cause.printStackTrace(System.err);
	}

	/**
	 * Checks the username and password for a valid login prior to process the
	 * page
	 * 
	 * @param username
	 *            to be checked
	 * @param password
	 *            to be checked
	 * @return true if the credentials are valid, false if not
	 * @throws SQLException
	 *             Thrown if a database error occurrs
	 */
	protected abstract boolean checkLogin(final String username,
			final String password) throws SQLException;

	protected User createUser(final String username) throws SQLException {
		return new User(username);
	}

	protected final void processRequest(final int method,
			final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final Session session = this.getSession(req, resp);
		if (session.isExpired())
			session.logout();
		session.doActivity();
		final Request request = new Request(resp, req, session);
		request.setRequestType(method);
		request.session.setRemoteAddress(req.getRemoteAddr());

		// If not logged in, check if the request provides login informations
		if (!request.session.isLoggedIn()) {
			try {
				if (request.hasParameter("username")
						&& request.hasParameter("password")) {
					final String username = request.getParameter("username");
					final String password = request.getParameter("password");
					if (this.checkLogin(username, password)) {
						final User user = createUser(username);
						if (user != null)
							request.session.setUser(user);
					}
				}
			} catch (SQLException e) {
				// Report error, but continue without info
				reportDatabaseException(e);
			}
		}

		// Handle request
		try {
			if (!checkRequest(request))
				throw new IllegalAccessException();

			final Page page = request.initPage();

			if (method == REQUEST_GET)
				this.doGet(request, page);
			else if (method == REQUEST_POST)
				this.doPost(request, page);
			else if (method == REQUEST_PUT)
				this.doPut(request, page);
			else if (method == REQUEST_TRACE)
				this.doTrace(request, page);
			else if (method == REQUEST_HEAD)
				this.doHead(request, page);
			else if (method == REQUEST_OPTIONS)
				this.doOptions(request, page);
			else if (method == REQUEST_DELETE)
				this.doDelete(request, page);

			if (page.isEnabled()) {
				// final int statusCode = page.getStatusCode();
				// request.response.setStatus(statusCode);
				page.print(request.getWriter());
			}
		} catch (IllegalArgumentException e) {
			request.printErrorPage("Illegal request (Illegal argument)");
			onRequestError(request, e);

		} catch (SQLException e) {
			request.printErrorPage("Database error (" + e.getErrorCode() + ")");
			reportDatabaseException(e);
			onRequestError(request, e);

		} catch (LoginRequiredException e) {
			printLoginRequiredPage(request, request.getRequestURI(true));

		} catch (IllegalAccessException e) {
			request.printErrorPage("Access denied");
			onRequestError(request, e);
		} finally {
			request.close();
		}
	}

	/**
	 * Is called whenever a request caused an error
	 * 
	 * @param request
	 *            that caused the error
	 * @param error
	 *            error that has been raised
	 */
	protected void onRequestError(final Request request, final Throwable error) {
		// Do nothing
	}

	/**
	 * Prints the Stack trace of a given {@link Throwable} to a request
	 * 
	 * @param request
	 *            Where to write
	 * @param e
	 *            Throwable with it's stack trace
	 * @throws IOException
	 *             Thrown if occurring while writing to stream
	 */
	protected void printException(final Request request, final Throwable e)
			throws IOException {
		final PrintWriter writer = request.getWriter();
		writer.println("<h3>Stack trace</h3>");
		writer.println("<div class=\"code\">");
		e.printStackTrace(new PrintStream(new OutputStream() {

			@Override
			public void write(int character) throws IOException {
				if (character < 0)
					return;
				if (character == '\n')
					writer.println("<br>\n");
				else
					writer.print((char) character);
			}
		}));
		writer.println("</div>");
	}

	@Override
	protected final void doDelete(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		processRequest(REQUEST_DELETE, req, resp);
	}

	protected abstract void printLoginRequiredPage(final Request request,
			final String redirectURL) throws IOException;

	@Override
	protected final void doOptions(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		processRequest(REQUEST_OPTIONS, req, resp);
	}

	@Override
	protected final void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(REQUEST_HEAD, req, resp);
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(REQUEST_POST, req, resp);
	}

	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(REQUEST_PUT, req, resp);
	}

	@Override
	protected final void doTrace(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		processRequest(REQUEST_TRACE, req, resp);
	}

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(REQUEST_GET, req, resp);
	}

	/**
	 * Get request
	 * 
	 * @param request
	 *            {@link Request} instance containing all data
	 */
	protected void doGet(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doGet(request.getRequest(), request.getResponse());
	}

	protected void doDelete(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doDelete(request.getRequest(), request.getResponse());
	}

	protected void doOptions(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doOptions(request.getRequest(), request.getResponse());
	}

	protected void doHead(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doHead(request.getRequest(), request.getResponse());
	}

	protected void doPost(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doPost(request.getRequest(), request.getResponse());
	}

	protected void doPut(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doPut(request.getRequest(), request.getResponse());
	}

	protected void doTrace(Request request, final Page page)
			throws ServletException, IOException, SQLException,
			IllegalAccessException {
		super.doTrace(request.getRequest(), request.getResponse());
	}

	/**
	 * @return number of currently active sessions
	 */
	static synchronized int getSessionCount() {
		removeDeadSessions();
		return sessions.size();
	}

	/**
	 * @return all current sessions
	 */
	static synchronized List<Session> getSessions() {
		removeDeadSessions();

		final List<Session> result = new ArrayList<Session>(sessions.size());
		for (final Session session : sessions.values())
			result.add(session);
		return result;
	}

	/**
	 * Searches for a active session with the given id
	 * 
	 * @param id
	 *            of the session to be processed
	 * @return {@link Session} instance with the given session id or null, if
	 *         none found
	 */
	public static Session getSession(final String id) {
		if (id == null || id.isEmpty())
			return null;

		synchronized (sessions) {
			removeDeadSessions();
			return sessions.get(id);
		}
	}

	public static String formatSeconds(final long deltaSeconds) {
		if (deltaSeconds < 60)
			return deltaSeconds + " s";
		long minutes = deltaSeconds / 60;
		if (minutes < 60)
			return minutes + " min";
		long hours = minutes / 60;
		if (hours < 24) {
			minutes -= hours * 60;
			return hours + " h, " + minutes + " min";
		}
		long days = hours / 24;
		return days + " days";
	}

	/**
	 * Removes the given session from the list of active sessions
	 * 
	 * @param session
	 *            to be removed
	 */
	void removeSession(final Session session) {
		if (session == null)
			return;
		final String sid = session.getId();
		synchronized (sessions) {
			if (sessions.containsKey(sid))
				sessions.remove(sid);
		}
	}

}