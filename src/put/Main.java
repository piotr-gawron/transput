package put;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import put.data.BusTransportData;
import put.data.Municipalities;
import put.data.Municipality;
import put.data.TrainTransportData;
import put.data.TransportConnection;
import put.data.TransportType;
import put.graph.GraphModel;
import put.output.GoogleMapVis;
import put.output.Report;
import put.output.SchedulePrinter;
import put.reader.BusConnectionReader;
import put.reader.FileNameMapper;
import put.reader.INameMapper;
import put.reader.InvalidFileException;
import put.reader.MunicipalitiesReader;
import put.reader.SelfNameMapper;
import put.reader.TrainConnectionReader;
import put.reader.TransportConnectionReader;

public class Main {

	private static Logger	logger	= Logger.getLogger(Main.class);

	Configuration					configuration;

	static {
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	public static void main(String[] args) {
		try {
			new Main().run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void run(String[] args) throws IOException, InvalidFileException, ParseException {

		configuration = Configuration.getConfiguration(args);

		MunicipalitiesReader municipalitiesReader = new MunicipalitiesReader();
		logger.info("Reading municipalities: " + configuration.getMunicipalitiesFileName());
		Municipalities municipalities = municipalitiesReader.readDataFromFile(configuration.getMunicipalitiesFileName());

		if (configuration.getOtherMunicipalitiesFileName() != null) {
			logger.info("Reading foreign municipalities: " + configuration.getOtherMunicipalitiesFileName());
			municipalities.addOtherMunicipalities(municipalitiesReader.readOtherMunicipalities(configuration.getOtherMunicipalitiesFileName()));
		}

		if (configuration.getMunicipalitiesCoordinatesFileName() != null) {
			logger.info("Reading coordinates of municipalities: " + configuration.getMunicipalitiesCoordinatesFileName());
			municipalitiesReader.addCoordinates(municipalities, configuration.getMunicipalitiesCoordinatesFileName());
		}

		BusConnectionReader busReader = new BusConnectionReader();

		if (configuration.getBusConnectionFileName() != null) {
			logger.info("Reading bus distance: " + configuration.getBusConnectionFileName());
			busReader.updateBusDistances(municipalities, configuration.getBusConnectionFileName());
		}

		if (configuration.getBusNeighborhoodFileName() != null) {
			logger.info("Reading bus neighborhood: " + configuration.getBusNeighborhoodFileName());
			busReader.updateBusConnections(municipalities, configuration.getBusNeighborhoodFileName());
		}

		INameMapper trafficNameMapper = new SelfNameMapper();

		if (configuration.getTrafficNameMapperFileName() != null) {
			logger.info("Reading traffic name mapper: " + configuration.getTrafficNameMapperFileName());
			trafficNameMapper = new FileNameMapper(configuration.getTrafficNameMapperFileName(), 0, 2);
		}

		if (configuration.getTrafficFileName() != null) {
			logger.info("Reading traffic: " + configuration.getTrafficFileName());
			busReader.updateTraffic(municipalities, configuration.getTrafficFileName(), trafficNameMapper);
		}

		TrainConnectionReader trainReader = new TrainConnectionReader();
		if (configuration.getTrainTimeFileName() != null) {
			logger.info("Reading train time: " + configuration.getTrainTimeFileName());
			trainReader.updateTrainTimes(municipalities, configuration.getTrainTimeFileName());
		}

		if (configuration.getTrainNeighborhoodFileName() != null) {
			logger.info("Reading train neighborhood: " + configuration.getTrainNeighborhoodFileName());
			trainReader.updateTrainConnections(municipalities, configuration.getTrainNeighborhoodFileName());
		}

		List<TransportConnection> buses = new ArrayList<TransportConnection>();

		if (configuration.getBusesFileName() != null) {
			logger.info("Reading bus lines: " + configuration.getBusesFileName());

			TransportConnectionReader tcReader = new TransportConnectionReader(TransportType.BUS);
			buses = tcReader.readTransportConnections(municipalities, configuration.getBusesFileName());
		}

		List<TransportConnection> trains = new ArrayList<TransportConnection>();
		if (configuration.getTrainsFileName() != null) {
			logger.info("Reading train lines: " + configuration.getTrainsFileName());

			TransportConnectionReader tcReader = new TransportConnectionReader(TransportType.TRAIN);
			trains = tcReader.readTransportConnections(municipalities, configuration.getTrainsFileName());
		}

		TrainTransportData.getInstance().setMunicipalities(municipalities);
		BusTransportData.getInstance().setMunicipalities(municipalities);

		logger.info("Creating graph model");
		GraphModel graphModel = new GraphModel(municipalities);
		graphModel.addConnections(buses);
		graphModel.addConnections(trains);

		graphModel.computeBestConnections();

		for (Municipality mun : graphModel.getUnconnectedToPoznanMunicipalities()) {
			logger.warn(mun.getName() + " is not connected (via direct or indirect connection) to Poznań");
		}

		graphModel.putTrafficOnConnections();
		List<TransportConnection> suggestedConnections = new ArrayList<TransportConnection>();
		suggestedConnections.addAll(graphModel.getAndReduceSinglePossibilites());
		suggestedConnections.addAll(graphModel.getAndReduceOtherPossibilites());

		 SchedulePrinter sp = new SchedulePrinter(suggestedConnections);
		 sp.print("out/schedule.html");
		
		 Report report = new Report(municipalities,graphModel);
		 report.addTransportConnections(buses);
		 report.addTransportConnections(trains);
		
		 report.createReportPack("out/");

		graphModel.clearTraffic();
		graphModel.putTrafficOnConnections();

		GoogleMapVis gm = new GoogleMapVis(municipalities, graphModel);
		gm.print("out/gmapMunicipalities.html", true, false, false);
		gm.print("out/gmapBusTraffic.html", false, true, false);
		gm.print("out/gmapTrainTraffic.html", false, false, true);
	}

}
