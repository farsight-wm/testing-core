package farsight.testing.core.advice.remit;

import static farsight.testing.core.advice.Scope.*;

import farsight.testing.core.advice.Scope;

public final class GlobalRemit implements Remit {

	@Override
	public final boolean isApplicable() {
		return true;
	}

	@Override
	public boolean isApplicable(Scope scope) {
		return scope == ALL || scope == GLOBAL;
	}

	@Override
	public String toString() {
		return "GlobalScope";
	}
}
