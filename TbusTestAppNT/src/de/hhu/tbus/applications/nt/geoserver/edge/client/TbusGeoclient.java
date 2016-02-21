package de.hhu.tbus.applications.nt.geoserver.edge.client;

import java.util.UUID;

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
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;
import com.dcaiti.vsimrti.rti.objects.vehicle.VehicleInfo;

import de.hhu.tbus.applications.nt.configuration.TbusConfiguration;
import de.hhu.tbus.applications.nt.emergencywarning.message.EmergencyWarningMessage;
import de.hhu.tbus.applications.nt.geoserver.edge.client.configuration.TbusGeoClientConfiguration;
import de.hhu.tbus.applications.nt.geoserver.edge.message.GeoDistributeMessage;
import de.hhu.tbus.applications.nt.geoserver.edge.message.GeoUpdateMessage;
import de.hhu.tbus.applications.nt.geoserver.edge.server.TbusGeoserver;
import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;
import de.hhu.tbus.applications.nt.message.TbusLogMessage;

/**
 * @author bialon
 * 
 */
public class TbusGeoclient extends VehicleApplication {

	private static enum eventFlag {
		GEOUPDATE_MSG
	};

	protected class BroadcastResource {
		public final long interval;
		public final double radius;
		public final EmbeddedMessage msg;

		public BroadcastResource(EmbeddedMessage msg, long interval,
				double radius) {
			this.msg = msg;
			this.interval = interval;
			this.radius = radius;
		}
	};

	private boolean isBroadcasting = false;
	private UUID broadcastUUID;
	private boolean broadcastAckReceived;

	private TbusGeoClientConfiguration config;

	protected void initConfig() {
		try {
			config = (new TbusConfiguration<TbusGeoClientConfiguration>())
					.readConfiguration(TbusGeoClientConfiguration.class,
							TbusGeoClientConfiguration.configFilename,
							getOperatingSystem(), getLog());
		} catch (InstantiationException | IllegalAccessException ex) {
			getLog().error(
					"Cannot instantiate configuration object, using default configuration: ",
					ex);

			config = new TbusGeoClientConfiguration();
		}
	}

	protected String getRoadIdEdge() {
		String roadId = null;
		VehicleInfo vi;

		if ((vi = getOperatingSystem().getVehicleInfo()) != null) {
			roadId = vi.getRoadId();
		}

		if (roadId == null) {
			roadId = config.defaultRoadId;
		} else {
			roadId = roadId.substring(0, roadId.lastIndexOf("_"));
		}

		return roadId;
	}

	protected double getLanePosition() {
		VehicleInfo vi;
		double lanePos = config.defaultLanePos;

		if ((vi = getOperatingSystem().getVehicleInfo()) != null) {
			lanePos = vi.getLanePosition();
		}

		return lanePos;
	}

	/**
	 * Safety check for message transmission
	 * 
	 * @return
	 */
	public boolean readyToTransmit() {
		return (getOperatingSystem().getVehicleInfo() != null);
	}

	/**
	 * Send an update message to the TbusGeoServer
	 */
	private void sendGeoUpdateMessage() {
		VehicleOperatingSystem os = getOperatingSystem();
		long now = os.getSimulationTime();

		String roadId = getRoadIdEdge();
		double lanePos = getLanePosition();

		DestinationAddressContainer dac = DestinationAddressContainer
				.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(
						TbusGeoserver.getAddress(), 1));
		SourceAddressContainer sac = os.generateSourceAddressContainer();
		MessageRouting routing = new MessageRouting(dac, sac);

		GeoUpdateMessage message = new GeoUpdateMessage(roadId, lanePos, now,
				routing);

		// Transmission safety checks
		if (config.shouldTransmit) {
			if (readyToTransmit()) {
				getLog().info(
						"Send update message (id: " + message.getId()
								+ ") with position (" + roadId + " at "
								+ lanePos + ") to "
								+ TbusGeoserver.getAddress() + " at " + now);
				os.sendV2XMessage(message);
			} else {
				getLog().info(
						"Would send update message (id: " + message.getId()
								+ ") with position (" + roadId + " at "
								+ lanePos + ") to "
								+ TbusGeoserver.getAddress() + " at " + now
								+ ", but vehicle is not ready");
			}
		} else {
			getLog().info(
					"Would send update message (id: " + message.getId()
							+ ") with position (" + roadId + " at " + lanePos
							+ ") to " + TbusGeoserver.getAddress() + " at "
							+ now + ", but disabled per config");
		}
	}

	/**
	 * Send a GeoBroadcast (GeoDistribute) message
	 * 
	 * @param msg
	 *            The embedded message to send
	 * @param radius
	 *            The geo radius for distribution
	 * @param timeout
	 *            Message timeout, message is invalidated after
	 */
	private void sendGeoBroadcast(EmbeddedMessage msg, double radius) {
		VehicleOperatingSystem os = getOperatingSystem();

		String roadId = getRoadIdEdge();
		double lanePos = getLanePosition();
		long now = os.getSimulationTime();

		DestinationAddressContainer dac = DestinationAddressContainer
				.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(
						TbusGeoserver.getAddress(), 1));
		SourceAddressContainer sac = os.generateSourceAddressContainer();
		MessageRouting routing = new MessageRouting(dac, sac);

		EmbeddedMessage msgCopy = msg.copy(getDefaultRouting(), now);

		GeoDistributeMessage gdm = new GeoDistributeMessage(msgCopy, roadId,
				lanePos, radius, now, GeoDistributeMessage.MessageType.START,
				routing);
		getLog().info("Created message " + gdm);

		if (readyToTransmit()) {
			os.sendV2XMessage(gdm);
			getLog().info(
					"Send GeoBroadcast message (id: " + gdm.getId() + ") to "
							+ roadId + " with radius " + radius + " to "
							+ TbusGeoserver.getAddress() + " at "
							+ os.getSimulationTime());
		} else {
			getLog().info(
					"Would send GeoBroadcast message (id: " + gdm.getId()
							+ ") to " + roadId + " with radius " + radius
							+ " to " + TbusGeoserver.getAddress() + " at "
							+ os.getSimulationTime()
							+ " but vehicle is not ready");
		}
	}

	/**
	 * Start broadcasting a EmbeddedMessage for given interval and radius around
	 * the node
	 * 
	 * @param msg
	 *            The EmbeddedMessage to broadcast
	 * @param offset
	 *            Offset for the first broadcast
	 * @param interval
	 *            The interval to broadcast at
	 * @param timeout
	 *            The messages' timeout
	 * @param radius
	 *            The radius around the current node to broadcast at
	 */
	protected void startGeoBroadcast(EmbeddedMessage msg, long offset,
			long interval, double radius) {
		if (isBroadcasting) {
			getLog().error(
					"Starting GeoBroadcast with another broadcast already running! Aborting");
			return;
		}

		isBroadcasting = true;
		broadcastUUID = msg.getUuid();
		broadcastAckReceived = false;
		OperatingSystem os = getOperatingSystem();
		BroadcastResource br = new BroadcastResource(msg, interval, radius);
		Event broadcastEvent = new Event(os.getSimulationTime() + offset, this,
				br);

		os.getEventManager().addEvent(broadcastEvent);
		getLog().info("Starting broadcast in " + offset + "ns");
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
		OperatingSystem os = getOperatingSystem();

		if (evt.getResource() == null) {
			return;
		} else if (evt.getResource() instanceof eventFlag) {
			switch ((eventFlag) evt.getResource()) {
			case GEOUPDATE_MSG:
				// Send GeoUpdate message
				sendGeoUpdateMessage();

				// Set next event call
				os.getEventManager().addEvent(
						new Event(os.getSimulationTime() + config.interval,
								this, eventFlag.GEOUPDATE_MSG));
				break;
			default:
				break;
			}
		} else if (evt.getResource() instanceof BroadcastResource) {
			if (!isBroadcasting) {
				getLog().info(
						"Received GeoBroadcast event while isBroadcasting set to false - don't worry, VSimRTI does not allow to cancel events");
				return;
			}

			// Repeat broadcast start as long as no ACK was received
			if (!broadcastAckReceived) {
				BroadcastResource br = (BroadcastResource) evt.getResource();
				sendGeoBroadcast(br.msg, br.radius);

				os.getEventManager().addEvent(
						new Event(os.getSimulationTime() + br.interval, this,
								br));
			}
		}
	}

	protected MessageRouting getDefaultRouting() {
		DestinationAddressContainer dac = DestinationAddressContainer
				.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(
						TbusGeoserver.getAddress(), 1));
		SourceAddressContainer sac = getOperatingSystem()
				.generateSourceAddressContainer();

		return new MessageRouting(dac, sac);
	}

	protected void logMessageStatistics(V2XMessage msg) {
		OperatingSystem os = getOperatingSystem();
		long now = os.getSimulationTime();

		if (msg != null) {
			String source = msg.getRouting().getSourceAddressContainer()
					.getSourceAddress().getIPv4Address().toString();
			String dest = msg.getRouting().getDestinationAddressContainer()
					.getDestinationAddress().getIPv4Address().toString();

			if (msg instanceof TbusLogMessage) {
				long delay;
				if (msg instanceof EmergencyWarningMessage) {
					// Use the original send timestamp and not the forwarded one
					delay = now
							- ((EmergencyWarningMessage) msg).originalTimestamp;
				} else {
					delay = now - ((TbusLogMessage) msg).getTimestamp();
				}

				getLog().info(
						source + " -> " + dest + " at " + now + " delay "
								+ delay + ": "
								+ ((TbusLogMessage) msg).getLog());
			} else {
				getLog().info(
						source + " -> " + dest + " at " + now + ": "
								+ msg.getClass().getSimpleName());
			}
		} else {
			getLog().info("Received null message at " + now);
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#afterUpdateConnection()
	 */
	@Override
	public void afterUpdateConnection() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#afterUpdateVehicleInfo()
	 */
	@Override
	public void afterUpdateVehicleInfo() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#beforeUpdateConnection()
	 */
	@Override
	public void beforeUpdateConnection() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication#beforeUpdateVehicleInfo()
	 */
	@Override
	public void beforeUpdateVehicleInfo() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#afterGetAndResetUserTaggedValue()
	 */
	@Override
	public void afterGetAndResetUserTaggedValue() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#beforeGetAndResetUserTaggedValue()
	 */
	@Override
	public void beforeGetAndResetUserTaggedValue() {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onApplicationSpecificMessage(com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage)
	 */
	@Override
	public void onApplicationSpecificMessage(ApplicationSpecificMessage arg0) {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onSumoTraciByteArrayMessageResponse(com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse)
	 */
	@Override
	public void onSumoTraciByteArrayMessageResponse(
			SumoTraciByteArrayMessageResponse arg0) {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#receiveV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage)
	 */
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage msg) {
		V2XMessage v2xMsg = msg.getMessage();
		if (v2xMsg instanceof EmbeddedMessage) {
			if (((EmbeddedMessage) v2xMsg).getUuid().equals(broadcastUUID)) {
				broadcastAckReceived = true;
			}
		}

		logMessageStatistics(v2xMsg);
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		initConfig();

		// Start updating geo position with first offset
		getOperatingSystem().getEventManager().addEvent(
				new Event(getOperatingSystem().getSimulationTime()
						+ config.offset, this, eventFlag.GEOUPDATE_MSG));

		getLog().info(
				"TbusGeoClient constructor called at "
						+ getOperatingSystem().getSimulationTime() + " for id "
						+ getOperatingSystem().getId());
		
		getLog().info("Got IP-Address :"+getOperatingSystem().generateSourceAddressContainer().getSourceAddress().getIPv4Address().toString());
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
	public void unableToSendV2XMessage(UnableToSendV2XMessage arg0) {
	}

}
