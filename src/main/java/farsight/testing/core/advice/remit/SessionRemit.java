package farsight.testing.core.advice.remit;

import static farsight.testing.core.advice.Scope.*;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Session;

import farsight.testing.core.advice.Scope;

public class SessionRemit implements Remit {

	public static final String NO_SESSION = "NoSession";
	
	private final String associatedSessionId;
	
	public SessionRemit() {
		associatedSessionId = getSessionID();
	}
	
	public SessionRemit(String associatedSessionId) {
		this.associatedSessionId = associatedSessionId;
	}
	
	@Override
	public boolean isApplicable() {
		return getSessionID().equals(associatedSessionId);
	}

	@Override
	public boolean isApplicable(Scope scope) {
		return (scope == ALL || scope == SESSION) && isApplicable();
	}

	@Override
	public String toString() {
		return "SessionScope[" + getSessionID() + ']';
	}
	
	private String getSessionID() {
		Session session = InvokeState.getCurrentSession();
		final String id;
		if (session == null) {
			id = NO_SESSION;
		} else {
			id = session.getSessionID();
		}
		return id;
	}
}
