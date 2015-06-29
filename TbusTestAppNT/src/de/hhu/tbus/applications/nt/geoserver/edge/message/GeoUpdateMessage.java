package de.hhu.tbus.applications.nt.geoserver.edge.message;

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
	private final String roadId;
	private final double lanePos;
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
			String roadId,
			double lanePos,
			long timestamp,
			MessageRouting routing) {
		super(routing);
		this.roadId = roadId;
		this.lanePos = lanePos;
		this.timestamp = timestamp;
		
		// Only encode by size
		encodedMessage = new EncodedV2XMessage(((Double.SIZE + Long.SIZE) / Byte.SIZE) + roadId.length());
	}
	
	/**
	 * @return The encoded V2X message
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedMessage;
	}

	/**
	 * @return the roadId
	 */
	public String getRoadId() {
		return roadId;
	}

	/**
	 * @return the lanePos
	 */
	public double getLanePos() {
		return lanePos;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
}