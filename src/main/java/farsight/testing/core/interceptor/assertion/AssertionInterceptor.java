package farsight.testing.core.interceptor.assertion;

import java.util.HashMap;
import java.util.Map;

import com.wm.data.IData;

import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.interceptor.Interceptor;

/**
 * Default assertion. Counts the invokes and registers if one
 *         has asserted
 */
public class AssertionInterceptor implements Interceptor, Assertable {

	private final String assertionName;
	private int invokeCount = 0;
	private boolean asserted;

	public AssertionInterceptor(String assertionName) {
		this.assertionName = assertionName;
	}

	public boolean performAssert(IData idata) {
		return true;
	}

	public void reset() {
		invokeCount = 0;
		asserted = false;
	}

	@Override
	public final InterceptResult intercept(FlowPosition flowPosition, IData idata) {
		invokeCount++;
		if (performAssert(idata)) {
			asserted = true;
		}
		// Changed to TRUE since otherwise it is not possible to use asserts as traps.
		// If someone want's the original service to be executed one should use interceptPoint BEFORE!
		return InterceptResult.TRUE;
	}

	@Override
	public int getInvokeCount() {
		return invokeCount;
	}

	public boolean hasAsserted() {
		return asserted;
	}

	@Override
	public String getName() {
		return assertionName;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> am = new HashMap<>();
		am.put("assertionName", assertionName);
		am.put("invokeCount", invokeCount);
		am.put("asserted", Boolean.toString(asserted));
		return am;
	}
}
