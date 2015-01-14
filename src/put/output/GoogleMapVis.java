package put.output;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import put.Configuration;
import put.data.ConnectionStop;
import put.data.InvalidArgumentException;
import put.data.Municipalities;
import put.data.Municipality;
import put.data.OtherMunicipality;
import put.data.TransportConnection;
import put.data.TransportType;
import put.graph.GraphConnection;
import put.graph.GraphModel;
import put.graph.TrafficSource;

public class GoogleMapVis {
	private class GmapConnection {
		String	type;
		String	title;
		String	from;
		String	center;
		String	to;
		String	desc		= "";
		double	traffic	= 0;
	}

	Logger							logger		= Logger.getLogger(GoogleMapVis.class);
	Municipalities			municipalities;
	GraphModel					graphModel;

	GmapConnection[][]	connections;

	private double			maxValue	= 1000;

	public GoogleMapVis(Municipalities municipalities, GraphModel graphModel) {
		this.municipalities = municipalities;
		this.graphModel = graphModel;
	}

	public void print(String fileName, boolean printMunicipalities, boolean printBusTrafic, boolean printTrainTrafic) throws IOException {

		connections = new GmapConnection[municipalities.getMunicipalityCount()][municipalities.getMunicipalityCount()];
		PrintWriter out = new PrintWriter(fileName, Configuration.getConfiguration().getEncoding());

		out.print("<html><head><title>Computed schedule</title>\n");
		out.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n");
		out.print("<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\"></script>\n");

		out.print("<script>\n");
		out.print("  function initialize() {\n");
		out.print("    var myLatlng = new google.maps.LatLng(52.414985,16.925125);\n");
		out.print("    var mapOptions = {\n");
		out.print("      zoom: 8,\n");
		out.print("      center: myLatlng\n");
		out.print("    }\n");
		out.print("    var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);\n");

		if (printMunicipalities) {
			for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
				Municipality municipality = municipalities.getMunicipality(i);
				printMunicipality(out, i, municipality);
			}
		}
		if (printBusTrafic) {
			printBuses(out);
		}
		if (printTrainTrafic) {
			printTrains(out);
		}
		out.print("  }\n");

		out.print("  google.maps.event.addDomListener(window, 'load', initialize);\n");

		out.print("</script>\n");

		out.print("</head>\n");
		out.print("<body>\n");
		out.print("<div style=\"height: 100%\" id=\"map-canvas\"></div>\n");
		String legendName = FilenameUtils.getBaseName(fileName) + ".png";
		out.print("<img src=\"" + legendName + "\" style=\"position: fixed; bottom: 0px; left: 0px;\">\n");

		out.print("</body>\n");
		out.print("</html>\n");
		out.close();

		LegendGenerator.generate(0, maxValue, Color.GREEN, Color.RED, FilenameUtils.getPath(fileName) + legendName, 640, 60);

	}

	private void printBuses(PrintWriter out) {
		int counter = 0;
		createConnections(TransportType.BUS);
		int count = municipalities.getMunicipalityCount();

		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null) {
					maxValue = Math.max(maxValue, connections[i][j].traffic);
				}
			}
		}

		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null) {
					printConnection(out, counter++, connections[i][j]);
				}
			}
		}
	}

	private void printTrains(PrintWriter out) {
		int counter = 1000000;
		createConnections(TransportType.TRAIN);
		int count = municipalities.getMunicipalityCount();
		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null) {
					maxValue = Math.max(maxValue, connections[i][j].traffic);
				}
			}
		}
		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null) {
					printConnection(out, counter++, connections[i][j]);
				}
			}
		}
	}

	private void createConnections(TransportType type) {
		int count = municipalities.getMunicipalityCount();
		for (int i = 0; i < count; i++) {
			for (GraphConnection connection : graphModel.successorEdges[i]) {
				if (connection.getTraffic() != null && connection.getTraffic() > Configuration.getConfiguration().getNegligibleTrafficSize()
						&& connection.getType().equals(type)) {

					// find the shortest connection
					int length = Integer.MAX_VALUE;
					TransportConnection line = null;
					for (TransportConnection tc : connection.getAvailableVehicles()) {
						boolean beginFound = false;
						boolean endFound = false;
						int len = 0;
						for (int j = 0; j < tc.getStops().size(); j++) {
							if (beginFound && !endFound) {
								len++;
							}
							if (tc.getStop(j).getMunicipality().equals(connection.getStartMunicipality())) {
								beginFound = true;
							}
							if (tc.getStop(j).getMunicipality().equals(connection.getEndMunicipality())) {
								endFound = true;
							}
						}
						if (len < length) {
							length = len;
							line = tc;
						}
					}

					// fill the data into our local map
					boolean beginFound = false;
					boolean endFound = false;
					for (int j = 0; j < line.getStops().size(); j++) {
						if (beginFound && !endFound) {
							addConnection(line.getStop(j - 1), line.getStop(j), connection, line.getType());
						}
						if (line.getStop(j).getMunicipality().equals(connection.getStartMunicipality())) {
							beginFound = true;
						}
						if (line.getStop(j).getMunicipality().equals(connection.getEndMunicipality())) {
							endFound = true;
						}
					}
				}
			}
		}

	}

	private void addConnection(ConnectionStop stop, ConnectionStop stop2, GraphConnection connection, TransportType transportType) {
		Integer start = municipalities.getIndexByMunicipality(stop.getMunicipality());
		Integer end = municipalities.getIndexByMunicipality(stop2.getMunicipality());
		if (start == null || end == null)
			return;
		GmapConnection gConnection = connections[start][end];
		if (gConnection == null) {
			gConnection = createConnection(stop.getMunicipality(), stop2.getMunicipality(), 0, transportType);
			connections[start][end] = gConnection;
			connections[end][start] = gConnection;
		}
		gConnection.traffic += connection.getTraffic();
		for (TrafficSource ts : connection.getTrafficSources()) {
			if (ts.getAmount() > Configuration.getConfiguration().getNegligibleTrafficSize()) {
				gConnection.desc += ts.getStart().getName() + " - " + ts.getStop().getName() + " (" + String.format("%.2f", ts.getAmount()) + ")<br/>";
			}
		}
	}

	private void printConnection(PrintWriter out, int i, GmapConnection connection) {
//		if (connection.traffic < Configuration.getConfiguration().getMinTrafficForConnection()) {
//			return;
//		}
		out.print("    var line" + i + " = new google.maps.Polyline({\n");
		out.print("      path: [" + connection.from + "," + connection.to + "],\n");
		out.print("      strokeColor: '" + getColor(connection.traffic) + "',\n");
		out.print("      map: map,\n");
		out.print("      strokeOpacity: 1.0\n");
		out.print("    });\n");

		String content = "";
		content += connection.title + ": " + String.format("%.2f", connection.traffic) + "<br/><br/>";
		if (connection.type != null) {
			content += "Type: " + connection.type + "<br/><br/>";
		}
		if (connection.desc != null) {
			content += connection.desc;
		}
		out.print("    var contentString = '" + content + "';\n");

		out.print("    var infowindow" + i + " = new google.maps.InfoWindow({content: contentString,position: " + connection.center + "});\n");

		out.print("    google.maps.event.addListener(line" + i + ", 'click', function() {\n");
		out.print("      infowindow" + i + ".open(map);\n");
		out.print("    });\n");

	}

	private GmapConnection createConnection(Municipality from, Municipality to, double traffic, TransportType type) {
		GmapConnection result = new GmapConnection();
		result.from = "new google.maps.LatLng(" + from.getCoordinates().getLat() + "," + from.getCoordinates().getLng() + ")";
		result.to = "new google.maps.LatLng(" + to.getCoordinates().getLat() + "," + to.getCoordinates().getLng() + ")";
		result.center = "new google.maps.LatLng(" + (from.getCoordinates().getLat() + to.getCoordinates().getLat()) / 2 + ","
				+ (from.getCoordinates().getLng() + to.getCoordinates().getLng()) / 2 + ")";
		if (traffic > maxValue || traffic < 0) {
			throw new InvalidArgumentException("Invalid traffic in gmap represenatation");
		}
		if (type != null) {
			result.type = type.getCommonName();
		}
		result.traffic = traffic;
		result.title = from.getName() + " - " + to.getName();
		return result;
	}

	protected String getColor(double traffic) {
		double scale = traffic / maxValue;
		if (scale > 1) {
			throw new InvalidArgumentException("Invalid traffic in gmap represenatation");
		}
		scale = log2(scale + 1);
		int val = (int) (scale * 255.0);
		String color = "#" + String.format("%02x", val) + String.format("%02x", 255 - val) + "00";
		return color;
	}

	double log2(double x) {
		return Math.log(x) / Math.log(2.0d);
	}

	private void printMunicipality(PrintWriter out, int i, Municipality municipality) {
		out.print("    var marker" + i + " = new google.maps.Marker({\n");
		out.print("      position: new google.maps.LatLng(" + municipality.getCoordinates().getLat() + "," + municipality.getCoordinates().getLng() + "),\n");
		out.print("      map: map,\n");
		if (municipality instanceof OtherMunicipality) {
			out.print("      icon: 'green_MarkerB.png',\n");
		} else {
			out.print("      icon: 'blue_MarkerA.png',\n");
		}
		out.print("      title: '" + municipality.getName() + "'\n");
		out.print("    });\n");

		out.print("    var contentString = '" + municipality.getName() + "';\n");

		out.print("    var infowindow" + i + " = new google.maps.InfoWindow({content: contentString});\n");

		out.print("    google.maps.event.addListener(marker" + i + ", 'click', function() {\n");
		out.print("      infowindow" + i + ".open(map,marker" + i + ");\n");
		out.print("    });\n");
	}

	public void printConnections(String fileName, boolean printMunicipalities, boolean printBusTrafic, boolean printTrainTrafic,
			List<TransportConnection> suggestedConnections) throws IOException {
		maxValue = 1;
		connections = new GmapConnection[municipalities.getMunicipalityCount()][municipalities.getMunicipalityCount()];
		PrintWriter out = new PrintWriter(fileName, Configuration.getConfiguration().getEncoding());

		out.print("<html><head><title>Computed schedule</title>\n");
		out.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n");
		out.print("<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp\"></script>\n");

		out.print("<script>\n");
		out.print("  function initialize() {\n");
		out.print("    var myLatlng = new google.maps.LatLng(52.414985,16.925125);\n");
		out.print("    var mapOptions = {\n");
		out.print("      zoom: 8,\n");
		out.print("      center: myLatlng\n");
		out.print("    }\n");
		out.print("    var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);\n");

		if (printMunicipalities) {
			for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
				Municipality municipality = municipalities.getMunicipality(i);
				printMunicipality(out, i, municipality);
			}
		}
		if (printBusTrafic) {
			printBusesCount(out, suggestedConnections);
		}
		if (printTrainTrafic) {
			printTrainsCount(out, suggestedConnections);
		}
		out.print("  }\n");

		out.print("  google.maps.event.addDomListener(window, 'load', initialize);\n");

		out.print("</script>\n");

		out.print("</head>\n");
		out.print("<body>\n");
		out.print("<div style=\"height: 100%\" id=\"map-canvas\"></div>\n");
		String legendName = FilenameUtils.getBaseName(fileName) + ".png";
		out.print("<img src=\"" + legendName + "\" style=\"position: fixed; bottom: 0px; left: 0px;\">\n");

		out.print("</body>\n");
		out.print("</html>\n");
		out.close();

		LegendGenerator.generate(0, maxValue, Color.GREEN, Color.RED, FilenameUtils.getPath(fileName) + legendName, 640, 60);

	}

	private void printBusesCount(PrintWriter out, List<TransportConnection> suggestedConnections) {
		int counter = 0;
		createConnectionsCount(TransportType.BUS, suggestedConnections);
		int count = municipalities.getMunicipalityCount();

		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null) {
					maxValue = Math.max(maxValue, connections[i][j].traffic);
				}
			}
		}

		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null && connections[i][j].traffic > 0) {
					printConnection(out, counter++, connections[i][j]);
				}
			}
		}
	}

	private void printTrainsCount(PrintWriter out, List<TransportConnection> suggestedConnections) {
		int counter = 0;
		createConnectionsCount(TransportType.TRAIN, suggestedConnections);
		int count = municipalities.getMunicipalityCount();

		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null) {
					maxValue = Math.max(maxValue, connections[i][j].traffic);
				}
			}
		}

		for (int i = 0; i < count; i++) {
			for (int j = i + 1; j < count; j++) {
				if (connections[i][j] != null && connections[i][j].traffic > 0) {
					printConnection(out, counter++, connections[i][j]);
				}
			}
		}
	}

	private void createConnectionsCount(TransportType type, List<TransportConnection> suggestedConnections) {
		for (TransportConnection connection : suggestedConnections) {
			if (connection.getType().equals(type)) {
				for (int i = 1; i < connection.getStops().size(); i++) {
					ConnectionStop stop = connection.getStop(i - 1);
					ConnectionStop stop2 = connection.getStop(i);
					Integer start = municipalities.getIndexByMunicipality(stop.getMunicipality());
					Integer end = municipalities.getIndexByMunicipality(stop2.getMunicipality());

					if (start != null && end != null) {
						GmapConnection gConnection = connections[start][end];
						if (gConnection == null) {
							gConnection = createConnection(stop.getMunicipality(), stop2.getMunicipality(), 0, null);
							connections[start][end] = gConnection;
							connections[end][start] = gConnection;
						}
						gConnection.traffic += 1;
						gConnection.desc += type.getCommonName() + ": " + connection.getName() + "<br/>";
					} else {
						logger.debug("SKIP: " + stop.getMunicipality().getName() + ", " + stop2.getMunicipality().getName());
					}
				}
			}
		}

	}

}
