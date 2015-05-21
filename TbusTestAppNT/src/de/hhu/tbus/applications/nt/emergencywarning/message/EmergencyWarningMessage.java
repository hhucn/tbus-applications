/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;

/**
 * @author bialon
 *
 */
public class EmergencyWarningMessage extends EmbeddedMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3051425906236801637L;
	private final EncodedV2XMessage encodedMessage = new EncodedV2XMessage(getLength());
	
	public enum EmergencyType {
		POLICE,
		FIRE_BRIGADE,
		AMBULANCE,
		MILITARY,
		OTHER
	};
	
	private EmergencyType emergencyType;

	/**
	 * Constructor inherited by EmbeddedMessage
	 * @param routing Message routing
	 * @param timestamp Message original timestamp
	 * @param timeout Message timeout
	 */
	public EmergencyWarningMessage(MessageRouting routing, EmergencyType emergencyType, long timestamp, long timeout) {
		super(routing, timestamp, timeout);
		
		this.emergencyType = emergencyType;
	}
	
	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#createEmbeddedMessageWithRoutingAndTimestamp(com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting, long)
	 */
	@Override
	public V2XMessage copy(MessageRouting routing, long timestamp) {
		return new EmergencyWarningMessage(routing, emergencyType, timestamp, timeout);
	}
	
	/**
	 * The messages' emergency type
	 * @return Emergency type
	 */
	public EmergencyType getEmergencyType() {
		return emergencyType;
	}

	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#getLength()
	 */
	@Override
	public int getLength() {
		// Super length and 1 byte for the enum
		return super.getLength() + 1;
	}
	
	/**
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	@Override
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedMessage;
	}

}
