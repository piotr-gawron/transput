package put.reader;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import put.TestFunctions;
import put.data.Municipalities;
import put.reader.BusConnectionReader;
import put.reader.MunicipalitiesReader;


public class BusConnectionReaderTest extends TestFunctions{

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpdateConnection() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			BusConnectionReader busReader =  new BusConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			assertFalse(municipalities.getConnection(0, 1).isConnectedByBus());
			
			busReader.updateBusConnections(municipalities, "testFiles/connections.txt");
			assertTrue(municipalities.getConnection(0, 1).isConnectedByBus());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateDistance() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			BusConnectionReader busReader =  new BusConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			busReader.updateBusDistances(municipalities, "testFiles/distances.txt");
			assertEquals((Double)30.2,municipalities.getConnection(3, 0).getBusDistance());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateTime() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			BusConnectionReader busReader =  new BusConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			busReader.updateBusTimes(municipalities, "testFiles/distances.txt");
			assertEquals((Double)30.2,municipalities.getConnection(3, 0).getBusTime());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateDistance2() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			BusConnectionReader busReader =  new BusConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			busReader.updateBusDistances(municipalities, "testFiles/distancesEmpty.txt");
			assertEquals((Double)30.2,municipalities.getConnection(3, 0).getBusDistance());
			assertEquals((Double)30.0,municipalities.getConnection(1, 2).getBusDistance());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateTime2() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			BusConnectionReader busReader =  new BusConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			busReader.updateBusTimes(municipalities, "testFiles/distancesEmpty.txt");
			assertEquals((Double)30.2,municipalities.getConnection(3, 0).getBusTime());
			assertEquals((Double)30.0,municipalities.getConnection(1, 2).getBusTime());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

}
