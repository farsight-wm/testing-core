package farsight.testing.core.interceptor.bdd;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wmaop.interceptor.bdd.xsd.Scenario;
import org.wmaop.interceptor.bdd.xsd.Scope.User;
import org.wmaop.interceptor.bdd.xsd.Service;
import org.wmaop.interceptor.bdd.xsd.When;

import com.wm.data.IData;

import farsight.testing.core.advice.Advice;
import farsight.testing.core.advice.remit.GlobalRemit;
import farsight.testing.core.advice.remit.Remit;
import farsight.testing.core.advice.remit.SessionRemit;
import farsight.testing.core.advice.remit.UserRemit;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.interceptor.Interceptor;
import farsight.testing.core.matcher.AlwaysTrueMatcher;
import farsight.testing.core.matcher.FlowPositionMatcherImpl;
import farsight.testing.core.matcher.Matcher;
import farsight.testing.core.matcher.jexl.JexlIDataMatcher;
import farsight.testing.core.pointcut.ServicePipelinePointCut;

public class BddParser {

	private static final Logger logger = LogManager.getLogger(BddParser.class);

	class AssertionHolder {
		protected String whenCondition;
		protected String whenid;
		protected String assertionid;
	}

	public ParsedScenario parse(InputStream bddstream, String adviceId) throws BddParseException  {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Scenario.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Scenario scenario = (Scenario) jaxbUnmarshaller.unmarshal(bddstream);
			return new ParsedScenario(processAdvice(scenario, adviceId), scenario.getGiven().getService().getValue());
		} catch (JAXBException e) {
			throw new BddParseException("Error parsing " + adviceId, e);
		}
	}

	private Advice processAdvice(Scenario scenario, String adviceId) {
		Interceptor interceptor = new BddInterceptor(scenario, true);
		String id = adviceId != null && adviceId.length() > 0 ? adviceId : scenario.getId(); 
		return new Advice(id, getScope(scenario), getJoinPoint(scenario), interceptor);
	}

	private Remit getScope(Scenario scenario) {
		if (scenario.getScope() == null) {
			return new UserRemit();
		}
		if (scenario.getScope().getSession() != null) { 
			return new SessionRemit();
		}
		User user = scenario.getScope().getUser(); 
		if (user != null) {
			return new UserRemit(user.getUsername());
		}
		return new GlobalRemit();
	}
	private InterceptPoint getInterceptPoint(Scenario scenario) {
		InterceptPoint interceptPoint = InterceptPoint.valueOf(scenario.getGiven().getService().getIntercepted().toUpperCase());
		logger.info("Creating intercept point: " + interceptPoint);
		return interceptPoint;
	}

	private ServicePipelinePointCut getJoinPoint(Scenario scenario) {
		Service service = scenario.getGiven().getService();
		When when = scenario.getGiven().getWhen();

		FlowPositionMatcherImpl flowPositionMatcher = new FlowPositionMatcherImpl(service.getValue() + '_' + service.getIntercepted(), service.getValue());
		logger.info("Created flow position matcher: " + flowPositionMatcher);

		return new ServicePipelinePointCut(flowPositionMatcher, getMatcher(when), getInterceptPoint(scenario));
	}

	private Matcher<? super IData> getMatcher(When when) {
		String condition = null;
		String id = null;
		if (when != null) {
			condition = when.getCondition();
			id = when.getId();
		}
		Matcher<? super IData> pipelineMatcher;
		if (condition != null && condition.length() > 0) {
			pipelineMatcher = new JexlIDataMatcher(id, condition);
		} else {
			pipelineMatcher = new AlwaysTrueMatcher<>(id);
		}
		logger.info("Created pipeline matcher: " + pipelineMatcher);
		return pipelineMatcher;
	}
}
