/**
 * 
 */
package de.hhu.tbus.applications.testapp;

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
			
			log.info("Received at " + recMsg.getTime() + ": " + tbusMsg);
			log.info("Difference: " + (Math.abs(recMsg.getTime() - tbusMsg.getRecvTimestamp())) + "ns");
		}
	}

}
