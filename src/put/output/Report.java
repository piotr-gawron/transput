package put.output;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import put.Configuration;
import put.data.ConnectionStop;
import put.data.Municipalities;
import put.data.Municipality;
import put.data.TransportConnection;
import put.graph.GraphConnection;
import put.graph.GraphModel;

public class Report {
	Logger					logger	= Logger.getLogger(Report.class);
	Municipalities	municipalities;
	GraphModel			gm;

	public Report(Municipalities municipalities, GraphModel graphModel) {
		this.municipalities = municipalities;
		this.gm = graphModel;
	}

	List<TransportConnection>	transportConnections	= new ArrayList<TransportConnection>();

	public void addTransportConnections(Collection<TransportConnection> connections) {
		transportConnections.addAll(connections);
	}

	public String createReportForMunicipality(Municipality municipality) {
		int start = municipalities.getIndexByMunicipality(municipality);
		StringBuilder result = new StringBuilder();

		result.append("<html><head><title>" + municipality.getName() + "</title>");
		result.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		result.append("</head>");
		result.append("<body>");
		result.append("<h1>" + municipality.getName() + "</h1>");
		result.append("<p>Public transport connected to the municipality:</p>");
		result.append("<table border=\"1\">");
		result.append("<tr><th>Type</th><th>Stops</th><th>Name</th></tr>");

		Set<String> processedLines = new HashSet<String>();
		for (TransportConnection tc : transportConnections) {
			if (processedLines.contains(tc.getName())) {
				continue;
			}

			processedLines.add(tc.getName());

			boolean ok = false;
			for (ConnectionStop stop : tc.getStops()) {
				if (stop.getMunicipality().equals(municipality))
					ok = true;
			}
			if (ok) {
				result.append("<tr><td>" + tc.getType().getCommonName() + "</td>");
				result.append("<td>");
				boolean first = true;
				for (ConnectionStop stop : tc.getStops()) {
					if (!first) {
						result.append(" - ");
					} else
						first = false;

					if (stop.getMunicipality().equals(municipality)) {
						result.append("<font size=\"+1\"><b>" + stop.getMunicipality().getName() + "</b></font>");
					} else
						result.append(stop.getMunicipality().getName());
				}
				result.append("<td>" + tc.getName() + "</td>");
				result.append("</td></tr>");
			}
		}
		result.append("</table>");

		result.append("<p>Routes computed to:</p>");

		result.append("<table border=\"1\">");
		result.append("<tr><th>Municipality</th><th>traffic</th><th>connection</th></tr>");

		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
			Municipality end = municipalities.getMunicipality(i);
			Double t = municipalities.getConnection(start, i).getTraffic();
			String traffic = "N/A";
			if (t != null)
				traffic = String.format("%1$,.2f", t);

			t = municipalities.getConnection(i,start).getTraffic();
			String traffic2 = "N/A";
			if (t != null)
				traffic2 = String.format("%1$,.2f", t);
			
			result.append("<tr><td><a href=\"" + i + ".html\">" + end.getName() + "</a></td><td title=\"opposite: "+traffic2+"\">" + traffic + "</td>");
			result.append("<td>");
			List<GraphConnection> connections = gm.getConnection(municipality, end);
			if (connections == null) {
				result.append("N/A");
			} else {
				for (GraphConnection graphConnection : connections) {
					result.append(graphConnection.getType().getCommonName() + ": " + graphConnection.getStartMunicipality().getName() + " - "
							+ graphConnection.getEndMunicipality().getName() + " (~" + graphConnection.getFastestConnection() + " min)");
					result.append("<br/>");
				}
			}
			result.append("</td></tr>");
		}
		result.append("</table>");

		result.append("</body>");
		return result.toString();
	}

	public String createReportIndex() {
		StringBuilder result = new StringBuilder();

		result.append("<html><head><title>List of municipalities</title>");
		result.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		result.append("</head>");
		result.append("<body>");
		result.append("<h1>List of municipalities</h1>");
		result.append("<ul>");
		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
			Municipality end = municipalities.getMunicipality(i);
			result.append("<li><a href=\"" + i + ".html\">" + end.getName() + "</a></li>");
		}
		result.append("</ul>");

		result.append("</body>");
		return result.toString();

	}

	public void createReportPack(String dir) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(dir + "index.html", Configuration.getConfiguration().getEncoding());

		out.print(createReportIndex());
		out.close();

		for (int i = 0; i < municipalities.getMunicipalityCount(); i++) {
			out = new PrintWriter(dir + i + ".html", Configuration.getConfiguration().getEncoding());
			out.print(createReportForMunicipality(municipalities.getMunicipality(i)));
			out.close();
		}

	}
	

}
