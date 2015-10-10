/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning.message;

import java.util.UUID;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;

import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;
import de.hhu.tbus.applications.nt.message.TbusLogMessage;

/**
 * @author bialon
 *
 */
public class EmergencyWarningMessage extends EmbeddedMessage implements TbusLogMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3051425906236801637L;
	
	/**
	 * Minimum message length in Byte
	 */
	private static final int minLength = 40;
	
	private final EncodedV2XMessage encodedMessage;
	
	public enum EmergencyType {
		POLICE,
		FIRE_BRIGADE,
		AMBULANCE,
		MILITARY,
		OTHER
	};
	
	private final EmergencyType emergencyType;

	/**
	 * Constructor inherited by EmbeddedMessage
	 * @param routing Message routing
	 * @param timestamp Message original timestamp
	 * @param timeout Message timeout
	 */
	public EmergencyWarningMessage(MessageRouting routing, EmergencyType emergencyType, long timestamp, long timeout, UUID uuid) {
		super(routing, timestamp, timeout, uuid);
		
		this.emergencyType = emergencyType;
		this.encodedMessage = new EncodedV2XMessage(getLength());
	}
	
	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#createEmbeddedMessageWithRoutingAndTimestamp(com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting, long)
	 */
	@Override
	public EmbeddedMessage copy(MessageRouting routing, long timestamp) {
		EmbeddedMessage copyMessage = new EmergencyWarningMessage(routing, this.emergencyType, timestamp, this.timeout, this.uuid);
		copyMessage.originalTimestamp = this.timestamp;
		
		return copyMessage;
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
		int length = super.getLength() + 1;
		return (length < minLength) ? minLength : length;
	}
	
	/**
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	@Override
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedMessage;
	}
	
	public String getLog() {
		return this.getClass().getSimpleName() + " id " + getId() + " from (" + super.getLog() + ") emergencyType " + emergencyType.toString();
	}
}
