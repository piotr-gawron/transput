package put.data;

public class LatLng {
	private double	lat;
	private double	lng;

	/**
	 * @return the lat
	 * @see #lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat
	 *          the lat to set
	 * @see #lat
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * @return the lng
	 * @see #lng
	 */
	public double getLng() {
		return lng;
	}

	/**
	 * @param lng
	 *          the lng to set
	 * @see #lng
	 */
	public void setLng(double lng) {
		this.lng = lng;
	}

	public void setLat(String string) {
		setLat(Double.parseDouble(string));
	}

	public void setLng(String string) {
		setLng(Double.parseDouble(string));
	}
}
