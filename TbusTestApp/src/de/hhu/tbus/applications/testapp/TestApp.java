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
	 * File buffered reader
	 */
	private BufferedReader br;
	
	/**
	 * Path to packet information file
	 */
	private static final String path = "/opt/vsimrti/data/20140217_1-only-trainsize-on-lost-trains/1010/packets.txt.0.download.sorted";

	/**
	 * Called upon timerCall from TbusApplication
	 * Sends a message, if available.
	 * @see de.hhu.tbus.applications.testapp.TbusApplication#timerAction(long)
	 */
	protected void timerAction(long time) {
		while (!eventMessages.isEmpty() && eventMessages.peek().getSendTimestamp() <= time) {			
			log.info("Sending message (SendTimestamp: " + eventMessages.peek().getSendTimestamp() + "ns (Difference: " + (time  - eventMessages.peek().getSendTimestamp()) + " Train: " + eventMessages.peek().getSeqNr() + ", Packet: " + eventMessages.peek().getPacketNr() + ")");
			comMod.sendV2XMessage(eventMessages.poll());
			
			// Create new message (if possible)
			String line;
			try {
				if ((line = br.readLine()) != null) {
					TbusTestMessage msg = createMessage(line);
					
					addEvent(msg.getSendTimestamp());
					eventMessages.add(msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#dispose()
	 */
	@Override
	public void dispose() {
		try {
			if (br != null) br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		eventMessages.clear();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#initialize(com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer)
	 */
	@Override
	public void initialize(ApplicationLayer appLayer) {
		super.initialize(appLayer);
		
		comMod = appLayer.getApplicationToFacility().getCommunicationModuleReference();

		File file = new File(path);
		
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				} else {
					break;
				}
			}
			
			// Add one starting messages
			
			TbusTestMessage msg = createMessage(line);

			addEvent(msg.getSendTimestamp());
			eventMessages.add(msg);
			
			// And add 50 more
			for (int i = 0; i < 50; ++i) {
				if ((line = br.readLine()) != null) {
					msg = createMessage(line);

					addEvent(msg.getSendTimestamp());
					eventMessages.add(msg);
				}
			}
		} catch (Exception ex) {
			log.error("Unable to read CSV file");
			ex.printStackTrace();
		}
		
		log.info("Added " + eventMessages.size() + " events and messages to the queue!");
		
		start();
	}
	
	private TbusTestMessage createMessage(String message) {
		// Set the TTL to 1 because VSimRTI can't handle other TTLs at the moment
		TopologicalDestinationAddress tda = new TopologicalDestinationAddress(new byte[] {0,0,0,0}, 1);

		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(tda);
		SourceAddressContainer sac = appLayer.getApplicationToFacility().generateSourceAddressContainer();

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
