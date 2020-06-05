package farsight.testing.core.matcher;

import farsight.testing.core.interceptor.FlowPosition;

public interface FlowPositionMatcher extends Matcher<FlowPosition> {

	String getServiceName();

}
