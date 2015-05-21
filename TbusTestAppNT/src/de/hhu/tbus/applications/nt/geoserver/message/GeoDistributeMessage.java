/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

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
	private final double longitude;
	private final double latitude;
	private final double radius;
	private final long timestamp;
	private final EncodedV2XMessage encodedMessage;
	
	/**
	 * @param routing
	 */
	public GeoDistributeMessage(
			EmbeddedMessage message,
			double longitude,
			double latitude,
			double radius,
			long timestamp,
			MessageRouting routing) {
		super(routing);
		
		this.message = message;
		this.longitude = longitude;
		this.latitude = latitude;
		this.radius = radius;
		this.timestamp = timestamp;
		
		encodedMessage = new EncodedV2XMessage(message.getLength() + ((Double.SIZE * 3 + Long.SIZE) / Byte.SIZE));
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
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
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