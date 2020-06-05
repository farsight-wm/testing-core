package farsight.testing.core.interceptor.bdd;

import farsight.testing.core.advice.Advice;

public class ParsedScenario {

	private final String[] serviceNames;
	private final Advice advice;

	public ParsedScenario(Advice advice, String... mockServiceNames) {
		this.serviceNames = mockServiceNames;
		this.advice = advice;
	}

	public String[] getServiceNames() {
		return serviceNames;
	}

	public Advice getAdvice() {
		return advice;
	}

	
}
