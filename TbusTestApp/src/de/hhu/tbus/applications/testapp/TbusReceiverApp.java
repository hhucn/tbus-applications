/**
 * 
 */
package de.hhu.tbus.applications.testapp;

import java.util.HashMap;

import org.slf4j.Logger;

import com.dcaiti.vsimrti.fed.app.api.interfaces.Application;
import com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer;
import com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.testapp.message.TbusTestMessage;

/**
 * @author bialon
 *
 */
public class TbusReceiverApp implements Application {
	private Logger log;
	
	private class TbusAggregation {
		public int packetsArrived;
		public int packetsSend;
		public long firstPacketDelay;
		public int lastPacketNr;
		public long lastPacketDelay;
	}
	
	private HashMap<Integer, TbusAggregation> statistics = new HashMap<Integer, TbusAggregation>(60);

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#getMinimalTimerCallInterval()
	 */
	@Override
	public long getMinimalTimerCallInterval() {
		return 0L;
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#timerCall(long)
	 */
	@Override
	public void timerCall(long time) {
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#dispose()
	 */
	@Override
	public void dispose() {
		TbusAggregation stat;
		log.info("====================================== Rx Statistics ======================================");
		for (Integer key: statistics.keySet()) {
			stat = statistics.get(key);
			log.info("Statistics: Sequence " + key + " first delay " + stat.firstPacketDelay + " last delay " + stat.lastPacketDelay + " droprate " + (1.0 - ((double) stat.packetsArrived / stat.packetsSend)));
		}
		log.info("==================================== End Rx Statistics ====================================");
		
		statistics.clear();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#initialize(com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer)
	 */
	@Override
	public void initialize(ApplicationLayer appLayer) {
		log = appLayer.getApplicationToFacility().getLogger();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#receiveMessage(com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage)
	 */
	@Override
	public void receiveMessage(ReceivedV2XMessage recMsg) {
		
		V2XMessage msg = recMsg.getMessage();
		TbusTestMessage tbusMsg;
		
		if (msg instanceof TbusTestMessage) {
			tbusMsg = (TbusTestMessage) msg;
			long diff = recMsg.getTime() - tbusMsg.getRecvTimestamp();
			long diff2 = recMsg.getTime() - tbusMsg.getRealRecvTimestamp();
			
			log.info("Received message with id " + msg.getId() + " at " + recMsg.getTime() + ": " + tbusMsg);
			log.info("Difference: " + diff + "ns (" + ((double) diff / 1000000000) + "s) RealWorld-Difference: " + diff2);
			log.info("Plotline thisSim: " + recMsg.getTime() + " otherSim: " + tbusMsg.getRecvTimestamp() + " real: " + tbusMsg.getRealRecvTimestamp());
			
			// Aggregate statistics
			if (statistics.containsKey(tbusMsg.getSeqNr())) {
				TbusAggregation stat = statistics.get(tbusMsg.getSeqNr());
				if (tbusMsg.getPacketNr() > stat.lastPacketNr) {
					stat.lastPacketNr = tbusMsg.getPacketNr();
					stat.lastPacketDelay = diff;
				}
				// Increment packets arrived
				stat.packetsArrived++;
			} else {
				TbusAggregation stat = new TbusAggregation();
				stat.firstPacketDelay = diff;
				stat.lastPacketDelay = diff;
				stat.lastPacketNr = tbusMsg.getPacketNr();
				stat.packetsSend = tbusMsg.getTotalPacketNr();
				stat.packetsArrived = 1;
				
				statistics.put(tbusMsg.getSeqNr(), stat);
			}
		}
	}

}
