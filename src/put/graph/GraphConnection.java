package put.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import put.data.InvalidArgumentException;
import put.data.Municipality;
import put.data.TransportConnection;
import put.data.TransportType;

public class GraphConnection {
	Logger										logger						= Logger.getLogger(GraphConnection.class);
	private Municipality			startMunicipality;
	private Municipality			endMunicipality;

	private List<TransportConnection>	availableVehicles	= new ArrayList<TransportConnection>();

	Integer										fastestConnection	= null;

	private Double						traffic						= null;

	private TransportType			type;
	
	private List<TrafficSource> trafficSources  = new ArrayList<TrafficSource>();

	public GraphConnection(Municipality from, Municipality to, TransportType type) {
		this.startMunicipality = from;
		this.endMunicipality = to;
		this.type = type;
	}

	public void addTransportConnection(TransportConnection transportConnection) {
		availableVehicles.add(transportConnection);
		try {
			Integer fc = fastestConnection;
			if (fc == null)
				fc = Integer.MAX_VALUE;
			fc = Math.min(fc, transportConnection.getTimeBetweenStops(startMunicipality, endMunicipality));
			fastestConnection = fc;
		} catch (InvalidArgumentException e) {
			logger.error("Problem with line: " + transportConnection.getName() + "; ");
			throw e;
		}
	}

	public Municipality getStartMunicipality() {
		return startMunicipality;
	}

	public Municipality getEndMunicipality() {
		return endMunicipality;
	}

	public Integer getFastestConnection() {
		return fastestConnection;
	}

	public TransportType getType() {
		return type;
	}

	public void setType(TransportType type) {
		this.type = type;
	}

	public Double getTraffic() {
		return traffic;
	}

	public void setTraffic(Double traffic) {
		this.traffic = traffic;
	}

	public List<TransportConnection> getAvailableVehicles() {
		return availableVehicles;
	}

	public void setAvailableVehicles(List<TransportConnection> availableVehicles) {
		this.availableVehicles = availableVehicles;
	}

	public void addTrafficSource(TrafficSource ts) {
		Double currentTraffic = getTraffic();
		if (currentTraffic == null)
			currentTraffic = 0.0;
		currentTraffic += ts.getAmount();
		setTraffic(currentTraffic);
		trafficSources.add(ts);
	}

	public List<TrafficSource> getTrafficSources() {
		return trafficSources;
	}

	public void setTrafficSources(List<TrafficSource> trafficSources) {
		this.trafficSources = trafficSources;
	}

}
