package put.data;

public enum TransportType {
	BUS("Bus", BusTransportData.getInstance()), //
	TRAIN("Train", TrainTransportData.getInstance()), //
	UNKNOWN("Unknown", null);

	private String						commonName;
	private TransportTypeData	ttd;

	private TransportType(String commonName, TransportTypeData ttd) {
		this.commonName = commonName;
		this.ttd = ttd;
	}

	public String getCommonName() {
		return commonName;
	}

	public TransportTypeData getTransportTypeData() {
		return ttd;
	}

}
