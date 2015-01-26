/**
 * 
 */
package de.hhu.tbus.applications.testapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	
	/**
	 * Event message queue
	 */
	private Queue<TbusTestMessage> eventMessages = new LinkedList<TbusTestMessage>();
	/**
	 * Lower layer communication module
	 */
	private CommunicationModule comMod;

	/**
	 * Called upon timerCall from TbusApplication
	 * Sends a message, if available.
	 * @see de.hhu.tbus.applications.testapp.TbusApplication#timerAction(long)
	 */
	protected void timerAction(long time) {
		log.info("Timer called at " + time);

		while (!eventMessages.isEmpty()) {
			if (eventMessages.peek().getSendTimestamp() == time) {
				comMod.sendV2XMessage(eventMessages.poll());
				break;
			}
			
			log.info("Discarding message (Train: " + eventMessages.peek().getSeqNr() + ", Packet: " + eventMessages.peek().getPacketNr() + ")");
			eventMessages.poll();
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
		TopologicalDestinationAddress tda = new TopologicalDestinationAddress(new byte[] {1,0,0,0}, 1);
		
		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(tda);
		SourceAddressContainer sac = appLayer.getApplicationToFacility().generateSourceAddressContainer();
		
		MessageRouting routing = new MessageRouting(dac, sac);

		File file = new File("/home/bialon/data/tbus-2012-09-17-nach-datenratenanhebung-bei-loss/1010/downloadpackets.txt");
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				
				String[] properties = line.split(",");
				
				Long sendTimestamp = Long.parseLong(properties[0]);
				TbusTestMessage msg = new TbusTestMessage(
						routing,
						sendTimestamp,
						Long.parseLong(properties[1]),
						Integer.parseInt(properties[2]),
						Integer.parseInt(properties[3]),
						Integer.parseInt(properties[4]),
						Integer.parseInt(properties[5]));
				
				addEvent(sendTimestamp);
				eventMessages.add(msg);
			}
		} catch (Exception ex) {
			log.error("Unable to read CSV file");
			ex.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		log.info("Added " + eventMessages.size() + " events and messages to the queue!");
		
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
