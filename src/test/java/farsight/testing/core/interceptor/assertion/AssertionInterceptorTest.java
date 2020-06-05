package farsight.testing.core.interceptor.assertion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.wm.data.IData;

import farsight.testing.core.interceptor.FlowPosition;

public class AssertionInterceptorTest {

	@Test
	public void shouldExerciseAll() {
		AssertionInterceptor ai = new AssertionInterceptor("foo");
		assertEquals("foo", ai.getName());
		assertEquals(0, ai.getInvokeCount());
		assertFalse(ai.hasAsserted());
	
		ai.intercept(mock(FlowPosition.class), mock(IData.class));
		assertEquals(1, ai.getInvokeCount());
		assertTrue(ai.hasAsserted());
		assertEquals("true", ai.toMap().get("asserted"));
		
		ai.reset();
		assertEquals(0, ai.getInvokeCount());
		assertFalse(ai.hasAsserted());
		assertEquals("false", ai.toMap().get("asserted"));
	}

}
