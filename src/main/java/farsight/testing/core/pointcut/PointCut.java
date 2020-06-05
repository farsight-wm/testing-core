package farsight.testing.core.pointcut;

import com.wm.data.IData;

import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.matcher.FlowPositionMatcher;

public interface PointCut {

	boolean isApplicable(FlowPosition pipelinePosition, IData idata);

	InterceptPoint getInterceptPoint();

	FlowPositionMatcher getFlowPositionMatcher();

	Object toMap();
}
