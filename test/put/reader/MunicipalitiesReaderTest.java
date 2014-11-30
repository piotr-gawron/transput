package put.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import put.Configuration;
import put.TestFunctions;
import put.data.Municipalities;

public class MunicipalitiesReaderTest extends TestFunctions {
	static Logger	logger	= Logger.getLogger(MunicipalitiesReaderTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadDataFromFile() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			assertNotNull(municipalities);
			assertNotNull(municipalities.getMunicipality(5));
			assertNotNull(municipalities.getMunicipalityByName("Pobiedziska"));
			assertNull(municipalities.getMunicipalityByName("aloha"));
			assertEquals((Integer) 1, municipalities.getIndexByName("Murowana Goślina"));
			assertEquals((Integer) 2, municipalities.getIndexByName("Pobiedziska"));
			assertNotNull(municipalities.getConnection(2, 3));
			try {
				municipalities.getConnection(10, 10);
				fail("Exception expected");
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testReadCoordinatesDataFromFile() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			reader.addCoordinates(municipalities, "testFiles/coordinates.txt");
			assertNull(municipalities.getMunicipalityByName("Swarzędz").getCoordinates());
			assertNotNull(municipalities.getMunicipalityByName("Czerwonak").getCoordinates());
			assertEquals(52.4639522, municipalities.getMunicipalityByName("Czerwonak").getCoordinates().getLat(), Configuration.getConfiguration().getEpsilon());
			assertEquals(16.9819276, municipalities.getMunicipalityByName("Czerwonak").getCoordinates().getLng(), Configuration.getConfiguration().getEpsilon());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}
}
