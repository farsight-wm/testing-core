package farsightwm.testing;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.List;
import com.softwareag.util.IDataMap;
import farsight.testing.core.advice.Advice;
import farsight.testing.core.advice.AdviceManager;
import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.flow.MockManager;
import farsight.testing.core.flow.ScenarioManager;
// --- <<IS-END-IMPORTS>> ---

public final class define

{
	// ---( internal utility methods )---

	final static define _instance = new define();

	static define _newInstance() { return new define(); }

	static define _cast(Object o) { return (define)o; }

	// ---( server methods )---




	public static final void getAdvice (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getAdvice)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		new MockManager().getAdvice(pipeline);
		// --- <<IS-END>> ---

                
	}



	public static final void getCaptures (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getCaptures)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// [o] record:1:required captures
		IDataMap p = new IDataMap(pipeline);
		String adviceId = p.getAsString("adviceId");
		AdviceManager manager = FarsightChainProcessor.getInstance().getAdviceManager();
		Advice advice = manager.getAdvice(adviceId);
		if(advice != null && advice.getInterceptor() != null) {
			p.put("captures", advice.getInterceptor().toMap().get("captures"));
		}
		// --- <<IS-END>> ---

                
	}



	public static final void getInvokeCount (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getInvokeCount)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		new MockManager().getInvokeCount(pipeline);				
		// --- <<IS-END>> ---

                
	}



	public static final void listAdvices (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(listAdvices)>> ---
		// @sigtype java 3.5
		IDataMap p = new IDataMap(pipeline);
		AdviceManager amgr = FarsightChainProcessor.getInstance().getAdviceManager();
		List<Advice> advices = amgr.listAdvice();
		IData[] result = new IData[advices.size()];
		int i = 0;
		for(Advice advice: advices) {
			IDataMap map = new IDataMap();
			map.putAll(advice.toMap());
			result[i++] = map.getIData();
		}
		p.put("advices", result);
		// --- <<IS-END>> ---

                
	}



	public static final void registerAssertion (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerAssertion)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// [i] field:0:required serviceName
		// [i] field:0:optional condition
		// [i] field:0:required interceptPoint {"BEFORE","INVOKE","AFTER"}
		new MockManager().registerAssertion(pipeline);
			
		// --- <<IS-END>> ---

                
	}



	public static final void registerExceptionMock (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerExceptionMock)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// [i] field:0:required interceptPoint
		// [i] field:0:required serviceName
		// [i] field:0:required condition
		// [i] field:0:required exception
		new MockManager().registerException(pipeline);
		// --- <<IS-END>> ---

                
	}



	public static final void registerFixedResponseMock (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerFixedResponseMock)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// [i] field:0:required serviceName
		// [i] field:0:optional condition
		// [i] field:0:required interceptPoint {"BEFORE","INVOKE","AFTER"}
		// [i] object:0:required response
		new MockManager().registerFixedResponseMock(pipeline);
		// --- <<IS-END>> ---

                
	}



	public static final void registerJexlResponseMock (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerJexlResponseMock)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// [i] field:0:required serviceName
		// [i] field:0:optional condition
		// [i] field:0:required interceptPoint {"BEFORE","INVOKE","AFTER"}
		// [i] field:0:required jexlScript
		new MockManager().registerJexlResponseMock(pipeline);	
		// --- <<IS-END>> ---

                
	}



	public static final void registerPipelineCapture (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerPipelineCapture)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// [i] field:0:required serviceName
		// [i] field:0:optional condition
		// [i] field:0:required interceptPoint {"BEFORE","INVOKE","AFTER"}
		// [i] field:0:optional capacity
		new MockManager().registerInMemoryCapture(pipeline);		
		// --- <<IS-END>> ---

                
	}



	public static final void registerScenario (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerScenario)>> ---
		// @sigtype java 3.5
		// [i] object:0:required scenarioAsStream
		// [i] field:0:required scenarioAsString
		// [i] object:0:required scenarioAsDocument
		// [i] field:0:required adviceId
		new ScenarioManager().registerScenario(pipeline);
		// --- <<IS-END>> ---

                
	}



	public static final void removeAdvice (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(removeAdvice)>> ---
		// @sigtype java 3.5
		// [i] field:0:required adviceId
		// pipeline
		new MockManager().removeAdvice(pipeline);
		// --- <<IS-END>> ---

                
	}
}

