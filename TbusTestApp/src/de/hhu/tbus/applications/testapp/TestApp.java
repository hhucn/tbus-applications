/**
 * 
 */
package de.hhu.tbus.applications.testapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Queue;

import com.dcaiti.vsimrti.fed.app.api.interfaces.Application;
import com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer;
import com.dcaiti.vsimrti.fed.app.api.interfaces.CommunicationModule;
import com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.SourceAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopologicalDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;
import de.hhu.tbus.applications.testapp.message.TbusTestMessage;

/**
 * @author bialon
 *
 */
public class TestApp extends TbusApplication implements Application {
	
	private Queue<TbusTestMessage> eventMessages = new LinkedList<TbusTestMessage>();
	private CommunicationModule comMod;

	protected void timerAction(long time) {
		log.info("Timer called at " + time);
		if (!eventMessages.isEmpty()) {
			comMod.sendV2XMessage(eventMessages.poll());
		}
	}
	
	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#dispose()
	 */
	@Override
	public void dispose() {}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#initialize(com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer)
	 */
	@Override
	public void initialize(ApplicationLayer appLayer) {
		super.initialize(appLayer);
		
		this.comMod = appLayer.getApplicationToFacility().getCommunicationModuleReference();
		
		// Set the TTL to 1 because VSimRTI can't handle other TTLs at the moment
		TopologicalDestinationAddress tda = new TopologicalDestinationAddress(new byte[] {0,0,0,0}, 1);
		
		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(tda);
		SourceAddressContainer sac = appLayer.getApplicationToFacility().generateSourceAddressContainer();
		
		MessageRouting routing = new MessageRouting(dac, sac);
		
		// TODO: Open file
		File file = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = br.readLine()) != null) {
				String[] properties = line.split(",");
				
				// TODO:
				// Create message from properties and add message and event time into queues
			}
		} catch (Exception ex) {
			log.error("Unable to read CSV file");
		}
		
		TbusTestMessage msg1 = new TbusTestMessage(
				routing,
				10000000000L,
				10000000000L,
				1,
				1,
				1,
				728);
		
		TbusTestMessage msg2 = new TbusTestMessage(
				routing,
				15000000000L,
				15000000000L,
				1,
				1,
				1,
				728);

		addEvent(10000000000L);
		eventMessages.add(msg1);
		
		addEvent(15000000000L);
		eventMessages.add(msg2);
		
		start();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#receiveMessage(com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage)
	 */
	@Override
	public void receiveMessage(ReceivedV2XMessage receivedMsg) {
		V2XMessage v2xMsg = receivedMsg.getMessage();
		
		if (!(v2xMsg instanceof TbusTestMessage)) {
			return;
		}
		
		TbusTestMessage msg = (TbusTestMessage) v2xMsg;
		
		log.info("Received message at simulation time " + receivedMsg.getTime() + " with calculated time " + msg.getRecvTimestamp());
	}

}
