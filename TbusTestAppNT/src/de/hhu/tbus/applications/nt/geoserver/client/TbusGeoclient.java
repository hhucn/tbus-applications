package de.hhu.tbus.applications.nt.geoserver.client;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication;
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

	/**
	 * @see com.dcaiti.vsimrti.rti.eventScheduling.EventProcessor#processEvent(com.dcaiti.vsimrti.rti.eventScheduling.Event)
	 */
	@Override
	public void processEvent(Event evt) throws Exception {
		VehicleOperatingSystem os = getOperatingSystem();
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
		
		// Set next event call
		os.getEventManager().addEvent(new Event(now + interval, this));
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
	public void receiveV2XMessage(ReceivedV2XMessage arg0) {
		// TODO
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		getOperatingSystem().getEventManager().addEvent(new Event(getOperatingSystem().getSimulationTime() + startTimeout, this));
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
