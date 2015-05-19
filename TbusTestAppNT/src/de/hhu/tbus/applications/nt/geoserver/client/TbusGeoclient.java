package de.hhu.tbus.applications.nt.geoserver.client;

import java.io.File;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.OperatingSystem;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.VehicleOperatingSystem;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage;
import com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.SourceAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopologicalDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage;

import de.fraunhofer.fokus.eject.ObjectInstantiation;
import de.hhu.tbus.applications.nt.geoserver.client.configuration.TbusGeoClientConfiguration;
import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;
import de.hhu.tbus.applications.nt.geoserver.message.EmergencyWarningMessage;
import de.hhu.tbus.applications.nt.geoserver.message.GeoDistributeMessage;
import de.hhu.tbus.applications.nt.geoserver.message.GeoUpdateMessage;
import de.hhu.tbus.applications.nt.geoserver.server.TbusGeoserver;

/**
 * @author bialon
 *
 */
public class TbusGeoclient extends VehicleApplication {
	// Event calling interval in ns
	private static final long interval = 1000000000L;
	
	private static final long startTimeout = 2000000000L;
	
	private static final Integer GEOUPDATEMSG_FLAG = 1;
	private static final Integer BROADCAST_START_FLAG = 2;
	
	private class BroadcastResource {
		public final long interval;
		public final double radius;
		public final EmbeddedMessage msg;
		
		public BroadcastResource(EmbeddedMessage msg, long interval, double radius) {
			this.msg = msg;
			this.interval = interval;
			this.radius = radius;
		}
	};
	
	private boolean isBroadcasting = false;
	
	private TbusGeoClientConfiguration config;
	
	private void initConfig() {
		final ObjectInstantiation<TbusGeoClientConfiguration> oi = new ObjectInstantiation<TbusGeoClientConfiguration>(TbusGeoClientConfiguration.class);
		
		try {
			File appConfig = new File(getOperatingSystem().getConfigurationPath().getAbsolutePath() + File.separator + "GeoClientConfig-" + getOperatingSystem().getId() + ".json");
			config = (TbusGeoClientConfiguration) oi.readFile(appConfig);
			getLog().info(oi.getLogMessage());
		} catch (InstantiationException ex) {
			getLog().error("Cannot instantiate configuration object: ", ex);
			
			config = new TbusGeoClientConfiguration();
		}
		
		getLog().info("Configuration for " + getOperatingSystem().getId() + ": isEmergencyVehicle: " + config.isEmergencyVehicle + ", interval: " + config.interval);
	}
	
	/**
	 * Send an update message to the TbusGeoServer
	 */
	private void sendGeoUpdateMessage() {
		OperatingSystem os = getOperatingSystem();
		long now = os.getSimulationTime();
		
		// Only send a message if we have a position
		if (os.getPosition() != null ) {
			double longitude = os.getPosition().longitude;
			double latitude = os.getPosition().latitude;
			
			DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(TbusGeoserver.getAddress(), 1));
			SourceAddressContainer sac = os.generateSourceAddressContainer();
			MessageRouting routing = new MessageRouting(dac, sac);
			
			GeoUpdateMessage message = new GeoUpdateMessage(longitude, latitude, now, routing);
			
			os.sendV2XMessage(message);
			getLog().info("Send update message with position (" + longitude + ", " + latitude + ") to " + TbusGeoserver.getAddress() + " at " + now);
		}		
	}

	/**
	 * Send a GeoBroadcast (GeoDistribute) message
	 * @param msg The embedded message to send
	 * @param radius The geo radius for distribution
	 * @param timeout Message timeout, message is invalidated after
	 */
	private void sendGeoBroadcast(EmbeddedMessage msg, double radius, long timeout) {
		OperatingSystem os = getOperatingSystem();
		
		double longitude;
		double latitude;
		
		if (os.getPosition() == null) {
			getLog().error("Current position is null!");
			longitude = 0;
			latitude = 0;
		} else {
			longitude = os.getPosition().longitude;
			latitude = os.getPosition().latitude;
		}
		
		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(TbusGeoserver.getAddress(), 1));
		SourceAddressContainer sac = os.generateSourceAddressContainer();
		MessageRouting routing = new MessageRouting(dac, sac);
		
		GeoDistributeMessage gdm = new GeoDistributeMessage(msg, longitude, latitude, radius, timeout, os.getSimulationTime(), routing);
		os.sendV2XMessage(gdm);
		getLog().info("Send GeoBroadcast message to (" + longitude + ", " + latitude + ") with radius " + radius + " to " + TbusGeoserver.getAddress() + " at " + os.getSimulationTime());
	}
	
	/**
	 * Start broadcasting a EmbeddedMessage for given interval and radius around the node 
	 * @param msg The EmbeddedMessage to broadcast
	 * @param interval The interval to broadcast at
	 * @param radius The radius around the current node to broadcast at
	 */
	protected void startGeoBroadcast(EmbeddedMessage msg, long interval, double radius) {
		if (isBroadcasting) {
			getLog().error("Starting GeoBroadcast with another broadcast already running! Aborting");
			return;
		}
		
		isBroadcasting = true;
		OperatingSystem os = getOperatingSystem();
		BroadcastResource br = new BroadcastResource(msg, interval, radius);
		Event broadcastEvent = new Event(os.getSimulationTime(), this, br);
		
		os.getEventManager().addEvent(broadcastEvent);
	}
	
	/**
	 * Stop the current GeoBroadcast
	 */
	protected void stopGeoBroadcast() {
		isBroadcasting = false;
	}
	
	/**
	 * @see com.dcaiti.vsimrti.rti.eventScheduling.EventProcessor#processEvent(com.dcaiti.vsimrti.rti.eventScheduling.Event)
	 */
	@Override
	public void processEvent(Event evt) throws Exception {
		VehicleOperatingSystem os = getOperatingSystem();
		
		if (evt.getResource() != null) {	
			if (evt.getResource() instanceof Integer && evt.getResource() == GEOUPDATEMSG_FLAG) {
				// Send GeoUpdate message
				sendGeoUpdateMessage();
				
				// Set next event call
				os.getEventManager().addEvent(new Event(os.getSimulationTime() + interval, this, GEOUPDATEMSG_FLAG));
			} else if (evt.getResource() instanceof Integer && evt.getResource() == BROADCAST_START_FLAG) {
				DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(TbusGeoserver.getAddress(), 1));
				SourceAddressContainer sac = getOperatingSystem().generateSourceAddressContainer();
				MessageRouting routing = new MessageRouting(dac, sac);
				
				startGeoBroadcast(new EmergencyWarningMessage(routing, getOperatingSystem().getSimulationTime()), config.interval, config.radius);
			} else if (evt.getResource() instanceof BroadcastResource) {
				if (!isBroadcasting) {
					getLog().info("Received GeoBroadcast event while isBroadcasting set to false - don't worry, VSimRTI does not allow to cancel events");
					return;
				}
				
				BroadcastResource br = (BroadcastResource) evt.getResource();
				sendGeoBroadcast(br.msg, br.radius, br.interval);
				
				os.getEventManager().addEvent(new Event(os.getSimulationTime() + br.interval, this, br));
			}
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#afterUpdateConnection()
	 */
	@Override
	public void afterUpdateConnection() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#afterUpdateVehicleInfo()
	 */
	@Override
	public void afterUpdateVehicleInfo() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#beforeUpdateConnection()
	 */
	@Override
	public void beforeUpdateConnection() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#beforeUpdateVehicleInfo()
	 */
	@Override
	public void beforeUpdateVehicleInfo() {}

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
	public void onApplicationSpecificMessage(ApplicationSpecificMessage arg0) {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onSumoTraciByteArrayMessageResponse(com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse)
	 */
	@Override
	public void onSumoTraciByteArrayMessageResponse(
			SumoTraciByteArrayMessageResponse arg0) {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#receiveV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage)
	 */
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage msg) {
		getLog().info(getOperatingSystem().getId() + " received message " + msg.getClass() + " at " + getOperatingSystem().getSimulationTime());
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		initConfig();
		
		// Start updating geo position
		getOperatingSystem().getEventManager().addEvent(new Event(getOperatingSystem().getSimulationTime() + startTimeout, this, GEOUPDATEMSG_FLAG));
		
		if (config.isEmergencyVehicle) {
			getOperatingSystem().getEventManager().addEvent(new Event(getOperatingSystem().getSimulationTime() + startTimeout, this, BROADCAST_START_FLAG));
		}
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
	public void unableToSendV2XMessage(UnableToSendV2XMessage arg0) {}

}
