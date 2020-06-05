package farsight.testing.core.advice.remit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import farsight.testing.core.advice.Scope;

public class GlobalRemitTest {

	@Test
	public void shouldExerciseScopeApplicability() {
		GlobalRemit gr = new GlobalRemit();
		assertTrue(gr.isApplicable());
		assertTrue(gr.isApplicable(Scope.ALL));
		assertTrue(gr.isApplicable(Scope.GLOBAL));
		assertFalse(gr.isApplicable(Scope.USER));
		assertFalse(gr.isApplicable(Scope.SESSION));
	}

}
