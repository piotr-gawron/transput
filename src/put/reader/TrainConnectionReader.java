package put.reader;

import java.io.IOException;

import org.apache.log4j.Logger;

import put.data.Municipalities;
import put.data.MunicipalityConnection;

public class TrainConnectionReader extends AbstractConnectionReader {
	Logger	logger	= Logger.getLogger(TrainConnectionReader.class);

	public void updateTrainConnections(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				connection.setConnectedByTrain(objectToBool(object));
			}
		});
	}

	public void updateTrainDistances(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				Double res = objectToDouble(object);
				if (res != null)
					connection.setTrainDistance(res);
			}
		});
	}

	public void updateTrainTimes(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				if (object instanceof String) {
					String str = (String) object;
					if (str.trim().equals("")) {
						connection.setTrainTime(null);
					} else {
						String tmp[] = str.split(":");
						if (tmp.length != 2) {
							logger.warn("Invalid train time: " + str);
						} else {
							int hour = 0;
							int minute = 0;
							try {
								hour = Integer.parseInt(tmp[0]);
								minute = Integer.parseInt(tmp[1]);
								connection.setTrainTime(hour* 60 + minute);
							} catch (NumberFormatException e ) {
								logger.warn("Problem with train time value: "+str);
							}
						}
					}
				} else if (object != null) {
					logger.warn("Wrong object type: " + object.getClass());
				}
			}
		});
	}
}
