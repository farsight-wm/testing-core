package farsight.testing.core.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MatchResultTest {

	@Test
	public void shouldMatchTrue() {
		MatchResult mrt = new MatchResult(true, "foo");
		assertTrue(mrt.isMatch());
		assertEquals("foo", mrt.getId());
		
		assertTrue(MatchResult.TRUE.isMatch());
		assertEquals("undefined", MatchResult.TRUE.getId());
	}
	
	@Test
	public void shouldMatchFalse() {
		MatchResult mrf = new MatchResult(false, "bar");
		assertFalse(mrf.isMatch());
		assertEquals("bar", mrf.getId());
		
		assertFalse(MatchResult.FALSE.isMatch());
		assertEquals("undefined", MatchResult.FALSE.getId());
	}
}
