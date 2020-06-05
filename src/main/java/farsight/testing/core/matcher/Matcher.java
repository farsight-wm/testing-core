package farsight.testing.core.matcher;

import java.util.Map;

public interface Matcher<T> {
	MatchResult match(T value);

	Map<String, Object> toMap();
}