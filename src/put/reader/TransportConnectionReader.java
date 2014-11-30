package put.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import put.Configuration;
import put.data.Municipalities;
import put.data.Municipality;
import put.data.TransportConnection;
import put.data.TransportType;

public class TransportConnectionReader {
	private static Logger	logger				= Logger.getLogger(TransportConnectionReader.class);

	Configuration					configuration	= Configuration.getConfiguration();

	private TransportType	type					= null;

	public TransportConnectionReader(TransportType type) {
		this.type = type;
	}

	public TransportConnection readTransportConnectionForOneBusPerFile(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {

		TransportConnection connection = new TransportConnection("Unknown", TransportType.BUS);

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));

		try {
			String line = br.readLine();

			while (line != null) {
				String elements[] = line.split("\t");
				Municipality municipality = municipalities.getMunicipalityByName(elements[0]);
				String hour = elements[1].split(":")[0];
				String minute = elements[1].split(":")[1];

				connection.addStop(municipality, hour, minute);

				line = br.readLine();
			}
		} finally {
			br.close();
		}
		return connection;
	}

	public List<TransportConnection> readTransportConnections(Municipalities municipalities, String fileName) throws IOException, InvalidFileException {
		List<TransportConnection> result = new ArrayList<TransportConnection>();

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));

		try {
			String line = br.readLine();

			line = br.readLine();
			while (line != null) {

				String line2 = br.readLine();

				try {
					// result.addAll(getTransportConnectionsForLine(line, line2,
					// municipalities));
					result.addAll(getTransportConnectionsForLine(line, municipalities));
				} catch (InvalidFileException e) {
					logger.warn(e.getMessage(),e);
				}

				line = br.readLine();
			}
		} finally {
			br.close();
		}
		return result;
	}

	public List<TransportConnection> getTransportConnectionsForLine(String line, String line2, Municipalities municipalities) throws InvalidFileException {
		List<TransportConnection> result = new ArrayList<TransportConnection>();

		String row1[] = line.split("\t", -1);
		String row2[] = line2.split("\t", -1);

		if (row1.length > configuration.getMaxColumns())
			throw new InvalidFileException("Invalid number of fields. Expected " + configuration.getMaxColumns() + " but " + row1.length + " found. Line: \"" + line
					+ "\"");

		if (row2.length > configuration.getMaxColumns())
			throw new InvalidFileException("Invalid number of fields. Expected " + configuration.getMaxColumns() + " but " + row2.length + " found. Line: \"" + line2
					+ "\"");

		String name = row1[configuration.getNameColumn()];

		List<Municipality> stops = new ArrayList<Municipality>();
		String lastStop = "";

		for (int i = configuration.getNameColumn() + 1; i < configuration.getMaxColumns(); i++) {
			boolean empty = false;
			if (i >= row1.length || row1[i] == null || row1[i].trim().equals("")) {
				empty = true;
			}
			boolean empty2 = false;
			if (i >= row2.length || row2[i] == null || row2[i].trim().equals("")) {
				empty2 = true;
			}

			if (empty != empty2) {
				throw new InvalidFileException("Inconsistency in description of line: \"" + name + ". Row1: \"" + row1[i] + "\", whereas row2: \"" + row2[i] + "\"");
			}
			if (!row2[i].trim().equalsIgnoreCase(lastStop)) {
				if (row2[i].trim().equals("")) {
					lastStop = null;
				} else {
					if (lastStop == null) {
						throw new InvalidFileException("Invalid set of stops for line: " + name + ". Stop " + row2[i] + " found after empty stop.");
					}
					Municipality municipality = municipalities.getMunicipalityByName(row2[i].trim());
					if (municipality == null) {
						throw new InvalidFileException("Unknown municipality: \"" + row2[i].trim() + "\". Line: \"" + name + "\"");
					}
					stops.add(municipality);
					lastStop = row2[i].trim();
				}
			}
		}

		boolean lineActive = false;
		for (int i = 0; i < configuration.getHoursColumns(); i++) {
			if (row1[i] != null && !row1[i].trim().equals("")) {
				lineActive = true;
				TransportConnection connection = new TransportConnection(name, type);
				String time[] = row1[i].split(":");
				if (time.length < 2) {
					logger.warn("Unknown start hour \"" + row1[i] + "\", for " + type.getCommonName() + ": " + name);
					continue;
				}
				connection.addStop(stops.get(0), time[0], time[1]);
				for (int j = 1; j < stops.size(); j++)
					connection.addStop(stops.get(j), null, null);
				result.add(connection);
			}
		}

		if (!lineActive) {
			TransportConnection connection = new TransportConnection(name, type);
			for (int j = 0; j < stops.size(); j++)
				connection.addStop(stops.get(j), null, null);
			result.add(connection);
		}
		lineActive = false;

		for (int i = 0; i < configuration.getHoursColumns(); i++) {
			if (row2[i] != null && !row2[i].trim().equals("")) {
				TransportConnection connection = new TransportConnection(name + " (2)", type);
				String time[] = row2[i].split(":");
				if (time.length < 2) {
					logger.warn("Unknown start hour \"" + row2[i] + "\", for " + type.getCommonName() + ": " + name);
					continue;
				}
				connection.addStop(stops.get(stops.size() - 1), time[0], time[1]);
				for (int j = stops.size() - 2; j >= 0; j--)
					connection.addStop(stops.get(j), null, null);
				result.add(connection);
			}
		}

		if (!lineActive) {
			TransportConnection connection = new TransportConnection(name + " (2)", type);
			for (int j = stops.size() - 1; j >= 0; j--)
				connection.addStop(stops.get(j), null, null);
			result.add(connection);
		}

		return result;
	}

	public List<TransportConnection> getTransportConnectionsForLine(String line, Municipalities municipalities) throws InvalidFileException {
		List<TransportConnection> result = new ArrayList<TransportConnection>();

		String row[] = line.split("\t", -1);

		String name = row[configuration.getNameColumn()];

		List<Municipality> stops = new ArrayList<Municipality>();
		String lastStop = "";

		for (int i = configuration.getNameColumn() + 1; i < row.length; i++) {
			if (!row[i].trim().equalsIgnoreCase(lastStop)) {
				if (row[i].trim().equals("")) {
					lastStop = null;
				} else {
					if (lastStop == null) {
						throw new InvalidFileException("Invalid set of stops for line: " + name + ". Stop " + row[i] + " found after empty stop.");
					}
					Municipality municipality = municipalities.getMunicipalityByName(row[i].trim());
					if (municipality == null) {
						throw new InvalidFileException("Unknown municipality: \"" + row[i].trim() + "\". Line: \"" + name + "\"");
					}
					stops.add(municipality);
					lastStop = row[i].trim();
				}
			}
		}
		if (stops.size() > 1) {
			boolean lineActive = false;
			for (int i = 0; i < configuration.getHoursColumns(); i++) {
				if (row[i] != null && !row[i].trim().equals("")) {
					lineActive = true;
					TransportConnection connection = new TransportConnection(name, type);
					String time[] = row[i].split(":");
					if (time.length < 2) {
						logger.warn("Unknown start hour \"" + row[i] + "\", for " + type.getCommonName() + ": " + name);
						continue;
					}
					try {
						Integer.parseInt(time[0]);
						Integer.parseInt(time[1]);
					} catch (NumberFormatException e) {
						logger.warn("Invalid start hour \"" + row[i] + "\", for " + type.getCommonName() + ": " + name);
						continue;
					}
					connection.addStop(stops.get(0), time[0], time[1]);
					for (int j = 1; j < stops.size(); j++)
						connection.addStop(stops.get(j), null, null);
					result.add(connection);
				}
			}
			if (!lineActive) {
				TransportConnection connection = new TransportConnection(name, type);
				for (int j = 0; j < stops.size(); j++)
					connection.addStop(stops.get(j), null, null);
				result.add(connection);
			}

			lineActive = false;
			for (int i = 0; i < configuration.getHoursColumns(); i++) {
				if (row[i] != null && !row[i].trim().equals("")) {
					TransportConnection connection = new TransportConnection(name + " (2)", type);
					String time[] = row[i].split(":");
					if (time.length < 2) {
						logger.warn("Unknown start hour \"" + row[i] + "\", for " + type.getCommonName() + ": " + name);
						continue;
					}
					try {
						Integer.parseInt(time[0]);
						Integer.parseInt(time[1]);
					} catch (NumberFormatException e) {
						logger.warn("Invalid start hour \"" + row[i] + "\", for " + type.getCommonName() + ": " + name);
						continue;
					}
					connection.addStop(stops.get(stops.size() - 1), time[0], time[1]);
					for (int j = stops.size() - 2; j >= 0; j--)
						connection.addStop(stops.get(j), null, null);
					result.add(connection);
					lineActive = true;
				}
			}
			if (!lineActive) {
				TransportConnection connection = new TransportConnection(name + " (2)", type);
				for (int j = stops.size() - 1; j >= 0; j--)
					connection.addStop(stops.get(j), null, null);
				result.add(connection);
			}
		} else {
			logger.warn(type.getCommonName() + " \"" + name + "\" doesn't have enough stops (<2)");
		}
		return result;
	}

}
