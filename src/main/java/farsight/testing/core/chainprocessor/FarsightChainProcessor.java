package farsight.testing.core.chainprocessor;

import static farsight.testing.core.advice.AdviceState.ENABLED;
import static farsight.testing.core.interceptor.InterceptPoint.AFTER;
import static farsight.testing.core.interceptor.InterceptPoint.BEFORE;
import static farsight.testing.core.interceptor.InterceptPoint.INVOKE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;

import farsight.testing.core.advice.Advice;
import farsight.testing.core.advice.AdviceManager;
import farsight.testing.core.advice.Scope;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.stub.StubLifecycleObserver;
import farsight.testing.core.stub.StubManager;

public class FarsightChainProcessor implements InvokeChainProcessor {

	private static final InterceptResult NO_INTERCEPT = new InterceptResult(false);

	private static final Logger logger = LogManager.getLogger(FarsightChainProcessor.class);

	private static FarsightChainProcessor instance;

	private boolean interceptingEnabled = false;

	private final StubManager stubManager;
	private final AdviceManager adviceManager;
	
	public static FarsightChainProcessor getInstance() {
		return instance;
	}
	
	public static void setInstance(FarsightChainProcessor acp) {
		instance = acp;
		
	}
	
	@SuppressWarnings("unchecked")
	public static void uninstall() {
		//since reloads my lead to a new ClassLoder that loads this class we need to go with class name
		InvokeManager invokeManager = InvokeManager.getDefault();
		ArrayList<InvokeChainProcessor> processors = null;
		try {
			Field _processors = InvokeManager.class.getDeclaredField("_processors");
			_processors.setAccessible(true);
			processors = (ArrayList<InvokeChainProcessor>) _processors.get(invokeManager);
		} catch (Exception e) {
			throw new RuntimeException("Cannot uninstall FarsightChainProcessor", e);
		}
		final String className = FarsightChainProcessor.class.getCanonicalName();
		for(InvokeChainProcessor proc: processors) {
			if(className.equals(proc.getClass().getCanonicalName()))
				invokeManager.unregisterProcessor(proc);
		}
	}
	
	public static void install() {
		if(getInstance() != null) {
			uninstall();
			InvokeManager.getDefault().registerProcessor(getInstance());
		}
	}

	/**
	 * Instantiated by invokemanager - Limited control so no Spring here...
	 */
	public FarsightChainProcessor() {
		this(new AdviceManager(), new StubManager());
	}

	public FarsightChainProcessor(AdviceManager advMgr, StubManager stbMgr) {
		adviceManager = advMgr;
		stubManager = stbMgr;
		
		logger.info("Initialising " + this.getClass().getName());
		adviceManager.reset(Scope.ALL);
		setInstance(this);
	
		adviceManager.addObserver(new StubLifecycleObserver(stubManager));
	}

	public void setEnabled(boolean enabled) {
		interceptingEnabled = enabled;
		logger.info("Intercepting " + (enabled ? "enabled" : "disabled"));
	}

	public boolean isEnabled() {
		return interceptingEnabled;
	}

	/*
	 * ************* Interception ************* 
	 */
	
	@Override
	public void process(@SuppressWarnings("rawtypes") Iterator processorChain, BaseService baseService, IData idata,
			ServiceStatus serviceStatus) throws ServerException {
		
		logger.debug("AOP:process " + baseService.getNSName());

		if (interceptingEnabled) {
			processIntercept(processorChain, baseService, idata, serviceStatus);
		} else if (processorChain.hasNext()) {
			((InvokeChainProcessor) processorChain.next()).process(processorChain, baseService, idata, serviceStatus);
		}
	}

	private void processIntercept(@SuppressWarnings("rawtypes") Iterator processorChain, BaseService baseService,
			IData idata, ServiceStatus serviceStatus) throws ServerException {
		FlowPosition pipelinePosition = new FlowPosition(BEFORE, baseService.getNSName().getFullName());
		InterceptResult beforeIntResult = processAdvice(false, pipelinePosition, idata, serviceStatus);
		if (beforeIntResult.getException() != null) {
			return; // Exception in before to prevent execution of service/mock
		}
		
		pipelinePosition.setInterceptPoint(INVOKE);
		InterceptResult intResult = processAdvice(true, pipelinePosition, idata, serviceStatus);

		if (intResult.hasIntercepted() && logger.isDebugEnabled()) {
			logger.info("Intercepted: " + ReflectionToStringBuilder.toString(serviceStatus));
		}

		if (!intResult.hasIntercepted() && processorChain.hasNext()) {
			((InvokeChainProcessor) processorChain.next()).process(processorChain, baseService, idata, serviceStatus);
		}

		pipelinePosition.setInterceptPoint(AFTER);
		processAdvice(false, pipelinePosition, idata, serviceStatus);
	}

	private InterceptResult processAdvice(boolean exitOnIntercept, FlowPosition pos, IData idata, ServiceStatus serviceStatus) {
		InterceptResult hasIntercepted = NO_INTERCEPT;
		try {
			for (Advice advice : adviceManager.getAdvicesForInterceptPoint(pos.getInterceptPoint())) {
				if (advice.getAdviceState() == ENABLED && advice.isApplicable(pos, idata)) {
					InterceptResult ir = intercept(pos, idata, advice);
					if (ir.hasIntercepted()) {
						hasIntercepted = ir; // Ensure its only set, never reset to false
						if (exitOnIntercept) {
							// FIX: When invoking mocked service directly, the output pipeline is not returned to test
							serviceStatus.setReturnValue(idata);
							
							break; // Used to break on first intercept if required
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error intercepting. Behaviour at " + pos + " may be unknown", e);
		}
		if (hasIntercepted.getException() != null) {
			serviceStatus.setException(hasIntercepted.getException());
		}
		return hasIntercepted;
	}

	private InterceptResult intercept(FlowPosition pos, IData idata, Advice advice) {
		InterceptResult interceptResult = advice.getInterceptor().intercept(pos, idata);
		logger.info("Intercepting " + advice.getId() + " " + pos.getInterceptPoint() + ' ' + pos + " - " + interceptResult.hasIntercepted());
		return interceptResult;
	}

	public void reset(Scope scope) {
		adviceManager.reset(scope);
		if (scope == Scope.ALL) {
			stubManager.clearStubs();
			setEnabled(false);
		}
	}
	
	/*
	 * ************* Advice handling ************* 
	 */

	public AdviceManager getAdviceManager() {
		return adviceManager;
	}
	
	/*
	 * ************* Stub handling ************* 
	 */
	public StubManager getStubManager() {
		return stubManager;
	}
}
