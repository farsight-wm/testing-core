
package farsight.testing.core.matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wm.app.b2b.server.InvokeState;
import com.wm.lang.ns.NSService;

import farsight.testing.core.interceptor.FlowPosition;

public class CallingServicePositionMatcher implements FlowPositionMatcher {

	private final String serviceName;
	private final MatchResult matchTrue;
	private final String serviceNamespacePrefix;
	private final String id;

	private static final Logger logger = LogManager.getLogger(CallingServicePositionMatcher.class);

	public CallingServicePositionMatcher(String id, String serviceName, String serviceNamespacePrefix) {
		this.serviceName = serviceName;
		this.id = id;
		matchTrue = new MatchResult(true, id);
		this.serviceNamespacePrefix = serviceNamespacePrefix;
	}

	@Override
	public MatchResult match(FlowPosition obj) {
		if (obj == null || !serviceName.equals(obj.toString()))
			return MatchResult.FALSE;
		
		@SuppressWarnings("unchecked")
		Stack<NSService> callStack = InvokeState.getCurrentState().getCallStack();
		if (serviceNamespacePrefix == null || isCalledWithin(callStack)) {
			return matchTrue;
		}
		return MatchResult.FALSE;
	}

	private boolean isCalledWithin(Stack<NSService> callStack) {
		for (int i = callStack.size() - 2; i >-1; i--) {
			NSService nss = callStack.get(i);
			if (logger.isDebugEnabled()) {
				logger.debug("Call stack service " + nss.getNSName().getFullName() + " startsWith " + serviceNamespacePrefix +" = "+ nss.getNSName().getFullName().startsWith(serviceNamespacePrefix));
			}
			if (nss.getPackage().getName().equals(serviceNamespacePrefix) ||
					nss.getNSName().getFullName().startsWith(serviceNamespacePrefix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "CallingServicePositionMatcher["+serviceName+"]";
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> am = new HashMap<>();
		am.put("id", id);
		am.put("type", "CallingServicePositionMatcher");
		am.put("serviceName", serviceName);
		return am;
	}
}
