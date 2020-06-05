package farsightwm.testing.util;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.lang.xml.Document;
import com.wm.util.coder.IDataXMLCoder;
import com.wm.util.coder.XMLCoderWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
// --- <<IS-END-IMPORTS>> ---

public final class flow

{
	// ---( internal utility methods )---

	final static flow _instance = new flow();

	static flow _newInstance() { return new flow(); }

	static flow _cast(Object o) { return (flow)o; }

	// ---( server methods )---




	public static final void invoke (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(invoke)>> ---
		// @sigtype java 3.5
		// [i] field:0:required service
		// [i] field:0:required namespace
		// [i] object:0:required withNewThread
		// [i] object:0:required withPipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	service = IDataUtil.getString( pipelineCursor, "service" );
		String	namespace = IDataUtil.getString( pipelineCursor, "namespace" );
		Boolean	withPipeline = IDataUtil.getBoolean( pipelineCursor, "withPipeline", false );
		Boolean	withNewThread = IDataUtil.getBoolean( pipelineCursor, "withNewThread", false );
		pipelineCursor.destroy();
		if (namespace == null) {
			int colonPos = service.indexOf(':');
			if (colonPos == -1) {
				namespace = "";
			} else {
				namespace = service.substring(0, colonPos);
				service = service.substring(colonPos + 1);
			}
		}
		
		checkServiceAvailable(namespace, service);
		
		try {
			if (withNewThread) {
				Service.doThreadInvoke(namespace, service, withPipeline?IDataUtil.deepClone(pipeline):IDataFactory.create());
			} else {
				Service.doInvoke(namespace, service, withPipeline?pipeline:IDataFactory.create());
			}
		} catch( Exception e){
			throw new ServiceException(e);
		} finally {
			pipelineCursor.destroy();
		}
			
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void requestBodyToIData (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(requestBodyToIData)>> ---
		// @sigtype java 3.5
		// [i] object:0:required ffdata
		// [i] object:0:required node
		// [i] object:1:required contentStream
		// [o] object:0:required body
		IDataCursor pipelineCursor = pipeline.getCursor();
		try {
			Object ffdata = IDataUtil.get( pipelineCursor, "ffdata" );
			Object node = IDataUtil.get( pipelineCursor, "node" );
			Object contentStream = IDataUtil.get( pipelineCursor, "contentStream" );
			
			Object pipelineXml = ffdata != null ? ffdata : node != null ? node : contentStream;
			if (pipelineXml != null) {
				InputStream is;
				if (pipelineXml instanceof Document) {
					IData idata = new IDataXMLCoder().decode((Document)pipelineXml);
					IDataUtil.put(pipelineCursor, "body", idata);
				} else {
					if (pipelineXml instanceof InputStream) {
						is = (InputStream) pipelineXml;
					} else if (pipelineXml instanceof String) {
						is = new ByteArrayInputStream(((String)pipelineXml).getBytes());
					} else if (pipelineXml instanceof byte[]) {
						is = new ByteArrayInputStream((byte[])pipelineXml);
					} else {
						throw new ServiceException("Unknown type " + pipelineXml.getClass());
					}
					if (is.available() > 0) {
						XMLCoderWrapper xc = new XMLCoderWrapper();
						IData idata = xc.decode(is);
						IDataUtil.put(pipelineCursor, "body", idata);
					}
				}
			}
		} catch (IOException e) {
			throw new ServiceException(e);
		} finally {
			pipelineCursor.destroy();
		}		
		// --- <<IS-END>> ---

                
	}



	public static final void restorePipelineFromIData (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(restorePipelineFromIData)>> ---
		// @sigtype java 3.5
		// [i] object:0:required pipelineXml
		IDataCursor pipelineCursor = pipeline.getCursor();
		Object pipelineXml = IDataUtil.get( pipelineCursor, "pipelineXml" );
		InputStream is;
		try {
			if (pipelineXml instanceof Document) {
				IDataUtil.merge(new IDataXMLCoder().decode((Document)pipelineXml), pipeline);
			} else {
				if (pipelineXml instanceof InputStream) {
					is = (InputStream) pipelineXml;
				} else if (pipelineXml instanceof String) {
					is = new ByteArrayInputStream(((String)pipelineXml).getBytes());
				} else {
					throw new ServiceException("Unknown type " + pipelineXml.getClass());
				}
				if (is.available() > 0) {
					XMLCoderWrapper xc = new XMLCoderWrapper();
					IDataUtil.merge(xc.decode(is), pipeline);
				}
			}
		} catch (IOException e) {
			throw new ServiceException(e);
		} finally {
			pipelineCursor.destroy();
		}		
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static void checkServiceAvailable(String namespace, String service) throws ServiceException {
		try {
			Namespace.current().getNode(namespace + ":" + service).getPackage().getName();
		}
		catch (Exception e) {
			throw new ServiceException("Service could not be found: " + namespace + ":" + service);
		}
	}
		
	// --- <<IS-END-SHARED>> ---
}

