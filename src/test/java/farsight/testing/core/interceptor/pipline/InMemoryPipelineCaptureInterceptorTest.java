package farsight.testing.core.interceptor.pipline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.softwareag.util.IDataMap;
import com.wm.data.IData;
import com.wm.data.IDataFactory;

public class InMemoryPipelineCaptureInterceptorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void shouldCaptureToMemory() {
		InMemoryPipelineCaptureInterceptor impci = spy(new InMemoryPipelineCaptureInterceptor(1));
		assertEquals(0,  impci.toMap().get("captureCount"));
		impci.intercept(null, IDataFactory.create());
		assertEquals(1,  impci.toMap().get("captureCount"));
		IData[] storage = getStorage(impci);
		assertNotNull(storage);
		assertNotNull(storage[0]);
	}
	
	@Test
	public void capturesShouldBeClonded() {
		IData input = IDataFactory.create();
		InMemoryPipelineCaptureInterceptor impci = spy(new InMemoryPipelineCaptureInterceptor(1));
		impci.intercept(null, input);
		
		assertFalse(input == getStorage(impci)[0]);
	}
	
	@Test
	public void capturesShouldNotBeChanged() {
		IDataMap input = new IDataMap();
		InMemoryPipelineCaptureInterceptor impci = spy(new InMemoryPipelineCaptureInterceptor(1));
		input.put("value", "A");
		impci.intercept(null, input.getIData());
		input.put("value", "B");
		assertEquals("A", new IDataMap(getStorage(impci)[0]).getAsString("value"));
	}
	
	
	private IData[] getStorage(InMemoryPipelineCaptureInterceptor impci) {
		return (IData[]) impci.toMap().get("captures");
	}

}
