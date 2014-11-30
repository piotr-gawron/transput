package put.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import put.TestFunctions;
import put.data.Municipalities;


public class TrainConnectionReaderTest extends TestFunctions{
	 static Logger logger = Logger.getLogger(TrainConnectionReaderTest.class);

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
			TrainConnectionReader trainReader =  new TrainConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			assertFalse(municipalities.getConnection(0, 1).isConnectedByTrain());
			
			trainReader.updateTrainConnections(municipalities, "testFiles/connections.txt");
			assertTrue(municipalities.getConnection(0, 1).isConnectedByTrain());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateDistance() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			TrainConnectionReader trainReader =  new TrainConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			trainReader.updateTrainDistances(municipalities, "testFiles/distances.txt");
			assertEquals((Double)30.2,municipalities.getConnection(3, 0).getTrainDistance());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateDistance2() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			TrainConnectionReader trainReader =  new TrainConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			trainReader.updateTrainDistances(municipalities, "testFiles/distancesEmpty.txt");
			assertEquals((Double)30.2,municipalities.getConnection(3, 0).getTrainDistance());
			assertEquals((Double)30.0,municipalities.getConnection(1, 2).getTrainDistance());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

	@Test
	public void testUpdateTime2() {
		try {
			MunicipalitiesReader reader = new MunicipalitiesReader();
			TrainConnectionReader trainReader =  new TrainConnectionReader();
			Municipalities municipalities = reader.readDataFromFile("testFiles/municipalities.txt");
			
			trainReader.updateTrainTimes(municipalities, "testFiles/time.txt");
			assertEquals((Double)30.0,municipalities.getConnection(3, 0).getTrainTime());
			assertEquals((Double)30.0,municipalities.getConnection(1, 2).getTrainTime());
			assertNull(municipalities.getConnection(1, 1).getTrainTime());
			assertNull(municipalities.getConnection(1, 0).getTrainTime());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unknown exception");
		}
	}

}
