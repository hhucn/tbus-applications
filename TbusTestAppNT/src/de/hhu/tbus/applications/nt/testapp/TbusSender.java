/**
 * 
 */
package de.hhu.tbus.applications.nt.testapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage;
import com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.SourceAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopologicalDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage;

import de.hhu.tbus.applications.nt.testapp.message.TbusTestMessage;

/**
 * @author bialon
 *
 */
public class TbusSender extends VehicleApplication {
	
	private static final String path = "/opt/vsimrti/data/20140217_1-only-trainsize-on-lost-trains/1010/packets.txt.0.download.sorted";
	private BufferedReader br;
	
	TbusTestMessage currentMsg;

	/**
	 * @see com.dcaiti.vsimrti.rti.eventScheduling.EventProcessor#processEvent(com.dcaiti.vsimrti.rti.eventScheduling.Event)
	 */
	@Override
	public void processEvent(Event evt) throws Exception {
		getOperatingSystem().sendV2XMessage(currentMsg);
		
		currentMsg = createMessage(br.readLine());
		getOperatingSystem().getEventManager().addEvent(new Event(currentMsg.getSendTimestamp(), this));
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
	public void onSumoTraciByteArrayMessageResponse(SumoTraciByteArrayMessageResponse arg0) {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#receiveV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage)
	 */
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage arg0) {}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		File file = new File(path);
		
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			
			// Skip comments
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				} else {
					break;
				}
			}
			
			currentMsg = createMessage(line);
			getOperatingSystem().getEventManager().addEvent(new Event(currentMsg.getSendTimestamp(), this));
			
		} catch (Exception ex) {
			getLog().error("Unable to read CSV file");
			ex.printStackTrace();
		}
	}

	private TbusTestMessage createMessage(String message) {
		// Set the TTL to 1 because VSimRTI can't handle other TTLs at the moment
		TopologicalDestinationAddress tda = new TopologicalDestinationAddress(new byte[] {0,0,0,0}, 1);

		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(tda);
		SourceAddressContainer sac = getOperatingSystem().generateSourceAddressContainer();

		MessageRouting routing = new MessageRouting(dac, sac);
				
		String[] properties = message.split(",");
		
		Long sendTimestamp = Long.parseLong(properties[0]);
		TbusTestMessage msg = new TbusTestMessage(
				routing,
				sendTimestamp,
				Long.parseLong(properties[2]),
				Integer.parseInt(properties[10]),
				Integer.parseInt(properties[11]),
				Integer.parseInt(properties[12]),
				Integer.parseInt(properties[9]));
		
		msg.setRealRecvTimestamp(Long.parseLong(properties[1]));
		
		return msg;
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
