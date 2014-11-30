package put.data;

import org.apache.log4j.Logger;

import put.Configuration;

public class TrainTransportData implements TransportTypeData {
	Logger														logger					= Logger.getLogger(TrainTransportData.class);
	private Municipalities						municipalities	= null;

	private static TrainTransportData	instance				= null;

	public static TrainTransportData getInstance() {
		if (instance == null)
			instance = new TrainTransportData();
		return instance;
	}

	private TrainTransportData() {

	}

	@Override
	public double averageSpeed() {
		return 60;
	}

	@Override
	public int timeBetween(ConnectionStop from, ConnectionStop to) {
		MunicipalityConnection connection = municipalities.getConnection(from.getMunicipality(), to.getMunicipality());
		if (connection.getTrainTime() != null) {
			double result = connection.getTrainTime();
			return (int) result;
		} else {
			logger.warn("No information about train time distance between " + from.getMunicipality().getName() + " and " + to.getMunicipality().getName());
			double distance = 0;
			if (connection.getTrainDistance() != null)
				distance = connection.getTrainDistance();
			else
				distance = connection.getBusDistance();
			connection.setTrainTime(distance * 60 / averageSpeed());
			return (int) (distance * 60 / averageSpeed());
		}
	}

	public Municipalities getMunicipalities() {
		return municipalities;
	}

	public void setMunicipalities(Municipalities municipalities) {
		this.municipalities = municipalities;
	}

	@Override
	public double averageSize() {
		return Configuration.getConfiguration().getTrainSize();
	}

}
