package farsight.testing.core.interceptor.mock.restful;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.wm.data.IData;
import com.wm.data.IDataUtil;

import farsight.testing.core.flow.IDataXmlTool;
import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.interceptor.InterceptionException;

public class RestDelegatingInterceptor extends BaseInterceptor {

	public static final String MAP_DESTINATION_URL = "destinationUrl";
	public static final String APPLICATION_XML = "application/xml";

	private final String destinationUrl;
	private final String serviceName;

	public RestDelegatingInterceptor(String serviceName, String destinationUrl) {
		super("Restful:"+serviceName);
		this.serviceName = serviceName;
		this.destinationUrl = destinationUrl;
	}

	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData idata) {
		invokeCount++;
		sendPost(idata);
		return InterceptResult.TRUE;
	}

	@Override
	public String getName() {
		return serviceName;
	}
	
	private void sendPost(IData idata) {
		try {
			URL url = new URL(destinationUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", APPLICATION_XML);

			OutputStream os = conn.getOutputStream();
			IDataXmlTool.encode(os, idata);
			os.flush();

			IDataUtil.merge(IDataXmlTool.decode(conn.getInputStream()), idata);

			conn.disconnect();
		} catch (IOException e) {
			throw new InterceptionException("Error while forwarding request", e);
		}
	}

	@Override
	protected void addMap(Map<String, Object> am) {
		am.put(MAP_TYPE, "RestDelegatingInterceptor");
		am.put(MAP_DESTINATION_URL, destinationUrl);
	}
}
