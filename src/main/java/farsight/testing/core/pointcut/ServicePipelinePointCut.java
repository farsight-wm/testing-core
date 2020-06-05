package farsight.testing.core.pointcut;

import java.util.HashMap;
import java.util.Map;

import com.wm.data.IData;

import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.matcher.FlowPositionMatcher;
import farsight.testing.core.matcher.Matcher;

public class ServicePipelinePointCut implements PointCut {

	private final FlowPositionMatcher flowPositionMatcher;
	private final Matcher<? super IData> pipelineMatcher;
	private InterceptPoint interceptPoint;

	public ServicePipelinePointCut(FlowPositionMatcher flowPositionMatcher, Matcher<? super IData> pipelineMatcher, InterceptPoint interceptPoint) {
		this.flowPositionMatcher = flowPositionMatcher;
		this.pipelineMatcher = pipelineMatcher;
		this.interceptPoint = interceptPoint;
	}

	@Override
	public boolean isApplicable(FlowPosition pipelinePosition, IData idata) {
		return flowPositionMatcher.match(pipelinePosition).isMatch() && pipelineMatcher.match(idata).isMatch();
	}

	@Override
	public FlowPositionMatcher getFlowPositionMatcher() {
		return flowPositionMatcher;
	}

	public Matcher<? super IData> getPipelineMatcher() {
		return pipelineMatcher;
	}

	@Override
	public String toString() {
		return "ServicePipelinePointCut[" + flowPositionMatcher + " & " + pipelineMatcher + ']';
	}

	@Override
	public InterceptPoint getInterceptPoint() {
		return interceptPoint;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> am = new HashMap<>();
		am.put("interceptPoint", interceptPoint.toString());
		am.put("flowPositionMatcher", flowPositionMatcher.toMap());
		am.put("pipelineMatcher", pipelineMatcher.toMap());
		return am;
	}

}
