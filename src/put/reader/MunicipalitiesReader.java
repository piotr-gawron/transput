package put.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import put.Configuration;
import put.data.LatLng;
import put.data.Municipalities;
import put.data.Municipality;
import put.data.OtherMunicipality;

public class MunicipalitiesReader {
	Logger				logger				= Logger.getLogger(MunicipalitiesReader.class);

	Configuration	configuration	= Configuration.getConfiguration();

	public Municipalities readDataFromFile(String fileName) throws IOException, InvalidFileException {
		Municipality municipalities[];
		List<Municipality> list = new ArrayList<Municipality>();
		Set<String> names = new HashSet<String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));
		int counter = 0;
		try {
			String line = br.readLine();

			while (line.length() > 0 && line.charAt(0) == 65279) {
				line = line.substring(1);
			}
			while (line != null) {
				counter++;
				if (names.contains(line.trim().toLowerCase())) {
					throw new InvalidFileException("Names cannot duplicate: " + line);
				}
				names.add(line.trim().toLowerCase());
				list.add(new Municipality(line.trim()));
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		logger.debug(counter + " municipalities were read");
		municipalities = new Municipality[list.size()];
		int index = 0;
		for (Municipality municipality : list) {
			municipalities[index++] = municipality;
		}

		return new Municipalities(municipalities);
	}

	public List<OtherMunicipality> readOtherMunicipalities(String fileName) throws IOException {
		List<OtherMunicipality> result = new ArrayList<OtherMunicipality>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));
		try {
			String line = br.readLine();

			while (line.length() > 0 && line.charAt(0) == 65279) {
				line = line.substring(1);
			}
			while (line != null) {
				result.add(new OtherMunicipality(line.trim()));
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		logger.debug(result.size()+" other municipalities read.");
		return result;
	}

	public void addCoordinates(Municipalities municipalities, String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), configuration.getEncoding()));
		int counter = 0;
		try {
			String line = br.readLine();

			while (line.length() > 0 && line.charAt(0) == 65279) {
				line = line.substring(1);
			}
			while (line != null) {
				counter++;
				String[] tmp = line.split("\t");
				if (tmp.length != 2) {
					logger.warn("[File: " + fileName + "][line: " + counter + "] Invalid format. Skipping");
					line = br.readLine();
					continue;
				}
				String name = tmp[0];
				Municipality municipality = municipalities.getMunicipalityByName(name);
				if (municipality == null) {
					logger.warn("[File: " + fileName + "][line: " + counter + "] Unknown municipality: " + name);
					line = br.readLine();
					continue;
				}

				String[] coord = tmp[1].split(",");
				if (coord.length != 2) {
					logger.warn("[File: " + fileName + "][line: " + counter + "] Invalid format. Skipping");
					line = br.readLine();
					continue;
				}

				LatLng coordinates = new LatLng();
				coordinates.setLat(coord[0]);
				coordinates.setLng(coord[1]);

				municipality.setCoordinates(coordinates);
				line = br.readLine();
			}
		} finally {
			br.close();
		}

	}

}
