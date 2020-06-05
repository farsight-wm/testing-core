package farsight.testing.core.interceptor.bdd;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import farsight.testing.core.chainprocessor.FarsightChainProcessor;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FarsightChainProcessor.class)
@PowerMockIgnore("javax.management.*")
public class BddParserTest {

	@Test
	public void shouldParse() throws BddParseException {

		PowerMockito.mockStatic(FarsightChainProcessor.class);
		FarsightChainProcessor mockProcessor = mock(FarsightChainProcessor.class);
		PowerMockito.when(FarsightChainProcessor.getInstance()).thenReturn(mockProcessor);

		InputStream bddstream = this.getClass().getResourceAsStream("/bdd/multipleReturnBdd.xml");
		ParsedScenario scenario = new BddParser().parse(bddstream, null);
		
		assertEquals("aspect id",scenario.getAdvice().getId());
	}
}
