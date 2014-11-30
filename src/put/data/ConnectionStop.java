package put.data;

public class ConnectionStop {
	public ConnectionStop(Municipality municipality2, Integer hour2, Integer minute2) {
		this.municipality = municipality2;
		this.hour = hour2;
		this.minute = minute2;
	}

	public ConnectionStop(Municipality municipality2, String hour2, String minute2) {
		this.municipality = municipality2;
		if (hour2 != null && !hour2.equals(""))
			this.hour = Integer.parseInt(hour2);
		if (minute2 != null && !minute2.equals(""))
			this.minute = Integer.parseInt(minute2);
	}

	public ConnectionStop(Municipality municipality2) {
		this.municipality = municipality2;
		this.hour = null;
		this.minute = null;
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	private Municipality	municipality;
	private Integer				hour;
	private Integer				minute;
}
