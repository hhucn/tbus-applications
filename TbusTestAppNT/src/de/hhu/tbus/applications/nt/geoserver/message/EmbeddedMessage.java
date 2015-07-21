package de.hhu.tbus.applications.nt.geoserver.message;

import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.message.TbusLogMessage;

public abstract class EmbeddedMessage extends V2XMessage implements TbusLogMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2854498973643305145L;
	/**
	 * Message original timestamp
	 */
	protected final long timestamp;
	/**
	 * Timestamp of message forwarding
	 */
	public long originalTimestamp;
	/**
	 * Message timeout, i.e. critical data is invalid after (timestamp + timeout)
	 */
	protected final long timeout;

	protected EmbeddedMessage(MessageRouting routing, long timestamp, long timeout) {
		super(routing);
		
		this.timestamp = timestamp;
		this.timeout = timeout;
	}
	
	public abstract EmbeddedMessage copy(MessageRouting routing, long copyTimestamp);
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public int getLength() {
		return (Long.SIZE * 2) / Byte.SIZE;
	}
	
	public String getLog() {
		return this.getClass().getSimpleName() + " id " + getId() + " length " + getLength() + " timestamp " + timestamp + " originalTimestamp " + originalTimestamp + " timeout " + timeout;
	}
}