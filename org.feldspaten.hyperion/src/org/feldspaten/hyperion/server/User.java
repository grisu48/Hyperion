package org.feldspaten.hyperion.server;

/**
 * User instance for the {@link HttpSessionServlet}
 * 
 * @author phoenix
 *
 */
public class User {
	/** Username */
	private String username;

	public User(String username) {
		super();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
