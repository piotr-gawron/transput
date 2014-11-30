package put.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import put.graph.TrafficSource;

public class TransportConnection {
	public static class TransportConnectionNameComparator implements Comparator<TransportConnection> {

		@Override
		public int compare(TransportConnection arg0, TransportConnection arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}

	}

	private String								name;

	TransportType									type;

	private List<ConnectionStop>	stops						= new ArrayList<ConnectionStop>();

	private List<TrafficSource>		trafficSources	= new ArrayList<TrafficSource>();

	public TransportConnection(String name, TransportType type) {
		this.name = name;
		this.type = type;
	}

	public TransportConnection(TransportConnection connection) {
		this.name = connection.getName();
		this.type = connection.getType();
		for (ConnectionStop stop : connection.getStops()) {
			this.addStop(new ConnectionStop(stop.getMunicipality()));
		}
	}

	public List<ConnectionStop> getStops() {
		return stops;
	}

	public void addStop(ConnectionStop stop) {
		stops.add(stop);
	}

	public void addStop(Municipality municipality, int hour, int minute) {
		stops.add(new ConnectionStop(municipality, hour, minute));
	}

	public void addStop(Municipality municipality, String hour, String minute) {
		stops.add(new ConnectionStop(municipality, hour, minute));
	}

	public ConnectionStop getStop(int i) {
		return stops.get(i);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTimeBetweenStops(Municipality startMunicipality, Municipality endMunicipality) {
		ConnectionStop from = null;
		ConnectionStop to = null;

		for (ConnectionStop connectionStop : stops) {
			if (connectionStop.getMunicipality().equals(startMunicipality)) {
				from = connectionStop;
				to = null;
			}
			if (connectionStop.getMunicipality().equals(endMunicipality)) {
				to = connectionStop;
			}
			if (from != null && to != null)
				break;
		}
		if (from == null)
			throw new InvalidArgumentException(name + " doesn't pass through: " + startMunicipality.getName() + ". Or the order is wrong.");

		if (to == null)
			throw new InvalidArgumentException(name + " doesn't pass through: " + endMunicipality.getName() + ". Or the order is wrong.");

		if (from.getHour() != null && from.getMinute() != null && to.getHour() != null && to.getMinute() != null) {
			return (to.getHour() - from.getHour()) * 60 + to.getMinute() - from.getMinute();
		} else {
			int result = 0;
			boolean compute = false;
			ConnectionStop lastStop = null;
			for (ConnectionStop connectionStop : stops) {
				if (compute && !(connectionStop.getMunicipality() instanceof OtherMunicipality))
					result += type.getTransportTypeData().timeBetween(lastStop, connectionStop);
				if (connectionStop.equals(from))
					compute = true;
				if (connectionStop.equals(to))
					compute = false;
				if (!(connectionStop.getMunicipality() instanceof OtherMunicipality)) {
					lastStop = connectionStop;
				}
			}
			return result;
		}
	}

	public TransportType getType() {
		return type;
	}

	public void addTrafficSource(TrafficSource ts) {
		trafficSources.add(ts);
	}

	public List<TrafficSource> getTrafficSources() {
		return trafficSources;
	}

	public void setTrafficSources(List<TrafficSource> trafficSources) {
		this.trafficSources = trafficSources;
	}

	public double getDistanceBetweenStops(Municipality first, Municipality last) {
		return type.getTransportTypeData().distanceBetween(first, last);
	}
}
