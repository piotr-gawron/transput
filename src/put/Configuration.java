package put;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import put.exception.InvalidStateException;

public class Configuration {
	Logger					logger														= Logger.getLogger(Configuration.class);

	private String	encoding													= "UTF8";
	private String	configFileName										= "conf/config.properties";

	private String	municipalitiesFileName						= "data/municipalities.txt";
	private String	otherMunicipalitiesFileName				= "data/otherMunicipalities.txt";

	private String	busConnectionFileName							= "data/busConnection.txt";
	private String	busNeighborhoodFileName						= "data/busNeighborhood.txt";
	private String	busesFileName											= "data/buses.txt";
	private String	trafficFileName										= "data/traffic.txt";
	private String	trafficNameMapperFileName					= "data/trafficMapper.txt";
	private String	municipalitiesCoordinatesFileName	= "data/coordinates.txt";

	private String	trainTimeFileName									= null;
	private String	trainNeighborhoodFileName					= "data/trainNeighborhood.txt";
	private String	trainsFileName										= null;

	private boolean	validConfiguration								= false;

	private int			maxColumns												= 49;
	private int			hoursColumns											= 19;
	private int			nameColumn												= 19;

	private int			changeDifferentTransportTypeTime	= 20;
	private int			changeSameTransportTypeTime				= 10;

	private double	minTrafficForConnection						= 0;

	private double	negligibleTrafficSize							= 0.1;

	private int			busSize														= 80;
	private int			trainSize													= 200;

	private double	epsilon														= 1e-6;

	public boolean isValidConfiguration() {
		return validConfiguration;
	}

	private static Configuration	GLOBAL_CONFIGURATION	= null;

	public static Configuration getConfiguration(String[] args) throws ParseException, IOException {
		if (GLOBAL_CONFIGURATION == null) {
			reloadConfiguration(args);
		}
		return GLOBAL_CONFIGURATION;
	}

	public static Configuration getConfiguration() {
		if (GLOBAL_CONFIGURATION == null) {
			throw new InvalidStateException("Configuration not initialized");
		}
		return GLOBAL_CONFIGURATION;
	}

	public static Configuration reloadConfiguration(String[] args) throws ParseException, IOException {
		GLOBAL_CONFIGURATION = new Configuration(args);
		return GLOBAL_CONFIGURATION;
	}

	private Configuration(String[] args) throws ParseException, IOException {
		Options options = getOptions();

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		if (cmd.hasOption("h")) {
			printHelp(options);
			return;
		}
		if (cmd.hasOption("e")) {
			encoding = cmd.getOptionValue("e");
		}

		readConfigFile(configFileName);

		if (cmd.getOptionValue("m") != null)
			municipalitiesFileName = cmd.getOptionValue("m");
		if (cmd.getOptionValue("fm") != null)
			otherMunicipalitiesFileName = cmd.getOptionValue("fm");

		if (cmd.getOptionValue("bd") != null)
			busConnectionFileName = cmd.getOptionValue("bd");
		if (cmd.getOptionValue("bc") != null)
			busNeighborhoodFileName = cmd.getOptionValue("bc");
		if (cmd.getOptionValue("bl") != null)
			busesFileName = cmd.getOptionValue("bl");

		if (cmd.getOptionValue("tt") != null)
			trainTimeFileName = cmd.getOptionValue("tt");
		if (cmd.getOptionValue("tc") != null)
			trainNeighborhoodFileName = cmd.getOptionValue("tc");
		if (cmd.getOptionValue("tl") != null)
			trainsFileName = cmd.getOptionValue("tl");
		if (cmd.getOptionValue("nc") != null) {
			nameColumn = Integer.valueOf(cmd.getOptionValue("nc"));
			hoursColumns = nameColumn;
		}

		logger.debug("Using parameters: ");
		logger.debug("* encoding: " + encoding);
		logger.debug("* configFile: " + configFileName);

		logger.debug("* municipalitiesFileName: " + municipalitiesFileName);
		logger.debug("* otherMunicipalitiesFileName: " + otherMunicipalitiesFileName);
		logger.debug("* municipalitiesCoordinatesFileName: " + municipalitiesCoordinatesFileName);

		logger.debug("* busConnectionFileName: " + busConnectionFileName);
		logger.debug("* busNeighborhoodFileName: " + busNeighborhoodFileName);
		logger.debug("* busesFileName: " + busesFileName);

		logger.debug("* trainTimeFileName: " + trainTimeFileName);
		logger.debug("* trainNeighborhoodFileName: " + trainNeighborhoodFileName);
		logger.debug("* trainsFileName: " + trainsFileName);

		logger.debug("* trafficFileName: " + trafficFileName);
		logger.debug("* trafficMapperFileName: " + trafficNameMapperFileName);

		logger.debug("* maxColumns: " + maxColumns);
		logger.debug("* hoursColumns: " + hoursColumns);
		logger.debug("* nameColumn: " + nameColumn);

		logger.debug("* minTrafficForConnection: " + minTrafficForConnection);

		logger.debug("* busSize: " + busSize);
		logger.debug("* trainSize: " + trainSize);

		validConfiguration = true;
	}

	private void readConfigFile(String configFile) throws IOException {
		Properties prop = new Properties();
		InputStream input = new FileInputStream(configFile);

		// load a properties file
		prop.load(input);

		if (prop.getProperty("file.municipalities") != null) {
			municipalitiesFileName = prop.getProperty("file.municipalities");
		}
		if (prop.getProperty("file.otherMunicipalities") != null) {
			otherMunicipalitiesFileName = prop.getProperty("file.otherMunicipalities");
		}

		if (prop.getProperty("file.busConnection") != null) {
			busConnectionFileName = prop.getProperty("file.busConnection");
		}
		if (prop.getProperty("file.busNeighborhood") != null) {
			busNeighborhoodFileName = prop.getProperty("file.busNeighborhood");
		}
		if (prop.getProperty("file.buses") != null) {
			busesFileName = prop.getProperty("file.buses");
		}

		if (prop.getProperty("file.trainTime") != null) {
			trainTimeFileName = prop.getProperty("file.trainTime");
		}
		if (prop.getProperty("file.trainNeighborhood") != null) {
			trainNeighborhoodFileName = prop.getProperty("file.trainNeighborhood");
		}
		if (prop.getProperty("file.trains") != null) {
			trainsFileName = prop.getProperty("file.trains");
		}

		if (prop.getProperty("file.traffic") != null) {
			trafficFileName = prop.getProperty("file.traffic");
		}

		if (prop.getProperty("file.trafficMapper") != null) {
			trafficNameMapperFileName = prop.getProperty("file.trafficMapper");
		}

		if (prop.getProperty("hourColumns") != null) {
			hoursColumns = Integer.valueOf(prop.getProperty("hourColumns"));
		}
		if (prop.getProperty("maxColumns") != null) {
			maxColumns = Integer.valueOf(prop.getProperty("maxColumns"));
		}

		if (prop.getProperty("minTrafficForConnection") != null) {
			minTrafficForConnection = Double.valueOf(prop.getProperty("minTrafficForConnection"));
		}

		if (prop.getProperty("nameColumn") != null) {
			nameColumn = Integer.valueOf(prop.getProperty("nameColumn"));
		}

		if (prop.getProperty("busSize") != null) {
			busSize = Integer.valueOf(prop.getProperty("busSize"));
		}
		if (prop.getProperty("trainSize") != null) {
			trainSize = Integer.valueOf(prop.getProperty("trainSize"));
		}
	}

	private void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar transport.jar [options]", options);
	}

	private Options getOptions() {
		Options options = new Options();

		options.addOption("h", false, "display help");
		options.addOption("e", "encoding", true, "encoding of all files (default: UTF8)");
		options.addOption("d", "default", false, "use default files from data directory");
		options.addOption("m", "municipalities", true, "text file with the list of municipalities");
		options.addOption("fm", "foreign-municipalities", true, "text file with the list of municipalities from outside");
		options.addOption("bd", "bus-distances", true, "text file with bus distances");
		options.addOption("bc", "bus-connections", true, "text file with bus connections");
		options.addOption("bl", "bus-lines", true, "text file with bus lines");
		options.addOption("tt", "train-time", true, "text file with train times");
		options.addOption("tc", "train-connections", true, "text file with train connections");
		options.addOption("tl", "train-lines", true, "text file with train lines");
		options.addOption("nc", "name-column", true, "name column in buses/trains line data");

		return options;
	}

	public String getMunicipalitiesFileName() {
		return municipalitiesFileName;
	}

	public String getOtherMunicipalitiesFileName() {
		return otherMunicipalitiesFileName;
	}

	public String getBusConnectionFileName() {
		return busConnectionFileName;
	}

	public String getBusNeighborhoodFileName() {
		return busNeighborhoodFileName;
	}

	public String getBusesFileName() {
		return busesFileName;
	}

	public String getTrainTimeFileName() {
		return trainTimeFileName;
	}

	public String getTrainNeighborhoodFileName() {
		return trainNeighborhoodFileName;
	}

	public String getTrainsFileName() {
		return trainsFileName;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public int getMaxColumns() {
		return maxColumns;
	}

	public int getHoursColumns() {
		return hoursColumns;
	}

	public int getNameColumn() {
		return nameColumn;
	}

	public int getChangeDifferentTransportTypeTime() {
		return changeDifferentTransportTypeTime;
	}

	public void setChangeDifferentTransportTypeTime(int changeDifferentTransportTypeTime) {
		this.changeDifferentTransportTypeTime = changeDifferentTransportTypeTime;
	}

	public int getChangeSameTransportTypeTime() {
		return changeSameTransportTypeTime;
	}

	public void setChangeSameTransportTypeTime(int changeSameTransportTypeTime) {
		this.changeSameTransportTypeTime = changeSameTransportTypeTime;
	}

	public String getTrafficFileName() {
		return trafficFileName;
	}

	public static boolean isInitialized() {
		return GLOBAL_CONFIGURATION != null;
	}

	public String getTrafficNameMapperFileName() {
		return trafficNameMapperFileName;
	}

	public double getMinTrafficForConnection() {
		return minTrafficForConnection;
	}

	public void setMinTrafficForConnection(double minTrafficForConnection) {
		this.minTrafficForConnection = minTrafficForConnection;
	}

	public int getBusSize() {
		return busSize;
	}

	public int getTrainSize() {
		return trainSize;
	}

	public double getNegligibleTrafficSize() {
		return negligibleTrafficSize;
	}

	/**
	 * @return the municipalitiesCoordinatesFileName
	 * @see #municipalitiesCoordinatesFileName
	 */
	public String getMunicipalitiesCoordinatesFileName() {
		return municipalitiesCoordinatesFileName;
	}

	public double getEpsilon() {
		return epsilon;
	}

}
