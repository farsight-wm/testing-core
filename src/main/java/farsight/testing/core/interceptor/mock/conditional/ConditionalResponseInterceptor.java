package farsight.testing.core.interceptor.mock.conditional;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wm.data.IData;
import com.wm.data.IDataUtil;

import farsight.testing.core.flow.IDataXmlTool;
import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.interceptor.InterceptionException;
import farsight.testing.core.matcher.MatchResult;
import farsight.testing.core.matcher.jexl.JexlIDataMatcher;

public class ConditionalResponseInterceptor extends BaseInterceptor {

	public static final String MAP_RESPONSES = "responses";
	public static final String MAP_IGNORE_NO_MATCH = "ignoreNoMatch";
	public static final String MAP_DEFAULT_ID = "defaultId";
	public static final String MAP_DEFAULT_RESPONSE = "defaultResponse";
	public static final String MAP_CONDITION = "condition";

	private static final Logger logger = LogManager.getLogger(ConditionalResponseInterceptor.class);

	private final JexlIDataMatcher evaluator;
	private final Map<String, IData> responses = new HashMap<>();
	private final IData defaultResponse;
	private final String defaultId;
	private final boolean ignoreNoMatch;

	public ConditionalResponseInterceptor(List<ConditionResponse> conditionResponses, ConditionResponse defaultResponse, boolean ignoreNoMatch) throws IOException {
		super("ConditionalResponse:");
		Map<String, String> exprs = new LinkedHashMap<>();
		this.ignoreNoMatch = ignoreNoMatch;
		for (ConditionResponse cr : conditionResponses) {
			String sid = cr.getId();
			exprs.put(sid, cr.getExpression());
			responses.put(sid, IDataXmlTool.decode(cr.getResponse()));
			logger.info("Adding response id " + sid + " length " + cr.getResponse().length() + " for expression " + cr.getExpression());
		}
		if (defaultResponse != null && defaultResponse.getResponse() != null) {
			this.defaultResponse = IDataXmlTool.decode(defaultResponse.getResponse());
			defaultId = defaultResponse.getId();
		} else {
			defaultId = null;
			this.defaultResponse = null;
		}
		evaluator = new JexlIDataMatcher(exprs);
	}

	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData idata) {
		invokeCount++;
		MatchResult result = evaluator.match(idata);
		logger.info("Evaluated " + result);
		if (result != null) {
			logger.info("Merging response " + result.getId());
			IDataUtil.merge(responses.get(result.getId()), idata);
			return InterceptResult.TRUE;
		} else if (defaultResponse != null) {
			logger.info("Merging default response " + defaultId);
			IDataUtil.merge(defaultResponse, idata);
			return InterceptResult.TRUE;
		}
		if (ignoreNoMatch) {
			return InterceptResult.TRUE;
		}
		throw new InterceptionException("No conditions match pipeline state");
	}

	@Override
	protected void addMap(Map<String, Object> am) {
		am.put(MAP_TYPE, "ConditionalResponseInterceptor");
		am.put(MAP_CONDITION, evaluator.toMap());
		am.put(MAP_DEFAULT_RESPONSE, defaultResponse);
		am.put(MAP_DEFAULT_ID, defaultId);
		am.put(MAP_IGNORE_NO_MATCH, Boolean.toString(ignoreNoMatch));
		am.put(MAP_RESPONSES, responses);
	}
}
