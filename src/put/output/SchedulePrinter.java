package put.output;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import put.Configuration;
import put.data.ConnectionStop;
import put.data.Municipality;
import put.data.TransportConnection;
import put.data.TransportType;
import put.graph.TrafficSource;

public class SchedulePrinter {
	Logger										logger			= Logger.getLogger(SchedulePrinter.class);
	List<TransportConnection>	connections	= new ArrayList<TransportConnection>();

	public SchedulePrinter(List<TransportConnection> connections) {
		this.connections.addAll(connections);
		Collections.sort(this.connections, new TransportConnection.TransportConnectionNameComparator());
	}

	public void print(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(fileName, Configuration.getConfiguration().getEncoding());

		out.print("<html><head><title>Computed schedule</title>");
		out.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.print("</head>");
		out.print("<body>");

		Map<TransportType, Double> cost = new HashMap<TransportType, Double>();
		Map<TransportType, Double> income = new HashMap<TransportType, Double>();

		for (TransportType type : TransportType.values()) {
			cost.put(type, 0.0);
			income.put(type, 0.0);
		}
		for (TransportConnection tc : connections) {
			TransportType type = tc.getType();

			Double typeCost = cost.get(type);
			Double typeIncome = income.get(type);
			for (int i = 1; i < tc.getStops().size(); i++) {
				Municipality first = tc.getStop(i - 1).getMunicipality();
				Municipality second = tc.getStop(i).getMunicipality();
				if (first.getClass().equals(Municipality.class) && second.getClass().equals(Municipality.class)) {
					double distance = tc.getDistanceBetweenStops(first, second);
					typeCost += type.getTransportTypeData().kmCost() * distance;
				}
			}
			for (TrafficSource ts : tc.getTrafficSources()) {
				Municipality first = ts.getStart();
				Municipality second = ts.getStop();
				if (first.getClass().equals(Municipality.class) && second.getClass().equals(Municipality.class)) {
					double distance = tc.getDistanceBetweenStops(first, second);
					typeIncome += type.getTransportTypeData().kmIncome() * distance * ts.getAmount();
				}
			}
			cost.put(type, typeCost);
			income.put(type, typeIncome);
		}
		out.print("<h1>Cost</h1>");
		for (TransportType type : TransportType.values()) {
			Double distance = cost.get(type);
			out.print("<h2>" + type.getCommonName() + ": " + doubleToString(distance) + "</h2>");
		}

		out.print("<h1>Income</h1>");
		for (TransportType type : TransportType.values()) {
			Double distance = income.get(type);
			out.print("<h2>" + type.getCommonName() + ": " + doubleToString(distance) + "</h2>");
		}

		out.print("<h1>Connections</h1>");
		for (TransportConnection tc : connections) {
			out.print(connectionToHtml(tc));
			out.print("<br/><br/>");
		}
		out.print("</body>");
		out.print("</html>");
		out.close();
	}

	public String connectionToHtml(TransportConnection tc) {
		StringBuilder result = new StringBuilder();

		Double cost = 0.0;
		for (int i = 1; i < tc.getStops().size(); i++) {
			Municipality first = tc.getStop(i - 1).getMunicipality();
			Municipality second = tc.getStop(i).getMunicipality();
			if (first.getClass().equals(Municipality.class) && second.getClass().equals(Municipality.class)) {
				double distance = tc.getDistanceBetweenStops(first, second);
				cost += tc.getType().getTransportTypeData().kmCost() * distance;
			}
		}

		Double income = 0.0;
		for (TrafficSource ts : tc.getTrafficSources()) {
			Municipality first = ts.getStart();
			Municipality second = ts.getStop();
			if (first.getClass().equals(Municipality.class) && second.getClass().equals(Municipality.class)) {
				double distance = tc.getDistanceBetweenStops(first, second);
				income += tc.getType().getTransportTypeData().kmIncome() * distance * ts.getAmount();
			}
		}

		result.append("<b>[" + tc.getType().getCommonName() + "] " + tc.getName() + " (Cost: " + doubleToString(cost) + ", Income: " + doubleToString(income) + ")</b><br/>\n");

		result.append("<table border=\"1\" >\n");
		result.append("<tr>");
		for (ConnectionStop stop : tc.getStops()) {
			result.append("<td>" + stop.getMunicipality().getName() + "</td>");
		}
		result.append("</tr>\n");

		result.append("<tr>");
		List<TrafficSource> processedSources = new ArrayList<TrafficSource>();
		for (int i = 0; i < tc.getStops().size(); i++) {

			ConnectionStop stop = tc.getStop(i);
			result.append("<td valign=\"top\">");

			for (int j = 0; j < tc.getStops().size(); j++) {
				ConnectionStop stop2 = tc.getStop(j);
				Double amount = 0.0;
				StringBuilder desc = new StringBuilder();
				for (TrafficSource ts : tc.getTrafficSources()) {
					if (ts.getAmount() > Configuration.getConfiguration().getNegligibleTrafficSize() && ts.getStart().equals(stop.getMunicipality())
							&& ts.getStop().equals(stop2.getMunicipality()) && !processedSources.contains(ts)) {
						amount += ts.getAmount();
						desc.append(ts.getConnection().getStart().getName() + " - " + ts.getConnection().getStop().getName() + " (" + doubleToString(ts.getAmount())
								+ ")\n");
						processedSources.add(ts);
					}
				}
				if (amount > 0.0) {
					result.append("<div style=\"background-color: #DDFFDD\" title = \"" + desc.toString() + "\">");
					result.append(stop2.getMunicipality().getName() + ": " + doubleToString(amount));
					result.append("</div>");
				}

				amount = 0.0;
				desc = new StringBuilder();

				for (TrafficSource ts : processedSources) {
					if (ts.getAmount() > Configuration.getConfiguration().getNegligibleTrafficSize() && ts.getStop().equals(stop.getMunicipality())
							&& ts.getStart().equals(stop2.getMunicipality())) {
						amount += ts.getAmount();

						desc.append(ts.getConnection().getStart().getName() + " - " + ts.getConnection().getStop().getName() + " (" + doubleToString(ts.getAmount())
								+ ")\n");

					}

				}
				if (amount > 0.0) {
					result.append("<div style=\"background-color: #FFDDDD\" title = \"" + desc.toString() + "\">");
					result.append(stop2.getMunicipality().getName() + ": " + doubleToString(amount));
					result.append("</div>");
				}

			}
			result.append("&nbsp;</td>");
		}
		result.append("</tr>\n");
		result.append("</table>\n");

		return result.toString();
	}

	public String doubleToString(double d) {
		return String.format("%1$,.2f", d);
	}

	public void printConnections(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(fileName, Configuration.getConfiguration().getEncoding());

		out.print("<html><head><title>Suggested connections</title>");
		out.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.print("</head>");
		out.print("<body>");
		out.print("<table>");
		for (TransportConnection connection: connections) {
			out.print("<tr><td>"+connection.getName()+"</td>");
		}
		out.print("</table>");
		out.print("</body>");
		out.print("</html>");
		out.close();
	}
}
