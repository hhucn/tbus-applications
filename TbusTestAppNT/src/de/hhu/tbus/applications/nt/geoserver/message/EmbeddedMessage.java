package de.hhu.tbus.applications.nt.geoserver.message;

import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

public abstract class EmbeddedMessage {
	public EmbeddedMessage() {}
	
	public abstract V2XMessage createEmbeddedMessageWithRoutingAndTimestamp(MessageRouting routing, long timestamp);
	
	public abstract int getLength();
}