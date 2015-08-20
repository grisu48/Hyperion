package org.feldspaten.hyperion.server;

/**
 * Exception that is thrown, if a required requires a valid login to proceed
 * 
 */
public class LoginRequiredException extends IllegalAccessException {

    /** Serialisation id */
    private static final long serialVersionUID = -7225330138830621875L;

    public LoginRequiredException() {
	super();
    }

    public LoginRequiredException(String message) {
	super(message);
    }

}
