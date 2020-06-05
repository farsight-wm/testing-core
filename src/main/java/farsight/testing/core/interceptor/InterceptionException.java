package farsight.testing.core.interceptor;

public class InterceptionException extends RuntimeException {

	private static final long serialVersionUID = 7150470949910515710L;

	public InterceptionException(String message) {
		super(message);
	}

	public InterceptionException(Throwable cause) {
		super(cause);
	}

	public InterceptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
