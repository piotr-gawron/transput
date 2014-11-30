package put.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import put.Configuration;
import put.data.ConnectionStop;
import put.data.Municipalities;
import put.data.Municipality;
import put.data.MunicipalityConnection;
import put.data.OtherMunicipality;
import put.data.TransportConnection;
import put.data.TransportType;
import put.exception.InvalidStateException;

public class GraphModel {
	Logger										logger							= Logger.getLogger(GraphModel.class);

	Municipalities						municipalities			= null;

	List<TransportConnection>	connections					= new ArrayList<TransportConnection>();

	public List<GraphConnection>			successorEdges[]		= null;
	List<GraphConnection>			predecessorEdges[]	= null;

	List<GraphConnection>			bestConnections[][];

	Configuration							configuration;

	@SuppressWarnings("unchecked")
	public GraphModel(Municipalities municipalities) {
		configuration = Configuration.getConfiguration();
		this.municipalities = municipalities;
		successorEdges = new List[municipalities.getMunicipalityCount()];
		for (int i = 0; i < successorEdges.length; i++)
			successorEdges[i] = new ArrayList<GraphConnection>();
		predecessorEdges = new List[municipalities.getMunicipalityCount()];
		for (int i = 0; i < successorEdges.length; i++)
			predecessorEdges[i] = new ArrayList<GraphConnection>();

		bestConnections = new List[municipalities.getMunicipalityCount()][];
		for (int i = 0; i < municipalities.getMunicipalityCount(); i++)
			bestConnections[i] = new List[municipalities.getMunicipalityCount()];
	}

	public void addConnections(List<TransportConnection> connection) {
		for (TransportConnection transportConnection : connection) {
			addConnection(transportConnection);
		}
	}

	private void addConnection(TransportConnection transportConnection) {
		for (int i = 0; i < transportConnection.getStops().size(); i++) {
			if (!(transportConnection.getStop(i).getMunicipality() instanceof OtherMunicipality)) {
				for (int j = i + 1; j < transportConnection.getStops().size(); j++) {
					if (!(transportConnection.getStop(j).getMunicipality() instanceof OtherMunicipality)) {
						if (!transportConnection.getStop(i).getMunicipality().equals(transportConnection.getStop(j).getMunicipality())) {
							GraphConnection graphConnection = getGraphConnectionBetween(
									transportConnection.getStop(i), transportConnection.getStop(j), transportConnection.getType());
							graphConnection.addTransportConnection(transportConnection);
							graphConnection = getPredecessorGraphConnectionBetween(
									transportConnection.getStop(i), transportConnection.getStop(j), transportConnection.getType());
							graphConnection.addTransportConnection(transportConnection);
						}
					}
				}
			}
		}
	}

	private GraphConnection getPredecessorGraphConnectionBetween(ConnectionStop from, ConnectionStop to, TransportType type) {
		GraphConnection result = null;
		int end = municipalities.getIndexByMunicipality(to.getMunicipality());
		for (GraphConnection connection : predecessorEdges[end]) {
			if (connection.getStartMunicipality().equals(from.getMunicipality()) && connection.getType().equals(type)) {
				result = connection;
			}
		}
		if (result == null) {
			result = new GraphConnection(from.getMunicipality(), to.getMunicipality(), type);
			predecessorEdges[end].add(result);
		}
		return result;
	}

	private GraphConnection getGraphConnectionBetween(ConnectionStop from, ConnectionStop to, TransportType type) {
		GraphConnection result = null;

		int start = municipalities.getIndexByMunicipality(from.getMunicipality());
		for (GraphConnection connection : successorEdges[start]) {
			if (connection.getEndMunicipality().equals(to.getMunicipality()) && connection.getType().equals(type)) {
				result = connection;
			}
		}
		if (result == null) {
			result = new GraphConnection(from.getMunicipality(), to.getMunicipality(), type);
			successorEdges[start].add(result);
		}
		return result;
	}

	class DijkstraElement implements Comparable<DijkstraElement> {
		int						municipality;
		int						previous;
		int						distance;
		TransportType	type;
		TransportType	previousType;

		@Override
		public int compareTo(DijkstraElement arg0) {
			return Integer.compare(distance, arg0.distance);
		}
	}

	class PreviousElement {
		int						municipality;
		TransportType	type;

		public PreviousElement(int previous) {
			municipality = previous;
		}

		public PreviousElement(int previous, TransportType previousType) {
			municipality = previous;
			type = previousType;
		}
	}

	public void findShortestPath(Municipality from) {
		int start = municipalities.getIndexByMunicipality(from);

		PreviousElement previous[][] = new PreviousElement[TransportType.values().length][];
		for (TransportType tt : TransportType.values()) {
			previous[tt.ordinal()] = new PreviousElement[municipalities.getMunicipalityCount()];
		}

		int minDistance[][] = new int[TransportType.values().length][];
		for (TransportType tt : TransportType.values()) {
			minDistance[tt.ordinal()] = new int[municipalities.getMunicipalityCount()];
		}
		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
			for (TransportType tt : TransportType.values()) {
				minDistance[tt.ordinal()][i] = Integer.MAX_VALUE;
			}
		}
		for (TransportType tt : TransportType.values()) {
			minDistance[tt.ordinal()][start] = 0;
		}

		PriorityQueue<DijkstraElement> queue = new PriorityQueue<GraphModel.DijkstraElement>();

		for (TransportType tt : TransportType.values()) {
			DijkstraElement element = new DijkstraElement();
			element.distance = 0;
			element.municipality = start;
			element.previous = -1;
			element.type = tt;
			element.previousType = null;
			queue.add(element);
		}
		while (!queue.isEmpty()) {
			DijkstraElement element = queue.peek();
			queue.remove();
			// logger.debug("Get: " +
			// municipalities.getMunicipality(element.municipality).getName() + ", " +
			// element.type
			// + ((element.previous >= 0) ? " from " +
			// municipalities.getMunicipality(element.previous).getName() : "") +
			// " dist: " + element.distance);
			if (minDistance[element.type.ordinal()][element.municipality] == element.distance) {
				if (previous[element.type.ordinal()][element.municipality] != null) {
					logger.debug("Problem: " + municipalities.getMunicipality(element.municipality).getName());
					logger.debug(previous[element.municipality]);
					logger.debug(element.previous);
					throw new InvalidStateException();
				}
				previous[element.type.ordinal()][element.municipality] = new PreviousElement(element.previous, element.previousType);
			} else {
				continue;
			}
			for (GraphConnection gc : successorEdges[element.municipality]) {
				Integer fastest = gc.getFastestConnection();
				if (fastest != null) {
					int dist = element.distance;
					if (gc.getType() != element.type) {
						dist += Configuration.getConfiguration().getChangeDifferentTransportTypeTime();
					} else {
						dist += Configuration.getConfiguration().getChangeSameTransportTypeTime();
					}
					dist += fastest;
					int followingCity = municipalities.getIndexByMunicipality(gc.getEndMunicipality());
					// logger.debug("try: " +
					// municipalities.getMunicipality(followingCity).getName() + "; " +
					// gc.getType());
					if (dist < minDistance[gc.getType().ordinal()][followingCity]) {
						minDistance[gc.getType().ordinal()][followingCity] = dist;
						DijkstraElement el = new DijkstraElement();
						el.distance = dist;
						el.municipality = followingCity;
						el.previous = element.municipality;
						el.type = gc.getType();
						el.previousType = element.type;
						// logger.debug("Add: " +
						// municipalities.getMunicipality(el.municipality).getName());
						// logger.debug("type: " + el.type + "; dist:" + el.distance);
						// logger.debug(municipalities.getMunicipality(el.previous).getName());
						queue.add(el);
					}
				}
			}
		}

		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
			if (i == start)
				continue;
			Integer best = null;
			TransportType type = null;
			for (TransportType tt : TransportType.values()) {
				if (previous[tt.ordinal()][i] != null) {
					if (best == null || best > minDistance[tt.ordinal()][i]) {
						best = minDistance[tt.ordinal()][i];
						type = tt;
					}
				}
			}
			// logger.debug("Best between: "+from.getName()+"; "+municipalities.getMunicipality(i).getName());
			if (best != null) {
				List<GraphConnection> connections = new ArrayList<GraphConnection>();

				PreviousElement prev = previous[type.ordinal()][i];
				int current = i;
				do {
					for (GraphConnection gc : successorEdges[prev.municipality]) {
						if (gc.getEndMunicipality().equals(municipalities.getMunicipality(current)) && gc.getType().equals(type)) {
							connections.add(gc);
						}
					}
					// logger.debug(municipalities.getMunicipality(current).getName() +
					// " from " +
					// municipalities.getMunicipality(prev.municipality).getName() +
					// " by " + type);
					current = prev.municipality;
					type = prev.type;
					prev = previous[prev.type.ordinal()][prev.municipality];

				} while (prev.municipality >= 0);

				Collections.reverse(connections);
				bestConnections[start][i] = connections;
			}
			// logger.debug("------");
		}
	}

	public void computeBestConnections() {
		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
			findShortestPath(municipalities.getMunicipality(i));
		}
	}

	public List<GraphConnection> getConnection(Municipality from, Municipality to) {
		return bestConnections[municipalities.getIndexByMunicipality(from)][municipalities.getIndexByMunicipality(to)];
	}

	public List<Municipality> getUnconnectedToPoznanMunicipalities() {
		Municipality poznan = municipalities.getMunicipalityByName("Poznań");
		List<Municipality> result = new ArrayList<Municipality>();
		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {

			if (getConnection(poznan, municipalities.getMunicipality(i)) == null && !municipalities.getMunicipality(i).getName().equals("Poznań")) {

				result.add(municipalities.getMunicipality(i));
			}
		}
		return result;
	}

	public void clearTraffic() {
		int count = municipalities.getMunicipalityCount();
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				if (bestConnections[i][j] != null) {
					for (GraphConnection connection : bestConnections[i][j]) {
						connection.getTrafficSources().clear();
					}
				}
			}
		}

	}

	public void putTrafficOnConnections() {

		int count = municipalities.getMunicipalityCount();

		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				Double traffic = municipalities.getConnection(i, j).getTraffic();
				if (traffic != null && traffic > 0) {
					if (bestConnections[i][j] == null) {
						if (traffic > configuration.getNegligibleTrafficSize())
							logger.warn("There is no connection between " + municipalities.getMunicipality(i).getName() + " and "
									+ municipalities.getMunicipality(j).getName() + " but there is a traffic: " + traffic);
					} else {
						putTrafficOnConnection(municipalities.getConnection(i, j), bestConnections[i][j]);
					}
				}
			}
		}
	}

	protected void putTrafficOnConnection(MunicipalityConnection connection, List<GraphConnection> list) {
		Double traffic = connection.getTraffic();
		for (GraphConnection graphConnection : list) {
			TrafficSource ts = new TrafficSource();
			ts.setAmount(traffic);
			ts.setConnection(connection);
			ts.setStart(connection.getStart());
			ts.setStop(connection.getStop());
			graphConnection.addTrafficSource(ts);
		}
	}

	public List<TransportConnection> getAndReduceSinglePossibilites() {
		List<TransportConnection> result = new ArrayList<TransportConnection>();

		int count = municipalities.getMunicipalityCount();

		for (int i = 0; i < count; i++) {
			List<GraphConnection> edges = successorEdges[i];
			for (GraphConnection graphConnection : edges) {
				if (graphConnection.getTraffic() != null && graphConnection.getTraffic() > Configuration.getConfiguration().getMinTrafficForConnection()
						&& graphConnection.getAvailableVehicles().size() == 1) {
					// String start = graphConnection.getStartMunicipality().getName();
					// String stop = graphConnection.getEndMunicipality().getName();
					// logger.debug("[" + graphConnection.getType() + "]" + start + " - "
					// + stop + ": " + graphConnection.getTraffic());

					double typeSize = graphConnection.getType().getTransportTypeData().averageSize();

					double traffic = graphConnection.getTraffic();

					TransportConnection connection = graphConnection.getAvailableVehicles().get(0);
					while (traffic > Configuration.getConfiguration().getMinTrafficForConnection()) {
						traffic -= typeSize;

						TransportConnection newConnection = new TransportConnection(connection);
						ReduceTrafficByConnection rtbc = new ReduceTrafficByConnection(newConnection, graphConnection);
						rtbc.reduce();
						result.add(newConnection);
					}

				}
			}
		}

		return result;
	}

	protected class ReduceTrafficByConnection {
		int													count;
		double											typeSize;
		Map<Municipality, Integer>	mapIndexes;
		Map<Integer, Municipality>	reverseMap;
		double											freePlaces[];

		TransportConnection					connection;

		GraphConnection							graphConnection;

		public ReduceTrafficByConnection(TransportConnection connection, GraphConnection graphConnection) {
			this.connection = connection;
			this.graphConnection = graphConnection;

			count = connection.getStops().size();

			typeSize = graphConnection.getType().getTransportTypeData().averageSize();

			mapIndexes = new HashMap<Municipality, Integer>();
			reverseMap = new HashMap<Integer, Municipality>();
			for (int i = 0; i < count; i++) {
				ConnectionStop stop = connection.getStop(i);
				mapIndexes.put(stop.getMunicipality(), i);
				reverseMap.put(i, stop.getMunicipality());
			}

			initFreePlaces();
		}

		private void initFreePlaces() {
			freePlaces = new double[count];
			for (int i = 0; i < count; i++)
				freePlaces[i] = typeSize;
		}

		public void reduce() {
			reduceConnection(graphConnection, true);
			for (int i = count - 1; i > 0; i--) {
				for (int j = 0; j < count - i; j++) {
					if (connection.getStop(j).getMunicipality() instanceof OtherMunicipality)
						continue;

					if (connection.getStop(j + i).getMunicipality() instanceof OtherMunicipality)
						continue;

					if (connection.getStop(j).getMunicipality().equals(connection.getStop(j + i).getMunicipality()))
						continue;

					GraphConnection toReduce = getGraphConnectionBetween(connection.getStop(j), connection.getStop(j + i), connection.getType());
					reduceConnection(toReduce, true);
				}
			}
			// double usage = 0;
			// for (int i = 0; i < count; i++) {
			// usage += typeSize - freePlaces[i];
			// }
			// usage /= count;
			// logger.debug("[" + connection.getName() + "] Average occupied places: "
			// + usage);
		}

		public double findUsage() {
			reduceConnection(graphConnection, false);
			for (int i = count - 1; i > 0; i--) {
				for (int j = 0; j < count - i; j++) {
					if (connection.getStop(j).getMunicipality() instanceof OtherMunicipality)
						continue;
					if (connection.getStop(j + i).getMunicipality() instanceof OtherMunicipality)
						continue;
					if (connection.getStop(j).getMunicipality().equals(connection.getStop(j + i).getMunicipality()))
						continue;

					GraphConnection toReduce = getGraphConnectionBetween(connection.getStop(j), connection.getStop(j + i), connection.getType());
					if (!toReduce.equals(graphConnection))
						reduceConnection(toReduce, false);
				}
			}
			double usage = 0;
			for (int i = 0; i < count; i++) {
				usage += typeSize - freePlaces[i];
			}
			usage /= count;
			return usage;
		}

		private void reduceConnection(GraphConnection graphConnection2, boolean decreaseTrafficOnRoute) {
			if (graphConnection2 == null)
				return;
			if (graphConnection2.getTraffic() == null)
				return;
			int start = mapIndexes.get(graphConnection2.getStartMunicipality());
			int stop = mapIndexes.get(graphConnection2.getEndMunicipality());
			double maxPlaces = freePlaces[start];
			for (int i = start; i < stop; i++) {
				maxPlaces = Math.min(maxPlaces, freePlaces[i]);
			}

			double toReduce = Math.min(maxPlaces, graphConnection2.getTraffic());

			for (int i = start; i < stop; i++) {
				freePlaces[i] -= toReduce;
			}
			if (decreaseTrafficOnRoute) {
				double reduced = 0;
				for (TrafficSource source : graphConnection2.getTrafficSources()) {
					if (source.getAmount() > 0) {
						double tmpReduce = Math.min(toReduce - reduced, source.getAmount());

						TrafficSource ts = new TrafficSource();
						ts.setConnection(source.getConnection());
						ts.setStart(graphConnection2.getStartMunicipality());
						ts.setStop(graphConnection2.getEndMunicipality());
						ts.setAmount(tmpReduce);
						connection.addTrafficSource(ts);

						source.setAmount(source.getAmount() - tmpReduce);
						reduced += tmpReduce;
					}
				}

				graphConnection2.setTraffic(graphConnection2.getTraffic() - toReduce);
			}

		}
	}

	public List<TransportConnection> getAndReduceOtherPossibilites() {
		List<TransportConnection> result = new ArrayList<TransportConnection>();

		int count = municipalities.getMunicipalityCount();

		for (int i = 0; i < count; i++) {
			List<GraphConnection> edges = successorEdges[i];
			for (GraphConnection graphConnection : edges) {
				if (graphConnection.getTraffic() != null && graphConnection.getTraffic() > Configuration.getConfiguration().getMinTrafficForConnection()) {
					// String start = graphConnection.getStartMunicipality().getName();
					// String stop = graphConnection.getEndMunicipality().getName();
					// logger.debug("[" + graphConnection.getType() + "]" + start + " - "
					// + stop + ": " + graphConnection.getTraffic() + "["
					// + graphConnection.getAvailableVehicles().size() + "]: ");

					while (graphConnection.getTraffic() > Configuration.getConfiguration().getMinTrafficForConnection()) {
						TransportConnection connection = null;
						double bestUsage = -1;

						for (TransportConnection tc : graphConnection.getAvailableVehicles()) {
							ReduceTrafficByConnection rtbc = new ReduceTrafficByConnection(tc, graphConnection);
							double usage = rtbc.findUsage();
							if (usage > bestUsage) {
								bestUsage = usage;
								connection = tc;
							}
							// logger.debug(tc.getName() + "; usage = " + usage);
						}
						// logger.debug("choose: " + connection.getName());
						TransportConnection newConnection = new TransportConnection(connection);
						ReduceTrafficByConnection rtbc = new ReduceTrafficByConnection(newConnection, graphConnection);
						rtbc.reduce();
						result.add(newConnection);
					}
				}
			}
		}

		return result;
	}

}
