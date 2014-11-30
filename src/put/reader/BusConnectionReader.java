package put.reader;

import java.io.IOException;

import org.apache.log4j.Logger;

import put.data.Municipalities;
import put.data.MunicipalityConnection;

public class BusConnectionReader extends AbstractConnectionReader {
	Logger	logger	= Logger.getLogger(BusConnectionReader.class);

	public void updateBusConnections(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				connection.setConnectedByBus(objectToBool(object));
			}
		});
	}

	public void updateBusDistances(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				Double res = objectToDouble(object);
				if (res != null)
					connection.setBusDistance(res);
			}
		});
	}

	public void updateBusTimes(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				Double res = objectToDouble(object);
				if (res != null)
					connection.setBusTime(res);
			}
		});
	}

}
