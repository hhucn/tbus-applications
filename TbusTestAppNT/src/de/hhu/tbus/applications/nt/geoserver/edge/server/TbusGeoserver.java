/**
 * Implements a rudimentary Geoserver application running on a RSU
 */
package de.hhu.tbus.applications.nt.geoserver.edge.server;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.RoadSideUnitApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.OperatingSystem;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage;
import com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddress;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.SourceAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopologicalDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.configuration.TbusConfiguration;
import de.hhu.tbus.applications.nt.geoserver.edge.message.GeoDistributeMessage;
import de.hhu.tbus.applications.nt.geoserver.edge.message.GeoUpdateMessage;
import de.hhu.tbus.applications.nt.geoserver.edge.server.configuration.TbusGeoServerConfiguration;
import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;
import de.hhu.tbus.applications.nt.graph.TbusRoadGraph;
import de.hhu.tbus.applications.nt.message.TbusLogMessage;
import de.hhu.tbus.util.DoubleAccessMap;

/**
 * @author bialon
 * @author Norbert Goebel
 *
 */
public class TbusGeoserver extends TbusGeoserverBase {
	private DoubleAccessMap<InetAddress, String> ipToEdge = new DoubleAccessMap<InetAddress, String>();
	private HashMap<InetAddress, Double> ipToLanePos = new HashMap<InetAddress, Double>();
	private HashMap<InetAddress, GeoDistributeMessage> activeEmergencyMessages = new HashMap<InetAddress, GeoDistributeMessage>();
	private TbusRoadGraph graph;

	private TbusGeoServerConfiguration config;

	protected void initConfig() {		
		try {
			config = (new TbusConfiguration<TbusGeoServerConfiguration>()).readConfiguration(TbusGeoServerConfiguration.class, TbusGeoServerConfiguration.configFilename, getOperatingSystem(), getLog());
		} catch (InstantiationException | IllegalAccessException ex) {
			getLog().error("Cannot instantiate configuration object, using default configuration: ", ex);

			config = new TbusGeoServerConfiguration();
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#receiveV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage)
	 */
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage recvMsg) {
		V2XMessage msg = recvMsg.getMessage();

		logMessageStatistics(msg);

		if (msg == null) {
			return;
		}

		if (msg instanceof GeoUpdateMessage) {
			handleUpdateMessage((GeoUpdateMessage) msg);
		} else if (msg instanceof GeoDistributeMessage) {
			handleDistributeMessage((GeoDistributeMessage) msg);
		} else {
			// TODO
		}

	}

	protected void logMessageStatistics(V2XMessage msg) {
		OperatingSystem os	= getOperatingSystem();
		long now			= os.getSimulationTime();

		if (msg != null) {
			String source	= msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address().toString();
			String dest		= msg.getRouting().getDestinationAddressContainer().getDestinationAddress().getIPv4Address().toString();

			if (msg instanceof TbusLogMessage) {
				long delay 		= now - ((TbusLogMessage) msg).getTimestamp();

				getLog().info(source + " -> " + dest + " at " + now + " delay " + delay + ": " + ((TbusLogMessage) msg).getLog());
			} else {
				getLog().info(source + " -> " + dest + " at " + now + ": " + msg.getClass().getSimpleName());
			}
		} else {
			getLog().info("Received null message at " + now);
		}
	}

	private void handleUpdateMessage(GeoUpdateMessage msg) {		
		InetAddress senderIp = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		String edge = msg.getRoadId();
		double lanePos = msg.getLanePos();

		ipToEdge.put(senderIp, edge);
		ipToLanePos.put(senderIp, lanePos);

		if (activeEmergencyMessages.containsKey(senderIp)) {
			// Distribute updated EV message
			GeoDistributeMessage distributeMsg = activeEmergencyMessages.get(senderIp);
			
			distributeMessage(distributeMsg.getMessage(), senderIp, edge, lanePos, distributeMsg.getRadius(), msg.getTimestamp());
		}
	}

	private void handleDistributeMessage(GeoDistributeMessage msg) {		
		InetAddress senderIp = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		String sourceEdge = msg.getRoadId();
		double maxDistance = msg.getRadius();
		double msgLanePos = msg.getLanePos();

		EmbeddedMessage embeddedMsg = msg.getMessage();

		activeEmergencyMessages.put(senderIp, msg);

		// Inform sender of message by sending message back (ACK)
		distributeMessage(embeddedMsg, senderIp, sourceEdge, msgLanePos, maxDistance, msg.getTimestamp());
	}

	/**
	 * send a distribute Message to all vehicles, which might cross any possible emergency vehicles route in approximately maxDistance distance
	 * @param msg
	 * @param senderIp
	 * @param sourceEdge
	 * @param lanePos
	 * @param maxDistance
	 */
	private void distributeMessage(EmbeddedMessage msg, InetAddress senderIp, String sourceEdge, double lanePos, double maxDistance, long originalTimestamp) {
		Set<String> nextEdges = graph.getNextEdges(sourceEdge);
		Set<String> evEdges = new HashSet<String>();
		List<List<String>> evRoutes = new ArrayList<List<String>>();  
		Set<String> routesEdges = new HashSet<String>();
		
		//correct maxdistance by the rest of the current edges length
		double remainDistance = maxDistance - graph.getEdgeLength(sourceEdge) + lanePos;
		if (remainDistance < 0.0) remainDistance = 0.0; 
		
		//get all possible routes with length maxdistance the emergency vehicle can continue on
		evRoutes.addAll(graph.getRoutesStartingFrom(sourceEdge, remainDistance));
		
		//now get all the edges of the next routes with max distance of the ev
		for (List<String> list: evRoutes) {
			 evEdges.addAll(list);
		}
		
		//get all incoming edges to endpoints of the evEdges - those are the edges to broadcast the ev event to.
		for (String evEdge :evEdges){
			routesEdges.addAll(graph.getIncomingEdges(evEdge));
		}
		
		//and finally remove all evEdges from the routeEdges to prevent stalling evs
		routesEdges.removeAll(evEdges);
		
		//now routeEdges should contain edges that directly lead to possible ev edges
		// thus forward the message to all vehicles on routeEdges
		for (String edge: routesEdges) {
			Set<InetAddress> destinations;
			if ((destinations = ipToEdge.getKeys(edge)) == null) {
				// No vehicles on edge
				continue;
			}
			if (nextEdges.contains(edge)) {
				// Next edge, vehicles here drive normally
				continue;
			}

			for (InetAddress destinationIp: destinations) {
				if (senderIp.equals(destinationIp)) {
					continue;
				}

				// If this is the source edge, take lane position into account
				if (sourceEdge.equals(edge) && ipToLanePos.get(destinationIp) >= lanePos) {
					continue;
				}

				getLog().info("Forwarding message to " + destinationIp + " on edge " + edge);
				forwardEmbeddedMessage(msg, destinationIp, originalTimestamp);
			}
		}
	}

	private void forwardEmbeddedMessage(EmbeddedMessage msg, InetAddress destinationIp, long originalTimestamp) {
		OperatingSystem os = getOperatingSystem();

		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(new DestinationAddress(destinationIp), 1));
		SourceAddressContainer sac = os.generateSourceAddressContainer();

		MessageRouting routing = new MessageRouting(dac, sac);

		EmbeddedMessage forwardMsg = msg.copy(routing, os.getSimulationTime());
		forwardMsg.originalTimestamp = originalTimestamp;
		
		os.sendV2XMessage(forwardMsg);

		getLog().info("Forwarded GeoDistributeMessage " + forwardMsg.getId() + " content " + forwardMsg.getClass() + " to " + destinationIp + " at " + getOperatingSystem().getSimulationTime());
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		super.setUp();

		initConfig();

		graph = TbusRoadGraph.getInstance();
		graph.parse(new File(config.sumoNetFile));

		getLog().info("Parsed SUMO net file " + config.sumoNetFile);
	}
}
