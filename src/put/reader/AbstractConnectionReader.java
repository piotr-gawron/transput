package put.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import put.Configuration;
import put.data.Municipalities;
import put.data.MunicipalityConnection;

public class AbstractConnectionReader {
	private static Logger	logger				= Logger.getLogger(AbstractConnectionReader.class);

	Configuration					configuration	= Configuration.getConfiguration();

	protected static String objectToString(Object object) {
		if (object instanceof String)
			return (String) object;
		logger.warn("Invalid class type " + object.getClass());
		return null;
	}

	protected static Double objectToDouble(Object object) {
		if (object instanceof Double)
			return (Double) object;
		if (object instanceof String) {
			String str = (String) object;
			if (str.trim().equals(""))
				return null;
			try {
				return Double.valueOf(str.replace(',', '.'));
			} catch (NumberFormatException e) {
				logger.warn("Uknown string for double value: " + str);
				return null;
			}
		}
		logger.warn("Invalid class type " + object.getClass());
		return null;
	}

	protected static Integer objectToInteger(Object object) {
		if (object instanceof Integer)
			return (Integer) object;
		if (object instanceof String) {
			String str = (String) object;
			if (str.trim().equals(""))
				return null;
			try {
				return Integer.valueOf(str);
			} catch (NumberFormatException e) {
				logger.warn("Uknown string for double value: " + str);
				return null;
			}
		}
		logger.warn("Invalid class type " + object.getClass());
		return null;
	}

	protected static Boolean objectToBool(Object object) {
		if (object instanceof Boolean)
			return (Boolean) object;
		if (object instanceof String) {
			String str = (String) object;
			if (str.equalsIgnoreCase("TRUE"))
				return true;
			if (str.equalsIgnoreCase("1"))
				return true;
			return false;
		}
		logger.warn("Invalid class type " + object.getClass());
		return null;
	}

	public void updateConnections(Municipalities municipalities, String fileName, IConnectionUpdater updater) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, updater, new SelfNameMapper(), true);
	}

	public void updateConnections(Municipalities municipalities, String fileName, IConnectionUpdater updater, INameMapper nameMapper, boolean symetric)
			throws IOException, InvalidFileException {

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));

		int counter = municipalities.getMunicipalityCount();
		Set<String> uniqueNames = new HashSet<String>();

		try {
			String line = br.readLine();

			String names[] = line.split("\t");
			Integer ids[] = new Integer[counter];

			for (int i = 1; i <= counter; i++) {
				String name = names[i].trim();
				if (uniqueNames.contains(name)) {
					throw new InvalidFileException("Municipality " + name + " found multiple times.");
				}
				uniqueNames.add(name);

				String mappedName = nameMapper.getValue(name);
				if (mappedName == null) {
					throw new InvalidFileException("Cannot map municipality: \"" + name + "\"");
				}

				ids[i - 1] = municipalities.getIndexByName(mappedName);
				if (ids[i - 1] == null) {
					throw new InvalidFileException("Unknown municipality: \"" + mappedName + "\"");
				}
			}

			uniqueNames.clear();
			line = br.readLine();
			while (line != null) {
				String values[] = line.split("\t", -1);

				for (int i = 0; i < values.length; i++) {
					values[i] = values[i].trim();
				}
				if (uniqueNames.contains(values[0])) {
					throw new InvalidFileException("Municipality " + values[0] + " found multiple times.");
				}
				String mappedName = nameMapper.getValue(values[0]);
				Integer id = municipalities.getIndexByName(mappedName);
				if (id == null)
					throw new InvalidFileException("Unknown municipality: " + mappedName);
				for (int i = 1; i <= counter; i++) {
					updater.updateMunicipalityConnection(municipalities.getConnection(id, ids[i - 1]), values[i]);
					if (symetric)
						updater.updateMunicipalityConnection(municipalities.getConnection(ids[i - 1], id), values[i]);
				}
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	public void updateTraffic(Municipalities municipalities, String fileName, INameMapper nameMapper) throws IOException, InvalidFileException {
		updateConnections(municipalities, fileName, new IConnectionUpdater() {
			@Override
			public void updateMunicipalityConnection(MunicipalityConnection connection, Object object) {
				Double traffic = connection.getTraffic();
				if (traffic == null)
					connection.setTraffic(objectToDouble(object));
				else
					connection.setTraffic(traffic + objectToDouble(object));
			}
		}, nameMapper, false);
	}

}
