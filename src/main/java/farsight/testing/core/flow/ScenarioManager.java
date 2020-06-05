package farsight.testing.core.flow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.lang.xml.Document;
import com.wm.lang.xml.WMDocumentException;

import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.interceptor.bdd.BddParseException;
import farsight.testing.core.interceptor.bdd.BddParser;
import farsight.testing.core.interceptor.bdd.ParsedScenario;
import farsight.testing.core.stub.StubManager;

public class ScenarioManager {

	private static final Logger logger = LogManager.getLogger(StubManager.class);
	
	public void registerScenario(IData pipeline) throws ServiceException {
		
		IDataCursor pipelineCursor = pipeline.getCursor();
		Object scenarioAsStream = IDataUtil.get(pipelineCursor, "scenarioAsStream");
		String scenarioAsString = IDataUtil.getString(pipelineCursor, "scenarioAsString");
		Document scenarioAsNode = (Document) IDataUtil.get(pipelineCursor, "scenarioAsDocument");
		String adviceId = IDataUtil.getString(pipelineCursor, "adviceId"); 
		pipelineCursor.destroy();

		InputStream scenarioStream;
		if (scenarioAsStream != null) {
			scenarioStream = (InputStream) scenarioAsStream;
		} else if (scenarioAsString != null) {
			scenarioStream = new ByteArrayInputStream(scenarioAsString.getBytes());
		} else if (scenarioAsNode != null) {
			StringBuffer sb = new StringBuffer(); //Required for appendGeneratedMarkup signature
			try {
				scenarioAsNode.appendGeneratedMarkup(sb);
			} catch (WMDocumentException e) {
				logger.error("Error parsing scenario", e);
				throw new ServiceException("Error parsing scenario " + e.getMessage());
			}
			
			scenarioStream = new ByteArrayInputStream(sb.toString().getBytes());
		} else {
			throw new ServiceException("Must specify the advice xml as an input");
		}
		
		try {
			ParsedScenario scenario = new BddParser().parse(scenarioStream, adviceId);

			FarsightChainProcessor aop = FarsightChainProcessor.getInstance();
			aop.getAdviceManager().registerAdvice(scenario.getAdvice());
			aop.getStubManager().registerStubService(scenario.getServiceNames());
			aop.setEnabled(true);
		} catch (BddParseException e) {
			logger.error("Error parsing scenario", e);
			throw new ServiceException("Error parsing scenario: " + e.getMessage());
		}
	}
}
