/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.edge.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;

/**
 * Distributes a message to a geopoint and radius
 * @author bialon
 */
public class GeoDistributeMessage extends V2XMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3504281866940316666L;
	private final EmbeddedMessage message;
	private final String roadId;
	private final double radius;
	private final long timestamp;
	private final EncodedV2XMessage encodedMessage;
	
	/**
	 * @param routing
	 */
	public GeoDistributeMessage(
			EmbeddedMessage message,
			String roadId,
			double radius,
			long timestamp,
			MessageRouting routing) {
		super(routing);
		
		this.message = message;
		this.roadId = roadId;
		this.radius = radius;
		this.timestamp = timestamp;
		
		encodedMessage = new EncodedV2XMessage(message.getLength() + roadId.length() + ((Double.SIZE + Long.SIZE) / Byte.SIZE));
	}
	
	/**
	 * @return the message
	 */
	public EmbeddedMessage getMessage() {
		return message;
	}
	/**
	 * @return the longitude
	 */
	public String getRoadId() {
		return roadId;
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
	
}