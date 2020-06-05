package farsight.testing.core.interceptor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PipelinePositionTest {

	@Test
	public void shouldExtractPackageName() {
		assertEquals("pub", new FlowPosition(InterceptPoint.INVOKE, "pub:foo").packageName);
		assertEquals("", new FlowPosition(InterceptPoint.INVOKE, "").packageName);
	}

	@Test
	public void shouldExtractServiceName() {
		assertEquals("foo", new FlowPosition(InterceptPoint.INVOKE, "pub:foo").serviceName);
		assertEquals("", new FlowPosition(InterceptPoint.INVOKE, "").serviceName);
		assertEquals("foo", new FlowPosition(InterceptPoint.INVOKE, "foo").serviceName);
	}
}
