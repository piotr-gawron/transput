package put.reader;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import put.Configuration;
import put.TestFunctions;
import put.data.Municipalities;
import put.data.TransportConnection;
import put.data.TransportType;
import put.reader.MunicipalitiesReader;
import put.reader.TransportConnectionReader;

public class TransportConnectionReaderTest extends TestFunctions {
	Logger				logger				= Logger.getLogger(TransportConnectionReaderTest.class);

	Configuration	configuration	= Configuration.getConfiguration();

	@Before
	public void setUp() throws Exception {
		Configuration.reloadConfiguration(new String[]{"-nc","19"});
	}

	@After
	public void tearDown() throws Exception {
		Configuration.reloadConfiguration(new String[]{});
	}

	@Test
	public void testSingleBusPerFile() {
		try {
			MunicipalitiesReader municipalitiesReader = new MunicipalitiesReader();
			TransportConnectionReader tcReader = new TransportConnectionReader(TransportType.UNKNOWN);
			Municipalities municipalities = municipalitiesReader.readDataFromFile("testFiles/municipalities.txt");
			TransportConnection tConnection = tcReader.readTransportConnectionForOneBusPerFile(municipalities, "testFiles/transportConnection.txt");

			assertEquals(3, tConnection.getStops().size());
			assertEquals("Poznań", tConnection.getStop(0).getMunicipality().getName());
			assertEquals((Integer) 12, tConnection.getStop(0).getHour());
			assertEquals((Integer) 0, tConnection.getStop(0).getMinute());

			assertEquals("Swarzędz", tConnection.getStop(1).getMunicipality().getName());
			assertEquals((Integer) 12, tConnection.getStop(1).getHour());
			assertEquals((Integer) 20, tConnection.getStop(1).getMinute());

			assertEquals("Września", tConnection.getStop(2).getMunicipality().getName());
			assertEquals((Integer) 13, tConnection.getStop(2).getHour());
			assertEquals((Integer) 1, tConnection.getStop(2).getMinute());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}

	@Test
	public void testComplex() {
		try {
			MunicipalitiesReader municipalitiesReader = new MunicipalitiesReader();
			TransportConnectionReader tcReader = new TransportConnectionReader(TransportType.UNKNOWN);
			Municipalities municipalities = municipalitiesReader.readDataFromFile("testFiles/municipalities2.txt");
			List<TransportConnection> tConnections = tcReader.readTransportConnections(municipalities, "testFiles/transportConnections.txt");

			assertNotNull(tConnections);

			assertEquals(3, tConnections.size());
			assertEquals("Gostyn - Jerka - Koscian", tConnections.get(0).getName());
			assertEquals("Gostyn - Jerka - Koscian (2)", tConnections.get(1).getName());
			assertEquals("Gostyn - Jerka - Koscian (2)", tConnections.get(2).getName());

			assertEquals((Integer) 12, tConnections.get(0).getStop(0).getHour());
			assertEquals((Integer) 0, tConnections.get(0).getStop(0).getMinute());
			assertEquals("Gostyn", tConnections.get(0).getStop(0).getMunicipality().getName());

			assertEquals(3, tConnections.get(0).getStops().size());

			assertEquals((Integer) 9, tConnections.get(1).getStop(0).getHour());
			assertEquals((Integer) 12, tConnections.get(1).getStop(0).getMinute());
			assertEquals(3, tConnections.get(1).getStops().size());
			assertEquals("Koscian", tConnections.get(1).getStop(0).getMunicipality().getName());

			assertEquals((Integer) 12, tConnections.get(2).getStop(0).getHour());
			assertEquals((Integer) 2, tConnections.get(2).getStop(0).getMinute());
			assertEquals(3, tConnections.get(2).getStops().size());
			assertEquals("Koscian", tConnections.get(2).getStop(0).getMunicipality().getName());


		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}

}
