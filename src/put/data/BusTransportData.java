package put.data;

import org.apache.log4j.Logger;

import put.Configuration;

public class BusTransportData implements TransportTypeData {
	Logger logger = Logger.getLogger(BusTransportData.class);

	private static BusTransportData	instance				= null;
	private Municipalities					municipalities	= null;

	public static BusTransportData getInstance() {
		if (instance == null)
			instance = new BusTransportData();
		return instance;
	}

	private BusTransportData() {

	}

	@Override
	public double averageSpeed() {
		return 40;
	}

	@Override
	public int timeBetween(ConnectionStop from, ConnectionStop to) {
		MunicipalityConnection connection = municipalities.getConnection(from.getMunicipality(), to.getMunicipality());
		double distance = 100000;
		if (connection==null || connection.getBusDistance()==null) {
			logger.warn("Unknown distance between "+from.getMunicipality().getName()+" and "+to.getMunicipality().getName());
		} else {
			distance = connection.getBusDistance();
		}
		return (int) (distance * 60 / averageSpeed());
	}

	public Municipalities getMunicipalities() {
		return municipalities;
	}

	public void setMunicipalities(Municipalities municipalities) {
		this.municipalities = municipalities;
	}
	
	@Override
	public double averageSize() {
		return Configuration.getConfiguration().getBusSize();
	}

}
