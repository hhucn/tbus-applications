/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning.message;

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
	private final EncodedV2XMessage encodedMessage;
	
	public enum EmergencyType {
		POLICE,
		FIRE_BRIGADE,
		AMBULANCE,
		MILITARY,
		OTHER
	};
	
	private final EmergencyType emergencyType;
	private final String roadId;
	private final double lanePos;

	/**
	 * Constructor inherited by EmbeddedMessage
	 * @param routing Message routing
	 * @param timestamp Message original timestamp
	 * @param timeout Message timeout
	 */
	public EmergencyWarningMessage(MessageRouting routing, EmergencyType emergencyType, String roadId, double lanePos, long timestamp, long timeout) {
		super(routing, timestamp, timeout);
		
		this.emergencyType = emergencyType;
		this.roadId = roadId;
		this.lanePos = lanePos;
		
		this.encodedMessage = new EncodedV2XMessage(getLength());
	}
	
	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#createEmbeddedMessageWithRoutingAndTimestamp(com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting, long)
	 */
	@Override
	public EmbeddedMessage copy(MessageRouting routing, long timestamp) {
		long originalTimestamp = this.timestamp;
		EmbeddedMessage copyMessage = new EmergencyWarningMessage(routing, this.emergencyType, this.roadId, this.lanePos, timestamp, this.timeout);
		copyMessage.originalTimestamp = originalTimestamp;
		
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
	 * The messages' road id
	 * @return road id
	 */
	public String getRoadId() {
		return roadId;
	}
	
	/**
	 * The messages' lane position
	 * @return Lane position
	 */
	public double getLanePos() {
		return lanePos;
	}

	/**
	 * @see de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage#getLength()
	 */
	@Override
	public int getLength() {
		// Super length and 1 byte for the enum
		int length = super.getLength() + 1 + (Double.SIZE / Byte.SIZE) + roadId.length();
		return (length < 200) ? 200 : length;
	}
	
	/**
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	@Override
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedMessage;
	}
	
	public String getLog() {
		return this.getClass().getSimpleName() + " id " + getId() + " from (" + super.getLog() + ") emergencyType " + emergencyType.toString() + " roadId " + roadId + " lanePos " + lanePos;
	}
}
