package farsight.testing.core.advice.remit;

import farsight.testing.core.advice.Scope;

public interface Remit {
	boolean isApplicable();
	boolean isApplicable(Scope scope);
}
