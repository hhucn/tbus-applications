/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

/**
 * @author bialon
 *
 */
public class EmergencyWarningMessage extends EmbeddedMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3051425906236801637L;
	private final EncodedV2XMessage encodedMessage = new EncodedV2XMessage(8);

	public EmergencyWarningMessage(MessageRouting routing, long timestamp) {
		super(routing, timestamp);
	}
	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#createEmbeddedMessageWithRoutingAndTimestamp(com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting, long)
	 */
	@Override
	public V2XMessage copy(MessageRouting routing, long timestamp) {
		
		return new EmergencyWarningMessage(routing, timestamp);
	}

	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#getLength()
	 */
	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return (Long.SIZE / 8);
	}
	/**
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	@Override
	public EncodedV2XMessage getEncodedV2XMessage() {
		// TODO Auto-generated method stub
		return encodedMessage;
	}

}
