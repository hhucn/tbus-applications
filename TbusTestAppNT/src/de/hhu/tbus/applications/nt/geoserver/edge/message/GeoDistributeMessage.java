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
	private final EmbeddedMessage message;
	private final String roadId;
	private final String nextRoadId;
	private final double lanePos;
	private final double radius;
	private final long timestamp;
	private final EncodedV2XMessage encodedMessage;
	
	/**
	 * @param routing
	 */
	public GeoDistributeMessage(
			EmbeddedMessage message,
			String roadId,
			String nextRoadId,
			double lanePos,
			double radius,
			long timestamp,
			MessageRouting routing) {
		super(routing);
		
		this.message = message;
		this.roadId = roadId;
		this.nextRoadId = nextRoadId;
		this.lanePos = lanePos;
		this.radius = radius;
		this.timestamp = timestamp;
		
		encodedMessage = new EncodedV2XMessage(getSize());
	}
	
	private int getSize() {
		int size = message.getLength() + roadId.length() + nextRoadId.length() + ((Double.SIZE + Double.SIZE + Long.SIZE) / Byte.SIZE);
		
		return (size < 200) ? 200 : size;
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
	 * @return the next road Id
	 */
	public String getNextRoadId() {
		return nextRoadId;
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