package farsight.testing.core.interceptor.mock.jexl;

import java.util.Map;

import com.wm.data.IData;

import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.utils.jexl.Jexl;

public class JexlResponseInterceptor extends BaseInterceptor {

	public static final String MAP_RESPONSE = "response";
	public static final String MAP_JEXL_SCRIPT = "jexlScript";
	
	private static final String JEXL_RESPONSE_PREFIX = "JexlResponse:";
	
	private String jexlScript;


	public JexlResponseInterceptor(String jexlScript) {
		super(JEXL_RESPONSE_PREFIX);
		this.jexlScript = jexlScript;
		
		//TODO add resources
	}


	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData pipeline) {
		invokeCount++;
		
		Jexl.executeScript(jexlScript, pipeline, null);
		
		//TODO run Jexl with input
		return InterceptResult.TRUE;
	}

	@Override
	public String toString() {
		return "JexlResponseInterceptor";
	}

	@Override
	protected void addMap(Map<String, Object> am) {
		am.put(MAP_TYPE, "JexlResponseInterceptor");
		am.put(MAP_JEXL_SCRIPT, jexlScript);
		//TODO add additional resources
	}

}
