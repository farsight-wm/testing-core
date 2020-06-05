package farsight.testing.core.stub;

import java.util.Observable;
import java.util.Observer;

import farsight.testing.core.advice.Advice;

public class StubLifecycleObserver implements Observer {

	private StubManager stubManager;

	public StubLifecycleObserver(StubManager stubManager) {
		this.stubManager = stubManager;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Advice advice = (Advice)arg;
		handleState(advice);
	}

	private void handleState(Advice advice) {
		switch (advice.getAdviceState()) {
		case NEW:
			stubManager.registerStubService(advice);
			break;
		case DISPOSED:
			if (stubManager.hasStub(advice)) {
				stubManager.unregisterStubService(advice);
			}
			break;
		default:
			break;
		}
	}

}
