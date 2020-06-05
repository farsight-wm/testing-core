package farsight.testing.core.interceptor.mock.canned;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.wm.data.IData;
import com.wm.data.IDataUtil;

import farsight.testing.core.flow.IDataXmlTool;
import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;

public class CannedResponseInterceptor extends BaseInterceptor {

	public static final String MAP_RESPONSE = "response";
	public static final String MAP_RESPONSE_SEQUENCE = "responseSequence";
	
	private static final String CANNED_RESPONSE_PREFIX = "CannedResponse:";

	public enum ResponseSequence {
		SEQUENTIAL, RANDOM;
	}

	private final IData[] cannedIdata;
	private final ResponseSequence sequence;
	private int seqCount = 0;
	private Random random = new Random();

	public CannedResponseInterceptor(String idataXml) throws IOException {
		this(IDataXmlTool.decode(idataXml));
	}
	
	public CannedResponseInterceptor(ResponseSequence seq, List<String> list) throws IOException {
		this(seq, list.toArray(new String[list.size()]));
	}

	public CannedResponseInterceptor(ResponseSequence seq, String[] list) throws IOException {
		super(CANNED_RESPONSE_PREFIX);
		sequence = seq;
		cannedIdata = new IData[list.length];
		for (int i = 0; i < cannedIdata.length; i++) {
			cannedIdata[i] = IDataXmlTool.decode(list[i]);
		}
	}

	public CannedResponseInterceptor(InputStream idataXmlStream) throws IOException {
		this(IDataXmlTool.decode(idataXmlStream));
	}

	public CannedResponseInterceptor(IData idata) {
		super(CANNED_RESPONSE_PREFIX);
		cannedIdata = new IData[] {idata};
		sequence = ResponseSequence.SEQUENTIAL;
	}

	public CannedResponseInterceptor(ResponseSequence seq, IData... idata) {
		super(CANNED_RESPONSE_PREFIX);
		cannedIdata = idata;
		sequence = seq;
	}

	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData pipeline) {
		invokeCount++;
		if (cannedIdata != null) {
			IDataUtil.merge(getResponse(), pipeline);
		}
		return InterceptResult.TRUE;
	}

	protected IData getResponse() {
		IData response;
		if (sequence == ResponseSequence.RANDOM) {
			response = cannedIdata[random.nextInt(cannedIdata.length)];
		} else {
			response = cannedIdata[(seqCount++ % cannedIdata.length)];
		}
		return response;
	}

	@Override
	public String toString() {
		return "CannedResponseInterceptor";
	}

	@Override
	protected void addMap(Map<String, Object> am) {
		am.put(MAP_TYPE, "CannedResponseInterceptor");
		am.put(MAP_RESPONSE_SEQUENCE, sequence.toString());
		int i = 0;
		for (IData idata : cannedIdata) {
			am.put(MAP_RESPONSE + i++, idata);
		}
	}

}
