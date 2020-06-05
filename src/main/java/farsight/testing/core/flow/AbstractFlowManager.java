package farsight.testing.core.flow;

import java.text.MessageFormat;

import org.apache.commons.lang3.ArrayUtils;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

import farsight.testing.core.advice.Advice;
import farsight.testing.core.advice.remit.Remit;
import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.interceptor.Interceptor;
import farsight.testing.core.matcher.AlwaysTrueMatcher;
import farsight.testing.core.matcher.CallingServicePositionMatcher;
import farsight.testing.core.matcher.FlowPositionMatcher;
import farsight.testing.core.matcher.FlowPositionMatcherImpl;
import farsight.testing.core.matcher.Matcher;
import farsight.testing.core.matcher.jexl.JexlIDataMatcher;
import farsight.testing.core.pointcut.PointCut;
import farsight.testing.core.pointcut.ServicePipelinePointCut;

public abstract class AbstractFlowManager {

	public void mandatory(IData pipeline, String message, String... params) throws ServiceException {
		IDataCursor pipelineCursor = pipeline.getCursor();
		try {
			for (String p : params) {
				Object o = IDataUtil.get(pipelineCursor, p);
				if (o == null || "".equals(o)) {
					MessageFormat mf = new MessageFormat(message);
					throw new ServiceException(mf.format(ArrayUtils.addAll(new String[]{p}, params)));
				}
			}
		} finally {
			pipelineCursor.destroy();
		}
	
	}

	@SafeVarargs
	public final <T> void oneof(String message, T input, T... values) throws ServiceException {
		for (T v : values) {
			if (v.equals(input)) {
				return;
			}
		}
		MessageFormat mf = new MessageFormat(message);
		throw new ServiceException(mf.format(ArrayUtils.addAll(new Object[]{input}, values)));
	}

	protected void registerInterceptor(String adviceId, Remit scope, String interceptPoint, String serviceName, String pipelineCondition, Interceptor interceptor, String calledBy) throws ServiceException {
		String interceptPointUpper = interceptPoint.toUpperCase();
		oneof("interceptPoint {0} must be either {1}, {2} or {3}", interceptPointUpper, "BEFORE", "INVOKE", "AFTER");
		InterceptPoint ip = InterceptPoint.valueOf(interceptPointUpper);
	
		FlowPositionMatcher servicePositionMatcher;
		if (calledBy != null && !calledBy.isEmpty()) {
			servicePositionMatcher = new CallingServicePositionMatcher(serviceName, serviceName, calledBy);
		} else {
			servicePositionMatcher = new FlowPositionMatcherImpl(serviceName, serviceName);
		}
		Matcher<IData> pipelineMatcher;
		if (pipelineCondition != null && pipelineCondition.length() > 0) {
			pipelineMatcher = new JexlIDataMatcher(serviceName, pipelineCondition);
		} else {
			pipelineMatcher = new AlwaysTrueMatcher<>(serviceName);
		}
		PointCut joinPoint = new ServicePipelinePointCut(servicePositionMatcher, pipelineMatcher, ip);
		Advice advice = new Advice(adviceId, scope, joinPoint, interceptor);
		FarsightChainProcessor aop = FarsightChainProcessor.getInstance();
		aop.getAdviceManager().registerAdvice(advice);
		aop.setEnabled(true);
	}
}
