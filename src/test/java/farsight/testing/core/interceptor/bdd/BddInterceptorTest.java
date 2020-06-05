package farsight.testing.core.interceptor.bdd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;

import farsight.testing.core.advice.AdviceManager;
import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.interceptor.assertion.Assertable;
import farsight.testing.core.stub.StubManager;

@PowerMockIgnore("javax.management.*")
public class BddInterceptorTest {

	private FarsightChainProcessor cp;
	private IData pipeline;
	private ServiceStatus ss;
	private Iterator<InvokeChainProcessor> chainIterator;


	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() {
		cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));
		pipeline = IDataFactory.create();
		ss = mock(ServiceStatus.class);
		chainIterator = new ArrayList<InvokeChainProcessor>().iterator();
	}
	
	@Test
	public void shouldTranslateToMap() throws Exception {
		ParsedScenario scenario = configureProcessor("bdd/assertionBdd.xml");
		BddInterceptor bddi = (BddInterceptor) scenario.getAdvice().getInterceptor();
		Map<String, Object> m = bddi.toMap();
		assertEquals("BddInterceptor", m.get("type"));
	}
	
	@Test
	public void shouldAssert() throws Exception {
		configureProcessor("bdd/assertionBdd.xml");

		// Execute a service, no change to pipeline
		cp.process(chainIterator, getBaseService("pub.test:svcA"), pipeline, ss);

		AdviceManager asm = cp.getAdviceManager();
		// Correct service, condition doesnt match
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals(0, asm.getInvokeCountForPrefix("PreBarAssertion"));
		assertTrue(asm.verifyInvokedNever("PreBarAssertion"));
		assertFalse(asm.verifyInvokedOnceOnly("PreBarAssertion"));
		assertFalse(asm.verifyInvokedAtLeast(1, "PreBarAssertion"));
		assertFalse(asm.verifyInvokedAtLeastOnce("PreBarAssertion"));
		assertTrue(asm.verifyInvokedAtMost(1, "PreBarAssertion"));

		// Correct service, condition match
		add(pipeline, "foo", 2);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals(1, asm.getInvokeCountForPrefix("PreBarAssertion"));
		assertFalse(asm.verifyInvokedNever("PreBarAssertion"));
		assertTrue(asm.verifyInvokedOnceOnly("PreBarAssertion"));
		assertTrue(asm.verifyInvokedAtLeast(1, "PreBarAssertion"));
		assertTrue(asm.verifyInvokedAtLeastOnce("PreBarAssertion"));
		assertTrue(asm.verifyInvokedAtMost(1, "PreBarAssertion"));

		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals(2, asm.getInvokeCountForPrefix("PreBarAssertion"));
		assertFalse(asm.verifyInvokedNever("PreBarAssertion"));
		assertFalse(asm.verifyInvokedOnceOnly("PreBarAssertion"));
		assertTrue(asm.verifyInvokedAtLeast(1, "PreBarAssertion"));
		assertTrue(asm.verifyInvokedAtLeastOnce("PreBarAssertion"));
		assertFalse(asm.verifyInvokedAtMost(1, "PreBarAssertion"));

	}

	@Test
	public void shouldGetInterceptorOfType() throws Exception {
		ParsedScenario scenario = configureProcessor("bdd/assertionBdd.xml");
		BddInterceptor bddi = (BddInterceptor) scenario.getAdvice().getInterceptor();
		assertEquals(1, bddi.getInterceptorsOfType(Assertable.class).size());
	}
	
	@Test
	public void shouldFireCannedReturn() throws Exception {
		configureProcessor("bdd/cannedReturnBdd.xml");

		// Execute a service, no change to pipeline
		cp.process(chainIterator, getBaseService("pub.test:svcA"), pipeline, ss);
		assertEquals(null, get(pipeline, "apple"));

		// Execute mocked service, pipeline changed
		cp.process(chainIterator, getBaseService("pub.test:svcB"), pipeline, ss);
		assertEquals("alpha", get(pipeline, "apple"));
		assertEquals("beta", get(pipeline, "pear"));
	}

	@Test
	public void shouldSetException() throws Exception {
		configureProcessor("bdd/exceptionBdd.xml");

		// Pipeline mocking
		IData alpha = IDataFactory.create();
		add(alpha, "beta", "abc");
		add(pipeline, "alpha", alpha);

		// Execute a service, no change to pipeline
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		verify(ss, times(0)).setException(isA(Exception.class));

		// Execute mocked service, pipeline changed
		add(alpha, "beta", "hello");
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		verify(ss, times(1)).setException(isA(Exception.class));
	}

	@Test
	public void shouldExecuteMultipleReturnsWithDefault() throws Exception {
		configureProcessor("bdd/multipleReturnBdd.xml");

		// No change to pipeline, not fired
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals(null, get(pipeline, "apple"));

		// Service condition so should set default
		add(pipeline, "foo", 2);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("gamma", get(pipeline, "apple"));

		add(pipeline, "input", 1);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("alpha", get(pipeline, "apple"));

		add(pipeline, "input", 2);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("beta", get(pipeline, "apple"));
	}

	@Test
	public void shouldExecuteServiceAndWhenConditions() throws Exception {
		configureProcessor("bdd/multipleReturnWithElseBdd.xml");

		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("gamma", get(pipeline, "apple"));

		add(pipeline, "input", 1);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("alpha", get(pipeline, "apple"));

		add(pipeline, "input", 2);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("beta", get(pipeline, "apple"));
	}

	@Test
	public void shouldReturnWithoutElse() throws Exception {
		configureProcessor("bdd/multipleReturnWithoutElseBdd.xml");

		// No change to pipeline, not fired
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals(null, get(pipeline, "apple"));

		add(pipeline, "input", 1);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("alpha", get(pipeline, "apple"));

		add(pipeline, "input", 2);
		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("beta", get(pipeline, "apple"));
	}

	@Test
	public void shouldSequentialReturn() throws Exception {
		configureProcessor("bdd/sequentialReturnBdd.xml");

		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("alpha", get(pipeline, "a"));

		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("beta", get(pipeline, "b"));

		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("gamma", get(pipeline, "c"));

		cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		assertEquals("alpha", get(pipeline, "a"));
	}

	@Test
	public void shouldRandomReturn() throws Exception {
		configureProcessor("bdd/randomReturnBdd.xml");

		for (int i = 0; i < 20; i++) {
			cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);
		}

		assertEquals("beta", get(pipeline, "b"));
		assertEquals("gamma", get(pipeline, "c"));
		assertEquals("alpha", get(pipeline, "a"));
	}

	@Test
	public void shouldCapturePipeline() throws Exception {
		String capture = folder.getRoot().getAbsolutePath() + "\\pipelineCaptureBdd.xml";
		String actual = folder.getRoot().getAbsolutePath() + "\\pipelineCaptureBdd-1.xml";

		InputStream fs = this.getClass().getClassLoader().getResourceAsStream("bdd/pipelineCaptureBdd.xml");
		try (Scanner scanner = new Scanner(fs, "UTF-8")) {
			ByteArrayInputStream bais = new ByteArrayInputStream(
					scanner.useDelimiter("\\A").next().replace("{{fl}}", capture).getBytes());
			FarsightChainProcessor cp = new FarsightChainProcessor(new AdviceManager(), mock(StubManager.class));
			cp.setEnabled(true);
			ParsedScenario scenario = new BddParser().parse(bais, null);
			cp.getAdviceManager().registerAdvice(scenario.getAdvice());

			add(pipeline, "foo", 2);
			cp.process(chainIterator, getBaseService("org.wmaop.foo:bar"), pipeline, ss);

			File f = new File(actual);
			assertTrue(f.exists());
			assertTrue(new String(Files.readAllBytes(f.toPath()))
					.contains("<number name=\"foo\" type=\"java.lang.Integer\">2</number>"));
		}
	}

	private ParsedScenario configureProcessor(String testXmlFileName) throws BddParseException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		cp.setEnabled(true);

		ParsedScenario scenario = new BddParser().parse(classLoader.getResourceAsStream(testXmlFileName), null);
		cp.getAdviceManager().registerAdvice(scenario.getAdvice());
		return scenario;
	}

	private BaseService getBaseService(String svcName) {
		BaseService baseService = mock(BaseService.class);
		when(baseService.getNSName()).thenReturn(NSName.create(svcName));
		return baseService;
	}

	private void add(IData idata, String k, Object v) {
		IDataCursor cursor = idata.getCursor();
		IDataUtil.put(cursor, k, v);
		cursor.destroy();
	}

	private Object get(IData idata, String k) {
		IDataCursor cursor = idata.getCursor();
		Object o = IDataUtil.get(cursor, k);
		cursor.destroy();
		return o;
	}
}
