package put;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public abstract class TestFunctions {
	private static Logger	logger	= Logger.getLogger(TestFunctions.class);
	

	static {
		PropertyConfigurator.configure("conf/log4j.properties");
		try {
			Configuration.getConfiguration(new String[] {});
		} catch (Exception e) {
			logger.fatal(e.getMessage(),e);
		}
	}

}
