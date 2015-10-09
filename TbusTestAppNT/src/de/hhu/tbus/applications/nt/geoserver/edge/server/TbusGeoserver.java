/**
 * Implements a rudimentary Geoserver application running on a RSU
 */
package de.hhu.tbus.applications.nt.geoserver.edge.server;

import java.io.File;
import java.net.InetAddress;
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
 *
 */
public class TbusGeoserver extends RoadSideUnitApplication {
	private static DestinationAddress address = null;
	private DoubleAccessMap<InetAddress, String> ipToEdge = new DoubleAccessMap<InetAddress, String>();
	private HashMap<InetAddress, Double> ipToLanePos = new HashMap<InetAddress, Double>();
	private Set<InetAddress> activeEmergencyVehicles = new HashSet<InetAddress>();
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
	 * @see com.dcaiti.vsimrti.rti.eventScheduling.EventProcessor#processEvent(com.dcaiti.vsimrti.rti.eventScheduling.Event)
	 */
	@Override
	public void processEvent(Event evt) throws Exception {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#afterGetAndResetUserTaggedValue()
	 */
	@Override
	public void afterGetAndResetUserTaggedValue() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#beforeGetAndResetUserTaggedValue()
	 */
	@Override
	public void beforeGetAndResetUserTaggedValue() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onApplicationSpecificMessage(com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage)
	 */
	@Override
	public void onApplicationSpecificMessage(ApplicationSpecificMessage asm) {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onSumoTraciByteArrayMessageResponse(com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse)
	 */
	@Override
	public void onSumoTraciByteArrayMessageResponse(SumoTraciByteArrayMessageResponse stbamr) {}

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
		InetAddress sender = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		String edge = msg.getRoadId();
		double lanePos = msg.getLanePos();
		
		ipToEdge.put(sender, edge);
		ipToLanePos.put(sender, lanePos);
		
		if (activeEmergencyVehicles.contains(sender)) {
			// Distribute updated EV message
		}
	}
	
	private void handleDistributeMessage(GeoDistributeMessage msg) {		
		InetAddress senderIp = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		String sourceEdge = msg.getRoadId();
		String nextEdge = msg.getNextRoadId();
		double maxDistance = msg.getRadius();
		double msgLanePos = msg.getLanePos();
		
		EmbeddedMessage embeddedMsg = msg.getMessage();
		
		activeEmergencyVehicles.add(senderIp);
		
		//TODO: Inform sender (ACK) of message?
		
		distributeMessage(embeddedMsg, senderIp, sourceEdge, nextEdge, msgLanePos, maxDistance);
	}
	
	private void distributeMessage(EmbeddedMessage msg, InetAddress senderIp, String sourceEdge, String nextEdge, double lanePos, double maxDistance) {
		List<List<String>> routes = graph.getRoutesLeadingTo(nextEdge, maxDistance);
//		List<List<String>> routes = graph.getRoutesStartingFrom(sourceEdge, maxDistance);
		Set<String> routesEdges = new HashSet<String>();
		
		// Get a set of all edges within range
		for (List<String> list: routes) {
			routesEdges.addAll(list);
		}
		
		// Forward the message to all vehicles on the mentioned above edges
		for (String edge: routesEdges) {
			Set<InetAddress> destinations;
			if ((destinations = ipToEdge.getKeys(edge)) == null) {
				// No vehicles on edge
				continue;
			}
			if (nextEdge.equals(edge)) {
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
				forwardEmbeddedMessage(msg, destinationIp);
			}
		}
	}
	
	private void forwardEmbeddedMessage(EmbeddedMessage msg, InetAddress destinationIp) {
		OperatingSystem os = getOperatingSystem();
		
		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(new DestinationAddress(destinationIp), 1));
		SourceAddressContainer sac = os.generateSourceAddressContainer();
		
		MessageRouting routing = new MessageRouting(dac, sac);
		
		EmbeddedMessage forwardMsg = msg.copy(routing, os.getSimulationTime());
		os.sendV2XMessage(forwardMsg);
		
		getLog().info("Forwarded GeoDistributeMessage " + forwardMsg.getId() + " content " + forwardMsg.getClass() + " to " + destinationIp + " at " + getOperatingSystem().getSimulationTime());
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		// Set own IP address
		address = new DestinationAddress(getOperatingSystem().getAddress().getIPv4Address());
		
		initConfig();
		
		graph = TbusRoadGraph.getInstance();
		graph.parse(new File(config.sumoNetFile));
		
		getLog().info("Parsed SUMO net file " + config.sumoNetFile);
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#tearDown()
	 */
	@Override
	public void tearDown() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#unableToSendV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage)
	 */
	@Override
	public void unableToSendV2XMessage(UnableToSendV2XMessage msg) {}
	
	public static final DestinationAddress getAddress() {
		return address;
	}
}
