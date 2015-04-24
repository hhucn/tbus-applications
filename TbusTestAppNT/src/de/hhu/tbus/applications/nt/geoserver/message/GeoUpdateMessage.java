package de.hhu.tbus.applications.nt.geoserver.message;

import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;

/**
 * A Geoserver update message.
 * This message should be sent on a regular interval by Tbus mobile nodes
 * @author bialon
 *
 */
public class GeoUpdateMessage extends V2XMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Encoded V2X message for further handling
	 */
	private final EncodedV2XMessage encodedMessage;
	private final double longitude;
	private final double latitude;
	private final long timestamp;
	
	/**
	 * Create a new Geoserver Message
	 * @param longitude Postion longitude
	 * @param latitude Position latitude
	 * @param timestamp Timestamp of message
	 * @param id Node id
	 * @param routing Message routing
	 */
	public GeoUpdateMessage(
			double longitude,
			double latitude,
			long timestamp,
			MessageRouting routing) {
		super(routing);
		this.longitude = longitude;
		this.latitude = latitude;
		this.timestamp = timestamp;
		
		// Only encode by size
		encodedMessage = new EncodedV2XMessage((Double.SIZE * 2 + Long.SIZE) / Byte.SIZE);
	}
	
	/**
	 * @return The encoded V2X message
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedMessage;
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
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
}