package farsight.testing.core.chainprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.util.ServerException;
import com.wm.util.coder.IDataXMLCoder;

import farsight.testing.core.advice.Advice;
import farsight.testing.core.advice.AdviceManager;
import farsight.testing.core.advice.Scope;
import farsight.testing.core.advice.remit.GlobalRemit;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.interceptor.Interceptor;
import farsight.testing.core.interceptor.assertion.AssertionInterceptor;
import farsight.testing.core.interceptor.mock.canned.CannedResponseInterceptor;
import farsight.testing.core.interceptor.mock.exception.ExceptionInterceptor;
import farsight.testing.core.matcher.AlwaysTrueMatcher;
import farsight.testing.core.matcher.FlowPositionMatcherImpl;
import farsight.testing.core.matcher.Matcher;
import farsight.testing.core.matcher.jexl.JexlIDataMatcher;
import farsight.testing.core.pointcut.PointCut;
import farsight.testing.core.pointcut.ServicePipelinePointCut;
import farsight.testing.core.stub.StubManager;

public class FarsightChainProcessorTest {

	@Test
	public void shouldExecuteConditionalMatch() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		StubManager stubManager = mock(StubManager.class);
		AdviceManager adviceManager = new AdviceManager();
		FarsightChainProcessor cp = new FarsightChainProcessor(adviceManager , stubManager );
		assertEquals(stubManager, cp.getStubManager());
		assertEquals(adviceManager, cp.getAdviceManager());
		assertEquals(cp, FarsightChainProcessor.getInstance());
		cp.setEnabled(true);

		FlowPositionMatcherImpl serviceNameMatcher = new FlowPositionMatcherImpl("my id", "pre:foo");
		Matcher<IData> pipelineMatcher = new JexlIDataMatcher("doc", "documentName == 'iso'");
		AssertionInterceptor assertion = new AssertionInterceptor("myAssertion");
		Advice assertionAdvice = new Advice("adv1", new GlobalRemit(), new ServicePipelinePointCut(serviceNameMatcher, pipelineMatcher, InterceptPoint.BEFORE), assertion);
		cp.getAdviceManager().registerAdvice(assertionAdvice);
		assertEquals(1, cp.getAdviceManager().listAdvice().size());

		CannedResponseInterceptor interceptor = new CannedResponseInterceptor(classLoader.getResourceAsStream("cannedResponse.xml"));
		Advice interceptAdvice = new Advice("adv2", new GlobalRemit(), new ServicePipelinePointCut(serviceNameMatcher, pipelineMatcher, InterceptPoint.INVOKE), interceptor);
		cp.getAdviceManager().registerAdvice(interceptAdvice);
		
		assertEquals(2, cp.getAdviceManager().listAdvice().size());

		// Pipeline mocking
		IData idata = new IDataXMLCoder().decode(classLoader.getResourceAsStream("pipeline.xml"));
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create("pre:foo"));
		ServiceStatus ss = mock(ServiceStatus.class);

		Iterator<InvokeChainProcessor> chainIterator = new ArrayList<InvokeChainProcessor>().iterator();

		// Execute
		cp.process(chainIterator, baseService, idata, ss);

		assertTrue(((AssertionInterceptor) cp.getAdviceManager().getAdvice("adv1").getInterceptor()).hasAsserted());
		assertEquals(1, assertion.getInvokeCount());
		
		cp.getAdviceManager().clearAdvice();
		assertEquals(0, cp.getAdviceManager().listAdvice().size());

	}

	@Test
	public void shouldExecuteAlwaysTrueReponse() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		FarsightChainProcessor cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));
		cp.setEnabled(true);

		FlowPositionMatcherImpl serviceNameMatcher = new FlowPositionMatcherImpl("my id", "pre:foo");
		CannedResponseInterceptor interceptor = new CannedResponseInterceptor(classLoader.getResourceAsStream("cannedResponse.xml"));
		ServicePipelinePointCut pointCut = new ServicePipelinePointCut(serviceNameMatcher, new AlwaysTrueMatcher<IData>("my id"), InterceptPoint.INVOKE);
		Advice advice = new Advice("intercept", new GlobalRemit(), pointCut, interceptor);
		cp.getAdviceManager().registerAdvice(advice);

		// Pipeline mocking
		IData idata = new IDataXMLCoder().decode(classLoader.getResourceAsStream("pipeline.xml"));
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create("pre:foo"));
		ServiceStatus ss = mock(ServiceStatus.class);

		Iterator<InvokeChainProcessor> chainIterator = new ArrayList<InvokeChainProcessor>().iterator();

		// Execute
		cp.process(chainIterator, baseService, idata, ss);
		assertTrue(new String(new IDataXMLCoder().encodeToBytes(idata)).contains("\"apple\">alpha"));
	}

	@Test
	public void shouldUnregister() {
		FarsightChainProcessor cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));

		Interceptor interceptor = mock(Interceptor.class);
		PointCut pc = new ServicePipelinePointCut(new FlowPositionMatcherImpl("foo", "bar"), new AlwaysTrueMatcher<>("foo"), InterceptPoint.INVOKE); 
		Advice mockAdviceA = new Advice("a", new GlobalRemit(), pc, interceptor);
		cp.getAdviceManager().registerAdvice(mockAdviceA);
		assertEquals(1, cp.getAdviceManager().listAdvice().size());

		Advice mockAdviceAnotherA = new Advice("a", new GlobalRemit(), pc, interceptor);
		cp.getAdviceManager().registerAdvice(mockAdviceAnotherA);
		assertEquals(1, cp.getAdviceManager().listAdvice().size());
		
		
		Advice mockAdviceB = new Advice("b", new GlobalRemit(), pc, interceptor);
		cp.getAdviceManager().registerAdvice(mockAdviceB);
		assertEquals(2, cp.getAdviceManager().listAdvice().size());

		List<Advice> advices = cp.getAdviceManager().listAdvice();
		assertEquals("a", advices.get(0).getId());
		assertEquals("b", advices.get(1).getId());

		cp.getAdviceManager().unregisterAdvice("a");
		advices = cp.getAdviceManager().listAdvice();
		assertEquals(1, advices.size());
		assertEquals("b", advices.get(0).getId());

		cp.getAdviceManager().registerAdvice(mockAdviceA);
		assertEquals(2, cp.getAdviceManager().listAdvice().size());
		
		cp.getAdviceManager().unregisterAdvice(mockAdviceA);
		advices = cp.getAdviceManager().listAdvice();
		assertEquals(1, advices.size());
		assertEquals("b", advices.get(0).getId());

		cp.getAdviceManager().unregisterAdvice("notregistered"); // Check for non-existant
		cp.getAdviceManager().unregisterAdvice((Advice)null); // Check for non-existant
	}

	@Test
	public void shouldClearAdvice() {
		FarsightChainProcessor cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));

		PointCut pc = new ServicePipelinePointCut(new FlowPositionMatcherImpl("foo", "bar"), new AlwaysTrueMatcher<>("foo"), InterceptPoint.INVOKE); 
		Advice mockAdviceA = new Advice("a", new GlobalRemit(), pc, null);
		cp.getAdviceManager().registerAdvice(mockAdviceA);

		Advice mockAdviceB = new Advice("b",new GlobalRemit(),  pc, null);
		cp.getAdviceManager().registerAdvice(mockAdviceB);

		List<Advice> advices = cp.getAdviceManager().listAdvice();
		assertEquals(2, advices.size());
		cp.getAdviceManager().clearAdvice();
		assertEquals(0, cp.getAdviceManager().listAdvice().size());

	}

	@Test
	public void shouldEnableDisable() {
		FarsightChainProcessor cp = new FarsightChainProcessor();
		assertFalse(cp.isEnabled());
		cp.setEnabled(true);
		assertTrue(cp.isEnabled());
		cp.setEnabled(false);
		assertFalse(cp.isEnabled());
	}

	@Test
	public void shouldReset() {
		StubManager stubManager = mock(StubManager.class);
		AdviceManager adviceManager = mock(AdviceManager.class);
		FarsightChainProcessor cp = new FarsightChainProcessor(adviceManager, stubManager);
		cp.reset(Scope.SESSION);
		verify(stubManager, times(0)).clearStubs();
		verify(adviceManager, times(1)).reset(Scope.SESSION);
		reset(adviceManager, stubManager);
		
		cp.reset(Scope.ALL);
		verify(stubManager, times(1)).clearStubs();
		verify(adviceManager, times(1)).reset(Scope.ALL);

	}
	
	@Test
	public void shouldSetException() throws Exception{
		ClassLoader classLoader = this.getClass().getClassLoader();
		FarsightChainProcessor cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));
		cp.setEnabled(true);

		FlowPositionMatcherImpl serviceNameMatcher = new FlowPositionMatcherImpl("my id", "pre:foo");
		Exception exception = new Exception();
		Interceptor interceptor = new ExceptionInterceptor(exception );
		ServicePipelinePointCut pointCut = new ServicePipelinePointCut(serviceNameMatcher, new AlwaysTrueMatcher<IData>("my id"), InterceptPoint.INVOKE);
		Advice advice = new Advice("intercept", new GlobalRemit(), pointCut, interceptor);
		cp.getAdviceManager().registerAdvice(advice);

		// Pipeline mocking
		IData idata = new IDataXMLCoder().decode(classLoader.getResourceAsStream("pipeline.xml"));
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create("pre:foo"));
		ServiceStatus ss = mock(ServiceStatus.class);

		Iterator<InvokeChainProcessor> chainIterator = new ArrayList<InvokeChainProcessor>().iterator();

		// Execute
		cp.process(chainIterator, baseService, idata, ss);
		verify(ss, times(1)).setException(exception);

		cp.getAdviceManager().unregisterAdvice(advice);
		pointCut = new ServicePipelinePointCut(serviceNameMatcher, new AlwaysTrueMatcher<IData>("my id"), InterceptPoint.BEFORE);
		advice = new Advice("intercept", new GlobalRemit(), pointCut, interceptor);
		cp.getAdviceManager().registerAdvice(advice);
		
		// Execute
		cp.process(chainIterator, baseService, idata, ss);
		verify(ss, times(2)).setException(exception);
		
	
	}

	@Test
	public void shouldExecuteNextChainStepWhenNotInvoked() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		FarsightChainProcessor cp = new FarsightChainProcessor();
		cp.setEnabled(true);

		// Pipeline mocking
		IData idata = new IDataXMLCoder().decode(classLoader.getResourceAsStream("pipeline.xml"));
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create("pre:foo"));
		ServiceStatus ss = mock(ServiceStatus.class);

		InvokeChainProcessor icp = mock(InvokeChainProcessor.class);
		Iterator<InvokeChainProcessor> chainIterator = Arrays.asList(icp).iterator();
		
		// Execute
		cp.process(chainIterator, baseService, idata, ss);

		verify(icp, times(1)).process(chainIterator, baseService, idata, ss);
	}
	@Test
	public void shouldExecuteNextChainStepWhenDisabled() throws Exception {
		ClassLoader classLoader = this.getClass().getClassLoader();
		FarsightChainProcessor cp = new FarsightChainProcessor();

		// Pipeline mocking
		IData idata = new IDataXMLCoder().decode(classLoader.getResourceAsStream("pipeline.xml"));
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create("pre:foo"));
		ServiceStatus ss = mock(ServiceStatus.class);

		InvokeChainProcessor icp = mock(InvokeChainProcessor.class);
		Iterator<InvokeChainProcessor> chainIterator = Arrays.asList(icp).iterator();
		
		// Execute
		cp.process(chainIterator, baseService, idata, ss);

		verify(icp, times(1)).process(chainIterator, baseService, idata, ss);
	}
	
	@Test
	public void shouldFireMultiBeforeAfterAndSingleInvoke() throws ServerException, IOException {
		FarsightChainProcessor cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("err1", InterceptPoint.INVOKE, "a == 99"));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("pre1", InterceptPoint.BEFORE, null));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("pre2", InterceptPoint.BEFORE, null));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("inv1", InterceptPoint.INVOKE, "a == 1"));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("inv2", InterceptPoint.INVOKE, "a == 2"));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("inv3", InterceptPoint.INVOKE, "a == 3"));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("post1", InterceptPoint.AFTER, null));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("err2", InterceptPoint.INVOKE, "a == 99"));
		cp.getAdviceManager().registerAdvice(getCannedAdvice("post2", InterceptPoint.AFTER, null));
		
		IData idata = IDataFactory.create();
		IDataUtil.put(idata.getCursor(), "a", 2);
		
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create("pre:foo"));
		ServiceStatus ss = mock(ServiceStatus.class);

		Iterator<InvokeChainProcessor> chainIterator = new ArrayList<InvokeChainProcessor>().iterator();

		// Execute
		cp.setEnabled(true);
		cp.process(chainIterator, baseService, idata, ss);
		
		assertEquals(1, getInvokeCount(cp, "pre1"));
		assertEquals(1, getInvokeCount(cp, "pre2"));
		assertEquals(0, getInvokeCount(cp, "inv1"));
		assertEquals(1, getInvokeCount(cp, "inv2"));
		assertEquals(0, getInvokeCount(cp, "inv3"));
		assertEquals(1, getInvokeCount(cp, "post1"));
		assertEquals(1, getInvokeCount(cp, "post2"));
		
		cp.getAdviceManager().clearAdvice();
	}

	private int getInvokeCount(FarsightChainProcessor cp, String adviceId) {
		return cp.getAdviceManager().getAdvice(adviceId).getInterceptor().getInvokeCount();
	}
	
	private Advice getCannedAdvice(String adviceId, InterceptPoint interceptPoint, String expression) throws IOException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		FlowPositionMatcherImpl serviceNameMatcher = new FlowPositionMatcherImpl(adviceId, "pre:foo");
		CannedResponseInterceptor interceptor = new CannedResponseInterceptor(classLoader.getResourceAsStream("cannedResponse.xml"));
		Matcher<? super IData> matcher;
		if (expression != null) {
			matcher = new JexlIDataMatcher(adviceId, expression);
		} else {
			matcher = new AlwaysTrueMatcher<IData>(adviceId);
		}
		ServicePipelinePointCut pointCut = new ServicePipelinePointCut(serviceNameMatcher, matcher, interceptPoint);
		return new Advice(adviceId, new GlobalRemit(), pointCut, interceptor);
	}
}
