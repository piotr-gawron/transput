package put.data;

import org.apache.log4j.Logger;

public class Municipality {
	Logger					logger			= Logger.getLogger(Municipality.class);

	private String	name;

	private LatLng	coordinates	= null;

	public Municipality(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the coordinates
	 * @see #coordinates
	 */
	public LatLng getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates
	 *          the coordinates to set
	 * @see #coordinates
	 */
	public void setCoordinates(LatLng coordinates) {
		this.coordinates = coordinates;
	}

}
