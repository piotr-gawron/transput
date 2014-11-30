package put.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Municipalities {
	Logger														logger							= Logger.getLogger(Municipalities.class);

	private Municipality							municipalities[];

	private Map<String, Municipality>	municipalityByName;
	private Map<String, Integer>			indexByName;

	private MunicipalityConnection		connections[][];

	private List<OtherMunicipality>		otherMunicipalities	= new ArrayList<OtherMunicipality>();

	public Municipalities(Municipality municipalities[]) {
		this.municipalities = municipalities;
		municipalityByName = new HashMap<String, Municipality>();
		indexByName = new HashMap<String, Integer>();
		int index = 0;
		for (Municipality municipality : municipalities) {
			municipalityByName.put(municipality.getName().toLowerCase(), municipality);
			indexByName.put(municipality.getName().toLowerCase(), index++);
		}

		connections = new MunicipalityConnection[index][];

		for (int i = 0; i < index; i++) {
			connections[i] = new MunicipalityConnection[index];
			for (int j = 0; j < index; j++) {
				connections[i][j] = new MunicipalityConnection(municipalities[i], municipalities[j]);
			}
		}
	}

	public void addOtherMunicipality(OtherMunicipality otherMunicipality) throws InvalidArgumentException {
		for (Municipality municipality : municipalities) {
			if (municipality.getName().toLowerCase().trim().equals(otherMunicipality.getName().toLowerCase().trim())) {
				logger.warn(otherMunicipality.getName() + " cannot exist in the province and outside it!");
				return;
			}
		}
		// logger.debug("ADD: \""+otherMunicipality.getName()+"\"");
		if (municipalityByName.get(otherMunicipality.getName()) != null) {
			throw new InvalidArgumentException("Municipality " + otherMunicipality + " is already in the set");
		}
		otherMunicipalities.add(otherMunicipality);
		municipalityByName.put(otherMunicipality.getName().toLowerCase(), otherMunicipality);
	}

	public Municipality getMunicipality(int index) {
		return municipalities[index];
	}

	public Municipality getMunicipalityByName(String name) {
		Municipality result = municipalityByName.get(name.toLowerCase());
		if (result == null) {
			for (Municipality municipality : municipalities) {
				if (municipality.getName().toLowerCase().startsWith(name.toLowerCase().trim())) {
					logger.warn("Couldn't find: \"" + name + "\". Using: \"" + municipality.getName() + "\"");
					return municipality;
				}
			}
		}
		return municipalityByName.get(name.toLowerCase());
	}

	public Integer getIndexByName(String name) {
		return indexByName.get(name.trim().toLowerCase());
	}

	public MunicipalityConnection getConnection(int startIndex, int endIndex) {
		return connections[startIndex][endIndex];
	}

	public int getMunicipalityCount() {
		return connections.length;
	}

	public void addOtherMunicipalities(List<OtherMunicipality> readOtherMunicipalities) {
		for (OtherMunicipality otherMunicipality : readOtherMunicipalities) {
			try {
				addOtherMunicipality(otherMunicipality);
			} catch (InvalidArgumentException e) {
				logger.warn(e.getMessage() + ". Ignored");
			}
		}
	}

	public Integer getIndexByMunicipality(Municipality municipality) {
		return getIndexByName(municipality.getName());
	}

	public boolean isPrimarayMunicipality(Municipality municipality) {
		return (getIndexByMunicipality(municipality) != null);
	}

	public MunicipalityConnection getConnection(Municipality municipality, Municipality municipality2) {
		if (municipality instanceof OtherMunicipality)
			throw new InvalidArgumentException(municipality.getName() + " doesn't exist in our network");
		if (municipality2 instanceof OtherMunicipality)
			throw new InvalidArgumentException(municipality2.getName() + " doesn't exist in our network");
		return getConnection(getIndexByMunicipality(municipality), getIndexByMunicipality(municipality2));
	}

	public List<OtherMunicipality> getOtherMunicipalities() {
		return otherMunicipalities;
	}

}
