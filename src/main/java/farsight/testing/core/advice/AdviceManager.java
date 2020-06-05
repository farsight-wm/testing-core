package farsight.testing.core.advice;

import static farsight.testing.core.advice.AdviceState.DISPOSED;
import static farsight.testing.core.advice.AdviceState.ENABLED;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import farsight.testing.core.advice.remit.Remit;
import farsight.testing.core.interceptor.CompositeInterceptor;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.interceptor.Interceptor;

public class AdviceManager extends Observable {

	private static final Logger logger = LogManager.getLogger(AdviceManager.class);

	protected final Map<InterceptPoint, List<Advice>> advices = new EnumMap<>(InterceptPoint.class);
	protected final Map<String, Advice> idAdvice = new HashMap<>();

	public void registerAdvice(Advice advice) {

		Advice oldAdvice = getAdvice(advice.getId());
		if (oldAdvice != null) {
			unregisterAdvice(oldAdvice);
		}
		advices.get(advice.getPointCut().getInterceptPoint()).add(advice);
		idAdvice.put(advice.getId(), advice);
		if (advice.getAdviceState() == AdviceState.NEW) {
			notify(advice);
		}

		advice.setAdviceState(ENABLED);
		notify(advice);
		logger.info("Registered advice " + advice);
	}

	public void unregisterAdvice(String adviceId) {
		Advice advice = getAdvice(adviceId);
		if (advice != null) {
			unregisterAdvice(advice);
		}
	}

	public void unregisterAdvice(Advice advice) {
		if (advice == null) {
			return; // fail safe
		}
		// Possibly wrapped so base the removal on the id
		List<Advice> advcs = advices.get(advice.getPointCut().getInterceptPoint());
		for (Advice advc : advcs) {
			if (advc.getId().equals(advice.getId())) {
				advcs.remove(advc);
				break;
			}
		}
		idAdvice.remove(advice.getId());
		advice.setAdviceState(DISPOSED);
		notify(advice);
	}

	public void clearAdvice() {
		for (List<Advice> advs : advices.values()) {
			List<Advice> advCopy = new ArrayList<>(advs);
			for (Advice adv : advCopy) {
				unregisterAdvice(adv);
			}
		}
		logger.info("Cleared all Advice");
	}

	public Advice getAdvice(String id) {
		return idAdvice.get(id);
	}

	public List<Advice> listAdvice() {
		List<Advice> list = new ArrayList<>();
		for (List<Advice> adv : advices.values()) {
			list.addAll(adv);
		}
		return list;
	}

	public void reset(Scope scope) {
		if (scope == Scope.ALL) {
			advices.clear();
			idAdvice.clear();
			for (InterceptPoint ip : InterceptPoint.values()) {
				advices.put(ip, new ArrayList<Advice>());
			}
		} else {
			conditionallyUnregisterAdvice(scope);
		}
	}

	private void conditionallyUnregisterAdvice(Scope scope) {
		for (Entry<String, Advice> advRef : new HashMap<String, Advice>(idAdvice).entrySet()) {
			Advice adv = advRef.getValue();
			Remit remit = adv.getRemit();
			if (scope == null) {
				if (!remit.isApplicable(Scope.GLOBAL)) { // No scope so remove user and session only - ie not global
					unregisterAdvice(adv);
				}
			} else if (remit.isApplicable(scope)) { // Only unregister for applicable scope
				unregisterAdvice(adv);
			}
		}
	}

	public List<Advice> getAdvicesForInterceptPoint(InterceptPoint interceptPoint) {
		return advices.get(interceptPoint);
	}

	public int getInvokeCountForPrefix(String prefix) {
		int invokeCount = 0;
		for (Entry<String, Advice> e : idAdvice.entrySet()) {
			Advice advice = e.getValue();
			Interceptor i = advice.getInterceptor();
			if (e.getKey().startsWith(prefix)) {
				invokeCount += i.getInvokeCount();
			} else if (i instanceof CompositeInterceptor) {
				for (Interceptor inc : ((CompositeInterceptor)i).getInterceptors()) {
					if (inc.getName().startsWith(prefix)) {
						invokeCount += inc.getInvokeCount();
					}
				}
			}

		}
		return invokeCount;
	}

	public boolean verifyInvokedOnceOnly(String name) {
		return getInvokeCountForPrefix(name) == 1;
	}
	
	public boolean verifyInvokedAtLeastOnce(String name) {
		return getInvokeCountForPrefix(name) >= 1;
	}
	
	public boolean verifyInvokedAtLeast(int count, String name) {
		return getInvokeCountForPrefix(name) >= count;
	}
	
	public boolean verifyInvokedNever(String name) {
		return getInvokeCountForPrefix(name) == 0;
	}

	public boolean verifyInvokedAtMost(int count, String name) {
		return getInvokeCountForPrefix(name) <= count;
	}

	protected void notify(Advice advice) {
		setChanged();
		notifyObservers(advice);
	}
	
}
