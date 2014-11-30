package put.data;

public interface TransportTypeData {

	public double averageSpeed();
	public double averageSize();
	public int timeBetween(ConnectionStop from, ConnectionStop to);
	public double distanceBetween(Municipality first, Municipality last);
	public double kmCost();
}
