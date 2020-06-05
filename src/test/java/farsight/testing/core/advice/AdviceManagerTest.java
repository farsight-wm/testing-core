package farsight.testing.core.advice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import farsight.testing.core.advice.remit.Remit;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.interceptor.Interceptor;
import farsight.testing.core.pointcut.PointCut;

public class AdviceManagerTest {

	private AdviceManager adm;

	@Before
	public void setUp() {
		adm = new AdviceManager();
	}

	@Test
	public void shouldResetAdviceForAll() {
		setupScopedAdvice(adm, true);
		adm.reset(Scope.ALL);
		assertNull(adm.getAdvice("advGlobal"));
		assertNull(adm.getAdvice("advSession"));
		assertNull(adm.getAdvice("advUser"));
	}

	@Test
	public void shouldResetAdviceOnlyForGlobalScope() {
		setupScopedAdvice(adm, true);
		adm.reset(Scope.GLOBAL);
		assertNull(adm.getAdvice("advGlobal"));
		assertNotNull(adm.getAdvice("advSession"));
		assertNotNull(adm.getAdvice("advUser1"));
		assertNotNull(adm.getAdvice("advUser2"));
	}

	@Test
	public void shouldResetAdviceOnlyForSessionScope() {
		setupScopedAdvice(adm, true);
		adm.reset(Scope.SESSION);
		assertNotNull(adm.getAdvice("advGlobal"));
		assertNull(adm.getAdvice("advSession"));
		assertNotNull(adm.getAdvice("advUser1"));
		assertNotNull(adm.getAdvice("advUser2"));
	}

	@Test
	public void shouldResetAdviceOnlyForUserScope() {
		setupScopedAdvice(adm, false);
		adm.reset(Scope.USER);
		assertNotNull(adm.getAdvice("advGlobal"));
		assertNotNull(adm.getAdvice("advSession"));
		assertNull(adm.getAdvice("advUser1"));
		assertNotNull(adm.getAdvice("advUser2"));
	}

	@Test
	public void shouldResetAdviceForUserAndSession() {
		setupScopedAdvice(adm, true);
		adm.reset(null);
		assertNotNull(adm.getAdvice("advGlobal"));
		assertNull(adm.getAdvice("advSession"));
		assertNull(adm.getAdvice("advUser1"));
		assertNull(adm.getAdvice("advUser2"));
	}

	@Test
	public void shouldTallyInvokeCountForPrefix() {
		adm.reset(Scope.ALL);
		adm.registerAdvice(createAdvice("adv1", Scope.GLOBAL, true));
		adm.registerAdvice(createAdvice("adv2", Scope.GLOBAL, true));
		assertEquals(2, adm.getInvokeCountForPrefix("ad"));
	}

	private void setupScopedAdvice(AdviceManager adm, boolean all) {
		adm.reset(Scope.ALL);
		adm.registerAdvice(createAdvice("advGlobal", Scope.GLOBAL, true));
		adm.registerAdvice(createAdvice("advSession", Scope.SESSION, true));
		adm.registerAdvice(createAdvice("advUser1", Scope.USER, true));
		adm.registerAdvice(createAdvice("advUser2", Scope.USER, all));
	}

	private Advice createAdvice(String id, Scope scope, boolean applicable) {
		Remit remit = mock(Remit.class);
		when(remit.isApplicable(scope)).thenReturn(applicable);
		Advice adv = mock(Advice.class);
		Interceptor interceptor = mock(Interceptor.class);
		when(interceptor.getInvokeCount()).thenReturn(1);
		when(adv.getInterceptor()).thenReturn(interceptor);
		PointCut pointCut = mock(PointCut.class);
		when(pointCut.getInterceptPoint()).thenReturn(InterceptPoint.INVOKE);
		when(adv.getPointCut()).thenReturn(pointCut);
		when(adv.getRemit()).thenReturn(remit);
		when(adv.getId()).thenReturn(id);
		return adv;
	}

}
