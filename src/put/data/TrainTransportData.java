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
		return Configuration.getConfiguration().getTrainSpeed();
	}

	@Override
	public int timeBetween(ConnectionStop from, ConnectionStop to) {
		MunicipalityConnection connection = municipalities.getConnection(from.getMunicipality(), to.getMunicipality());
		if (connection.getTrainDistance() == null) {
			logger.warn("No train distance information between " + from.getMunicipality().getName() + " and " + to.getMunicipality().getName());
		} else if (connection.getTrainDistance() < 1.0) {
			logger.warn("Train distance information between " + from.getMunicipality().getName() + " and " + to.getMunicipality().getName() + " is strange: "
					+ connection.getTrainDistance());
		}
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

	@Override
	public double distanceBetween(Municipality from, Municipality to) {
		MunicipalityConnection connection = municipalities.getConnection(from, to);
		double distance = 100000;
		if (connection == null || connection.getTrainDistance() == null) {

			if (connection.getBusDistance() != null) {
				distance = connection.getBusDistance();
			}
			logger.warn("Unknown distance between " + from.getName() + " and " + to.getName() + ". Using: " + distance + " km.");
		} else {
			distance = connection.getTrainDistance();
		}
		return distance;
	}

	@Override
	public double kmCost() {
		return Configuration.getConfiguration().getTrainKmCost();
	}

	@Override
	public double kmIncome() {
		return Configuration.getConfiguration().getTrainKmPassengerIncome();
	}

}
