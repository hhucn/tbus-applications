/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.edge.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;
import de.hhu.tbus.applications.nt.message.TbusLogMessage;

/**
 * Distributes a message to a geopoint and radius
 * @author bialon
 */
public class GeoDistributeMessage extends V2XMessage implements TbusLogMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3504281866940316666L;
	
	public enum MessageType {
		START,
		STOP
	}
	
	private final EmbeddedMessage message;
	private final String roadId;
	private final double lanePos;
	private final double radius;
	private final long timestamp;
	private final MessageType type;
	private final EncodedV2XMessage encodedMessage;
	
	/**
	 * @param routing
	 */
	public GeoDistributeMessage(
			EmbeddedMessage message,
			String roadId,
			double lanePos,
			double radius,
			long timestamp,
			MessageType type,
			MessageRouting routing) {
		super(routing);
		
		this.message = message;
		this.roadId = roadId;
		this.lanePos = lanePos;
		this.radius = radius;
		this.timestamp = timestamp;
		this.type = type;
		
		encodedMessage = new EncodedV2XMessage(getSize());
	}
	
	private int getSize() {
		return message.getLength() + roadId.length() + ((Double.SIZE + Double.SIZE + Long.SIZE) / Byte.SIZE);
	}
	
	/**
	 * @return the message
	 */
	public EmbeddedMessage getMessage() {
		return message;
	}
	/**
	 * @return the road id
	 */
	public String getRoadId() {
		return roadId;
	}
	/**
	 * @return the lanepos
	 */
	public double getLanePos() {
		return lanePos;
	}
	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @return Message type
	 */
	public MessageType getType() {
		return type;
	}
	/**
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	@Override
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedMessage;
	}
	
	public String getLog() {
		return this.getClass().getSimpleName() + " id "  + getId() + " contains (" + message.getLog() + ") length " + getSize() + " timestamp " + timestamp + " roadId " + roadId + " lanePos " + lanePos + " radius " + radius;
	}	
}