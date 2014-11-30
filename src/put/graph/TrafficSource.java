package put.graph;

import put.data.Municipality;
import put.data.MunicipalityConnection;

public class TrafficSource {
	private double									amount;
	private MunicipalityConnection	connection;
	private Municipality						start;
	private Municipality						stop;

	public TrafficSource(TrafficSource source) {
		this.amount = source.amount;
		this.connection = source.connection;
		this.start = source.start;
		this.stop = source.stop;
	}

	public TrafficSource() {
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public MunicipalityConnection getConnection() {
		return connection;
	}

	public void setConnection(MunicipalityConnection connection) {
		this.connection = connection;
	}

	public Municipality getStart() {
		return start;
	}

	public void setStart(Municipality start) {
		this.start = start;
	}

	public Municipality getStop() {
		return stop;
	}

	public void setStop(Municipality stop) {
		this.stop = stop;
	}
}
