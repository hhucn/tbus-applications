/**
 * Implements a rudimentary Geoserver application running on a RSU
 */
package de.hhu.tbus.applications.nt.geoserver.edge.server;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.RoadSideUnitApplication;
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
import de.hhu.tbus.util.DoubleAccessMap;

/**
 * @author bialon
 *
 */
public class TbusGeoserver extends RoadSideUnitApplication {
	private static DestinationAddress address = null;
	private DoubleAccessMap<InetAddress, String> ipToEdge = new DoubleAccessMap<InetAddress, String>();
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
	public void processEvent(Event evt) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#afterGetAndResetUserTaggedValue()
	 */
	@Override
	public void afterGetAndResetUserTaggedValue() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#beforeGetAndResetUserTaggedValue()
	 */
	@Override
	public void beforeGetAndResetUserTaggedValue() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onApplicationSpecificMessage(com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage)
	 */
	@Override
	public void onApplicationSpecificMessage(ApplicationSpecificMessage asm) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onSumoTraciByteArrayMessageResponse(com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse)
	 */
	@Override
	public void onSumoTraciByteArrayMessageResponse(
			SumoTraciByteArrayMessageResponse stbamr) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#receiveV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage)
	 */
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage recvMsg) {
		V2XMessage msg = recvMsg.getMessage();
		
		if (msg == null) {
			getLog().error("Received null message!");
			return;
		} else if (msg.getRouting() == null) {
			getLog().error("Received message with null routing!");
		} else {
			getLog().info("Received message with id " + msg.getId() + " from " + msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address());
		}
		
		if (msg instanceof GeoUpdateMessage) {
			handleUpdateMessage((GeoUpdateMessage) msg);
		} else if (msg instanceof GeoDistributeMessage) {
			handleDistributeMessage((GeoDistributeMessage) msg);
		} else {
			// TODO
		}

	}
	
	private void handleUpdateMessage(GeoUpdateMessage msg) {
		InetAddress sender = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		String edge = msg.getRoadId();
		
		ipToEdge.put(sender, edge);
	}
	
	private void handleDistributeMessage(GeoDistributeMessage msg) {
		InetAddress senderIp = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		String sourceEdge = msg.getRoadId();
		double maxDistance = msg.getRadius();
		long timestamp = msg.getTimestamp();
		
		EmbeddedMessage embeddedMsg = msg.getMessage();
		
		List<List<String>> routes = graph.getRoutesLeadingTo(sourceEdge, maxDistance);
		Set<String> routesEdges = new HashSet<String>();
		
		// Get a set of all edges within range
		for (List<String> list: routes) {
			routesEdges.addAll(list);
		}
		
		// Forward the message to all vehicles on the mentioned above edges
		for (String edge: routesEdges) {
			Set<InetAddress> destinations;
			if (sourceEdge.equals(edge)) {
				continue;
			} else if ((destinations = ipToEdge.getKeys(edge)) == null) {
				// No vehicles on edge
				continue;
			}
			
			for (InetAddress destinationIp: destinations) {
				if (senderIp.equals(destinationIp)) {
					continue;
				}
				forwardEmbeddedMessage(embeddedMsg, destinationIp, timestamp);
			}
		}
	}
	
	private void forwardEmbeddedMessage(EmbeddedMessage msg, InetAddress destinationIp, long timestamp) {
		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(new DestinationAddress(destinationIp), 1));
		SourceAddressContainer sac = getOperatingSystem().generateSourceAddressContainer();
		
		MessageRouting routing = new MessageRouting(dac, sac);
		
		V2XMessage forwardMsg = msg.copy(routing, timestamp);
		getOperatingSystem().sendV2XMessage(forwardMsg);
		
		getLog().info("Forwarded GeoDistributeMessage content " + forwardMsg.getClass() + " to " + destinationIp + " at " + getOperatingSystem().getSimulationTime());
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
	public void tearDown() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#unableToSendV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage)
	 */
	@Override
	public void unableToSendV2XMessage(UnableToSendV2XMessage msg) {
		// TODO Auto-generated method stub

	}
	
	public static final DestinationAddress getAddress() {
		return address;
	}
}
