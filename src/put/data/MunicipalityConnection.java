package put.data;

public class MunicipalityConnection {
	private boolean				connectedByBus		= false;
	private boolean				connectedByTrain	= false;
	private Double				busDistance;
	private Double				trainDistance;
	private Double				busTime;
	private Double				trainTime;
	private Double				traffic;

	private Municipality	start;
	private Municipality	stop;

	public MunicipalityConnection(Municipality start, Municipality stop) {
		this.start = start;
		this.stop = stop;
	}

	public boolean isConnectedByBus() {
		return connectedByBus;
	}

	public void setConnectedByBus(boolean connectedByBus) {
		this.connectedByBus = connectedByBus;
	}

	public boolean isConnectedByTrain() {
		return connectedByTrain;
	}

	public void setConnectedByTrain(boolean connectedByTrain) {
		this.connectedByTrain = connectedByTrain;
	}

	public Double getBusDistance() {
		return busDistance;
	}

	public void setBusDistance(Double busDistance) {
		this.busDistance = busDistance;
	}

	public Double getTrainDistance() {
		return trainDistance;
	}

	public void setTrainDistance(Double trainDistance) {
		this.trainDistance = trainDistance;
	}

	public Double getBusTime() {
		return busTime;
	}

	public void setBusTime(Double busTime) {
		this.busTime = busTime;
	}

	public Double getTrainTime() {
		return trainTime;
	}

	public void setTrainTime(Double trainTime) {
		this.trainTime = trainTime;
	}

	public Double getTraffic() {
		return traffic;
	}

	public void setTraffic(Double traffic) {
		this.traffic = traffic;
	}

	public void setTrainTime(int value) {
		setTrainTime((double) value);

	}

	public Municipality getStart() {
		return start;
	}

	public Municipality getStop() {
		return stop;
	}

}
