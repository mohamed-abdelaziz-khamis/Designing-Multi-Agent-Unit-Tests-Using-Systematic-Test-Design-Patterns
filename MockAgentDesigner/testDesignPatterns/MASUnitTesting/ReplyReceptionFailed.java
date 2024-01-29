/**
 * 
 */
package MASUnitTesting;

import junit.framework.AssertionFailedError;

/**
 * @author Mohamed Abd El Aziz
 *
 */
public class ReplyReceptionFailed extends AssertionFailedError {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public ReplyReceptionFailed(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ReplyReceptionFailed(Throwable cause) {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReplyReceptionFailed(String message, Throwable cause) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
