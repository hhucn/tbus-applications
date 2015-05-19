package de.hhu.tbus.applications.nt.geoserver.message;

import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

public abstract class EmbeddedMessage extends V2XMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2854498973643305145L;
	protected final long timestamp;

	protected EmbeddedMessage(MessageRouting routing, long timestamp) {
		super(routing);
		
		this.timestamp = timestamp;
	}
	
	public abstract V2XMessage copy(MessageRouting routing, long timestamp);
	
	public abstract int getLength();
}