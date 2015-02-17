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

import org.slf4j.Logger;

import com.dcaiti.vsimrti.fed.app.api.interfaces.Application;
import com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer;
import com.dcaiti.vsimrti.fed.app.api.interfaces.CommunicationModule;
import com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.SourceAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopologicalDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;

import de.hhu.tbus.applications.testapp.message.TbusTestMessage;

/**
 * @author bialon
 *
 */
public class TbusConstantIntervalApp implements Application {
	private ApplicationLayer appLayer;
	private CommunicationModule comMod;
	private Logger log;
	private BufferedReader br;
	
	/**
	 * Message queue
	 */
	private Queue<TbusTestMessage> messages = new LinkedList<TbusTestMessage>();
	
	private static final String path = "/home/bialon/data/20140217_1-only-trainsize-on-lost-trains/1010/packets.txt.0.download.sorted";
	
	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#getMinimalTimerCallInterval()
	 */
	@Override
	public long getMinimalTimerCallInterval() {
		return 66000000L;
//		return 1000000000L;
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#timerCall(long)
	 */
	@Override
	public void timerCall(long time) {
		if (time >= 3000000000L && !messages.isEmpty()) {
			TbusTestMessage msg = messages.poll();
			comMod.sendV2XMessage(msg);
			log.info("Sent message " + msg.getId() + " at " + time);
			
			// Generate new message (if possible)
			String line;
			try {
				if ((line = br.readLine()) != null) {
					TbusTestMessage newMsg = createMessage(line);
					
					messages.add(newMsg);
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
		
		messages.clear();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#initialize(com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer)
	 */
	@Override
	public void initialize(ApplicationLayer appLayer) {
		this.appLayer = appLayer;
		comMod = appLayer.getApplicationToFacility().getCommunicationModuleReference();
		log = appLayer.getApplicationToFacility().getLogger();

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
			
			// Add two starting messages
			
			TbusTestMessage msg = createMessage(line);

			messages.add(msg);
			
			if ((line = br.readLine()) != null) {
				msg = createMessage(line);

				messages.add(msg);
			}
		} catch (Exception ex) {
			log.error("Unable to read CSV file");
			ex.printStackTrace();
		}
		
		log.info("Added " + messages.size() + " events and messages to the queue!");
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
		
		log.info("Created message with id " + msg.getId());
		
		return msg;
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#receiveMessage(com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage)
	 */
	@Override
	public void receiveMessage(ReceivedV2XMessage arg0) {
		// TODO Auto-generated method stub

	}

}
