package put.reader;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BusConnectionReaderTest.class, MunicipalitiesReaderTest.class, TrainConnectionReaderTest.class, TransportConnectionReaderTest.class,
		FileNameMapperTest.class })
public class AllTests {

}
