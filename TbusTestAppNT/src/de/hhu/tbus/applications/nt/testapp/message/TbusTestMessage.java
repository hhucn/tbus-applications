/**
 * 
 */
package de.hhu.tbus.applications.nt.testapp.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

/**
 * @author bialon
 *
 */
public class TbusTestMessage extends V2XMessage {
	
	/**
	 * Serialization version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Encoded V2XMessage
	 */
	private final EncodedV2XMessage encodedV2XMessage;
	
	private final long sendTimestamp;
	private final long recvTimestamp;
	private final int seqNr;
	private final int packetNr;
	private final int totalPacketNr;
	private final int payloadSize;
	private byte[] payload;
	
	private long realRecvTimestamp;

	/**
	 * Initialize a new TbusTestMessage
	 */
	public TbusTestMessage(
			final MessageRouting routing,
			final long sendTimestamp,
			final long recvTimestamp,
			final int seqNr,
			final int packetNr,
			final int totalPacketNr,
			final int payloadSize) {
		super(routing);
		
		this.sendTimestamp = sendTimestamp;
		this.recvTimestamp = recvTimestamp;
		this.seqNr = seqNr;
		this.packetNr = packetNr;
		this.totalPacketNr = totalPacketNr;
		this.payloadSize = payloadSize;// - udpAndIpHeaderLength;
		
		encodedV2XMessage = new EncodedV2XMessage(payloadSize);
	}

	/**
	 * @return the sendTimestamp
	 */
	public long getSendTimestamp() {
		return sendTimestamp;
	}

	/**
	 * @return the recvTimestamp
	 */
	public long getRecvTimestamp() {
		return recvTimestamp;
	}

	/**
	 * @return the seqNr
	 */
	public int getSeqNr() {
		return seqNr;
	}

	/**
	 * @return the packetNr
	 */
	public int getPacketNr() {
		return packetNr;
	}

	/**
	 * @return the totalPacketNr
	 */
	public int getTotalPacketNr() {
		return totalPacketNr;
	}

	/**
	 * @return the payloadSize
	 */
	public int getPayloadSize() {
		return payloadSize;
	}

	/**
	 * @return the payload
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * @see com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage#getEncodedV2XMessage()
	 */
	@Override
	public EncodedV2XMessage getEncodedV2XMessage() {
		return encodedV2XMessage;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TbusTestMessage [sendTimestamp=" + sendTimestamp
				+ ", recvTimestamp=" + recvTimestamp + ", seqNr=" + seqNr
				+ ", packetNr=" + packetNr + ", totalPacketNr=" + totalPacketNr
				+ ", payloadSize=" + payloadSize + "]";
	}

	/**
	 * @return the realRecvTimestamp
	 */
	public long getRealRecvTimestamp() {
		return realRecvTimestamp;
	}

	/**
	 * @param realRecvTimestamp the realRecvTimestamp to set
	 */
	public void setRealRecvTimestamp(long realRecvTimestamp) {
		this.realRecvTimestamp = realRecvTimestamp;
	}
}
