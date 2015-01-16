/**
 * 
 */
package de.hhu.tbus.applications.testapp;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;

import com.dcaiti.vsimrti.fed.app.api.helper.SafeTimerLong;
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
public class TestApp implements Application {
	
	private long minimalTimerCallInterval = 0;
	private final SafeTimerLong safeTimer = new SafeTimerLong(this);
	
	private Queue<Long> eventTimes = new LinkedList<Long>();
	private Queue<TbusTestMessage> eventMessages = new LinkedList<TbusTestMessage>();
	
	private ApplicationLayer appLayer;
	private CommunicationModule comMod;
	private Logger log;

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#getMinimalTimerCallInterval()
	 */
	@Override
	public long getMinimalTimerCallInterval() {
		return minimalTimerCallInterval;
	}
	
	/**
	 * Retrieves the next event from eventTimes, updates the safe timer and triggers a timer update
	 * @param currentSimTime current simulation time
	 */
	private void setNextEvent(long currentSimTime) {
		long nextInterval = 0;
		
		if (!eventTimes.isEmpty()) {
			nextInterval = eventTimes.poll() - currentSimTime;
		}
		
		minimalTimerCallInterval = nextInterval;
		
		safeTimer.reset();
		appLayer.triggerTimerCallUpdate();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#timerCall(long)
	 */
	@Override
	public void timerCall(long time) {
		// Only respond to our timer calls
		if (!safeTimer.checkTimer(time)) {
			return;
		}
		setNextEvent(time);
		
		TbusTestMessage msg = eventMessages.poll();
		
		if (msg == null) {
			log.error("Got null message from queue, discarding...");
			return;
		}
		
		// Send message to network simulator
		comMod.sendV2XMessage(msg);
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#initialize(com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer)
	 */
	@Override
	public void initialize(ApplicationLayer appLayer) {
		this.appLayer = appLayer;
		this.comMod   = appLayer.getApplicationToFacility().getCommunicationModuleReference();
		this.log      = appLayer.getApplicationToFacility().getLogger();
		
//		byte[] destination = {0,0,0,1};
		DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(new byte[] {0,0,0,1}));//destination, 1));
		SourceAddressContainer sac = appLayer.getApplicationToFacility().generateSourceAddressContainer();
		
		MessageRouting routing = new MessageRouting(dac, sac);
		
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
		
		eventTimes.add(10000000000L);
		eventMessages.add(msg1);
		
		eventTimes.add(15000000000L);
		eventMessages.add(msg2);
		
		setNextEvent(appLayer.getApplicationToFacility().getPoolAccessReference().)
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
