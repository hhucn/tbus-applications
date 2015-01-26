/**
 * 
 */
package de.hhu.tbus.applications.testapp;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;

import com.dcaiti.vsimrti.fed.app.api.interfaces.Application;
import com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer;
import com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage;

/**
 * @author bialon
 *
 */
public abstract class TbusApplication implements Application {
	protected ApplicationLayer appLayer;
	protected Logger log;
	private long currentTimerCallInterval = 1000000000L;
	private long currentEventTime = 0L;
	private Queue<Long> eventTimes = new LinkedList<Long>();
	
	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#getMinimalTimerCallInterval()
	 */
	@Override
	public final long getMinimalTimerCallInterval() {
		return currentTimerCallInterval;
	}
	
	/**
	 * Updates the timer interval for narrowing down the time for the next planned event
	 * @param currentTime current simulation time in ns
	 * @return True if the interval has changed, otherwise false
	 */
	private final boolean updateTimerCallInterval(long currentTime) {
		long oldInterval = currentTimerCallInterval;
		long diff = currentEventTime - currentTime - currentTimerCallInterval;
		
		if (diff < 0) {
			// Next event is in the past, we stop setting our timers here
			currentTimerCallInterval = 0L;
			log.info("Stopped timer, event " + currentEventTime + " is in the past (current time: " + currentTime + ")");
		} else {
			// Instead of log_10(diff) we use the string length for exponent calculation
			int exp = Long.toString(diff).length() - 1;
			currentTimerCallInterval = BigInteger.TEN.pow(exp).longValue();
		}
		
		// Return true if the interval has changed, otherwise false
		return (oldInterval != currentTimerCallInterval);
	}
	
	protected final void addEvent(long time) {
		eventTimes.add(time);
	}
	
	/**
	 * Called on timerCall, TBUS application logic shall be placed here
	 */
	protected abstract void timerAction(long time); 

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.TimerCall#timerCall(long)
	 */
	@Override
	public final void timerCall(long time) {
		if (time == currentEventTime) {
			// Call TBUS application timer handling
			timerAction(time);
			
			// Set new time if in the future
			while (currentEventTime <= time && !eventTimes.isEmpty()) {
				currentEventTime = eventTimes.poll();
			}
		}
		
		// Trigger a TimerCall update if our interval has changed
		if (updateTimerCallInterval(time)) {
			appLayer.triggerTimerCallUpdate();
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#dispose()
	 */
	@Override
	public void dispose() {}

	/**
	 * Start event scheduling
	 */
	protected final void start() {
		long currentTime = appLayer.getApplicationToFacility().getPoolAccessReference().getBasicProviderReference().getCurrentTime();
		int discardedMessages = 0;
		
		while (!eventTimes.isEmpty() && currentEventTime <= (currentTime + currentTimerCallInterval)) {
			currentEventTime = eventTimes.poll();
			discardedMessages++;
		}
		
		log.info(currentTime + ": Discarded " + discardedMessages + " messages because of too early start time, starting with event at time " + currentEventTime);
		
		updateTimerCallInterval(currentTime);
	}
	
	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#initialize(com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer)
	 */
	@Override
	public void initialize(ApplicationLayer appLayer) {
		this.appLayer = appLayer;
		log = appLayer.getApplicationToFacility().getLogger();
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.app.api.interfaces.Application#receiveMessage(com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage)
	 */
	@Override
	public void receiveMessage(ReceivedV2XMessage msg) {}

}
