package farsight.testing.core.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Stack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Package;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSService;

import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptPoint;

@RunWith(PowerMockRunner.class)
@PrepareForTest(InvokeState.class)
@PowerMockIgnore("javax.management.*")
public class CallingServicePositionMatcherTest {

	private static final String PUB_FOO_BAR = "pub.foo:bar";

	@Test
	public void shouldMatchCorrectNamespacePackage() {
		NSName nsnTestService = NSName.create("org.wmaop.pub:testService");
		NSName nsnBarService = NSName.create(PUB_FOO_BAR);
		
		Package pkg1 = mock(Package.class);
		when(pkg1.getName()).thenReturn("pkg1");
		Package pkg2 = mock(Package.class);
		when(pkg2.getName()).thenReturn("pkg2");

		PowerMockito.mockStatic(InvokeState.class);
		InvokeState mockInvokeState = mock(InvokeState.class);
		when(InvokeState.getCurrentState()).thenReturn(mockInvokeState);

		NSService svcTest = mock(NSService.class);
		when(svcTest.getNSName()).thenReturn(nsnTestService);
		when(svcTest.getPackage()).thenReturn(pkg1);
		
		NSService svcBar = mock(NSService.class);
		when(svcBar.getNSName()).thenReturn(nsnBarService);
		when(svcBar.getPackage()).thenReturn(pkg2);
		
		Stack<NSService> callStack = new Stack<>();
		callStack.push(svcTest);
		callStack.push(svcBar);
		when(mockInvokeState.getCallStack()).thenReturn(callStack);
		
		FlowPosition fp = new FlowPosition(InterceptPoint.INVOKE, PUB_FOO_BAR);
		
		assertFalse(new CallingServicePositionMatcher("id", PUB_FOO_BAR, null).match(null).isMatch());
		assertFalse(new CallingServicePositionMatcher("id", PUB_FOO_BAR, null).match( new FlowPosition(InterceptPoint.INVOKE, "nother:service")).isMatch());

		// Check for none or partial namespace and service match
		assertTrue(new CallingServicePositionMatcher("id", PUB_FOO_BAR, null).match(fp).isMatch());
		assertTrue(new CallingServicePositionMatcher("id", PUB_FOO_BAR, "org.wmaop").match(fp).isMatch());
		assertTrue(new CallingServicePositionMatcher("id", PUB_FOO_BAR, "org.wmaop.pub").match(fp).isMatch());
		assertTrue(new CallingServicePositionMatcher("id", PUB_FOO_BAR, "org.wmaop.pub:testService").match(fp).isMatch());
		// Match on the calling package
		assertTrue(new CallingServicePositionMatcher("id", PUB_FOO_BAR, "pkg1").match(fp).isMatch());
		
		// Package for this call which is irrelevant
		assertFalse(new CallingServicePositionMatcher("id", PUB_FOO_BAR, "pkg2").match(fp).isMatch());
		// Matching on some arbitary service
		assertFalse(new CallingServicePositionMatcher("id", PUB_FOO_BAR, "org.wmaop.pub:anotherService").match(fp).isMatch());
	
		CallingServicePositionMatcher cspm = new CallingServicePositionMatcher("id", PUB_FOO_BAR, "org.wmaop.pub:anotherService");
		cspm.toString();
		assertEquals(PUB_FOO_BAR, cspm.toMap().get("serviceName"));
		assertEquals(PUB_FOO_BAR, cspm.getServiceName());
		
	}

}
