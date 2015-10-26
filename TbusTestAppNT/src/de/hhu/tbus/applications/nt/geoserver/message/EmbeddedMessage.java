package de.hhu.tbus.applications.nt.geoserver.message;

import java.util.UUID;

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
	
	protected final UUID uuid;

	protected EmbeddedMessage(MessageRouting routing, long timestamp, long timeout, UUID uuid) {
		super(routing);
		
		this.timestamp = timestamp;
		this.timeout = timeout;
		this.uuid = uuid;
	}
	
	public abstract EmbeddedMessage copy(MessageRouting routing, long copyTimestamp);
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public int getLength() {
		// 16 byte = UUID length (128 bit)
		return (Long.SIZE * 3) / Byte.SIZE + 16;
	}
	
	public String getLog() {
		return this.getClass().getSimpleName() + " id " + getId() + " length " + getLength() + " timestamp " + timestamp + " originalTimestamp " + originalTimestamp + " timeout " + timeout + " uuid " + uuid;
	}
}