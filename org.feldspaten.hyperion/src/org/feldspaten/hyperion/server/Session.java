package org.feldspaten.hyperion.server;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Session implements Closeable, Serializable {

	/** Serialisation ID for writing to file */
	private static final long serialVersionUID = 8173870806961293135L;

	/** Delay for inactive sessions */
	private static final long INACTIVE_DELAY = 10L * 60L * 1000L;

	private final HttpSessionServlet parent;

	/** Remote address */
	private String remoteAddress = "";

	/** Session ID */
	private final String sid;

	/** If the session is expired */
	private boolean expired = false;

	/** Logged in user, or null, if guest */
	private User user = null;

	/** timestamp of the last activity */
	private long lastActivity = System.currentTimeMillis();

	/** Failed logins since last login (only accurate when logged in) */
	private int failedLogins = 0;
	/** Last login (only accurate when logged in) */
	private Date lastLogin = null;

	/** Internal properties */
	protected Map<String, String> properties = new HashMap<>();

	/**
	 * Create new {@link Session} instance
	 * 
	 * @param sid
	 *            Session ID, Unique identification of the session
	 */
	public Session(final HttpSessionServlet parent, final String sid) {
		this.parent = parent;
		this.sid = sid;
	}

	/**
	 * @return Remote Address of the owner client
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * Assign the remoteAddress of the owner client
	 */
	protected void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getProperty(final String key) {
		return getProperty(key, "");
	}

	public String getProperty(final String key, final String defaultValue) {
		synchronized (properties) {
			if (!properties.containsKey(key))
				return defaultValue;
			else {
				final String value = properties.get(key);
				if (value == null)
					return defaultValue;
				return value;
			}
		}
	}

	public void setProperty(final String key, final String value) {
		if (key == null || key.trim().isEmpty())
			return;
		synchronized (properties) {
			properties.put(key, value);
		}
	}

	/**
	 * @return System milliseconds of the last activity
	 */
	public long getLastActivity() {
		return lastActivity;
	}

	public int getFailedLogins() {
		return failedLogins;
	}

	public void setFailedLogins(int failedLogins) {
		this.failedLogins = failedLogins;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getId() {
		return sid;
	}

	public void doActivity() {
		lastActivity = System.currentTimeMillis();
	}

	public boolean isExpired() {
		if (expired)
			return true;
		expired = (System.currentTimeMillis()) > (lastActivity + INACTIVE_DELAY);
		return expired;
	}

	public void expire() {
		expired = true;
	}

	public boolean isLoggedIn() {
		return user != null;
	}

	/**
	 * Logged in user or null, if a guest session
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Set user of the session. This is equal to logging in a session
	 * 
	 * @param user
	 *            User to be set. If null, the session is a guest session
	 */
	public void setUser(final User user) {
		this.user = user;
	}

	/**
	 * Set the expired state of the {@link Session}
	 * 
	 * @param expired
	 *            true if the session is expired, otherwise false
	 */
	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	@Override
	public void close() {
		expired = true;
		parent.removeSession(this);
	}

	public void logout() {
		setUser(null);
	}

	public String getUsername() {
		if (user == null)
			return "";
		return user.getUsername();
	}
}
