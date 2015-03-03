/**
 * 
 */
package de.hhu.tbus.applications.nt.testapp;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.VehicleApplication;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage;
import com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.testapp.message.TbusTestMessage;

/**
 * @author bialon
 *
 */
public class TbusReceiver extends VehicleApplication {

	/**
	 * @see com.dcaiti.vsimrti.rti.eventScheduling.EventProcessor#processEvent(com.dcaiti.vsimrti.rti.eventScheduling.Event)
	 */
	@Override
	public void processEvent(Event arg0) throws Exception {}

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
	public void receiveV2XMessage(ReceivedV2XMessage rcvdMsg) {
		V2XMessage msg = rcvdMsg.getMessage();
		
		if (msg instanceof TbusTestMessage) {
			TbusTestMessage tbusMsg = (TbusTestMessage) msg;
			long time = getOperatingSystem().getSimulationTime();
			long diff = time - tbusMsg.getRecvTimestamp();
			long diff2 = time - tbusMsg.getRealRecvTimestamp();
			
			getLog().info("Received message with id " + msg.getId() + " at " + time + ": " + tbusMsg);
			getLog().info("Difference: " + diff + "ns (" + ((double) diff / 1000000000) + "s) RealWorld-Difference: " + diff2);
			getLog().info("Plotline thisSim: " + time + " otherSim: " + tbusMsg.getRecvTimestamp() + " real: " + tbusMsg.getRealRecvTimestamp());
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {}

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
