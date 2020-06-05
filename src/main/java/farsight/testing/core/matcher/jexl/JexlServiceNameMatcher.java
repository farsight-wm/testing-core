package farsight.testing.core.matcher.jexl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;

import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.matcher.MatchResult;
import farsight.testing.core.matcher.Matcher;
import farsight.testing.utils.jexl.legacy.JexlExpressionFactory;

public class JexlServiceNameMatcher implements Matcher<FlowPosition> {

	private final JexlExpression expression;
	private final String sid;

	public JexlServiceNameMatcher(String sid, String expr) {
		this.sid = sid;
		expression = createExpression(sid, expr);
	}

	@Override
	public MatchResult match(FlowPosition flowPosition) {
		final JexlContext ctx = new MapContext();
		ctx.set("serviceName", flowPosition.toString());
		Object result = expression.evaluate(ctx);
		verifyExpressionResult(sid, result);
		if ((Boolean) result) {
			return new MatchResult(true, sid);
		}
		return MatchResult.FALSE;
	}

	private JexlExpression createExpression(String name, String exprText) {
		JexlExpression compiledExpr = JexlExpressionFactory.createExpression(exprText);
		Object result = compiledExpr.evaluate(new MapContext());
		verifyExpressionResult(name, result);
		return compiledExpr;
	}

	private void verifyExpressionResult(String name, Object result) {
		if (!(result instanceof Boolean)) {
			throw new JexlParseException("Cannot parse expression named '" + name + "' to get boolean, instead got "
					+ result.getClass().getSimpleName() + ": " + result);
		}
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> am = new HashMap<>();
		am.put("type", "JexlServiceNameMatcher");
		am.put("id", sid);
		am.put("expression", expression.toString());
		return am;
	}

}
