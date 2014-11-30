package put.reader;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import put.TestFunctions;

public class FileNameMapperTest extends TestFunctions{

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		try {
			INameMapper fnm = new FileNameMapper("testFiles/trafficMapper.txt", 0, 2);
			assertEquals("Drezdenko", fnm.getValue("30071"));
			assertEquals("Drezdenko", fnm.getValue("30105"));
			assertEquals("Drezdenko", fnm.getValue("306"));
			assertEquals(null, fnm.getValue("hello kity"));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
