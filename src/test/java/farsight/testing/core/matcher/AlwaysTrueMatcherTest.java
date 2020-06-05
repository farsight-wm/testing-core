package farsight.testing.core.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlwaysTrueMatcherTest {

	@Test
	public void shouldBeAlwaysTrue() {
		AlwaysTrueMatcher<Object> atm = new AlwaysTrueMatcher<Object>("foo");
		assertTrue(atm.match("kfhkfjhk").isMatch());
		assertEquals("foo", atm.match("gggkg").getId());
		assertEquals("foo", atm.toMap().get("id"));
	}

}
