/**
 * 
 */
package de.hhu.tbus.applications.testapp.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.annotation.Nonnull;

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
	private final byte[] payload;

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
		this.payloadSize = payloadSize;
		
		int remainingPayloadSize = payloadSize - 8 - 8 - 4 - 4 - 4 - 4;
		remainingPayloadSize = (remainingPayloadSize < 0) ? 0 : remainingPayloadSize;
		
		// Create payload with remaining size and fill with random data
		payload = new byte[remainingPayloadSize];
		new Random().nextBytes(payload);

		// Open outputstreams for encoded message creation
		final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		final DataOutputStream dataOut = new DataOutputStream(byteArrayOut);
		
		try {
			dataOut.writeLong(sendTimestamp);
			dataOut.writeLong(recvTimestamp);
			dataOut.writeInt(seqNr);
			dataOut.writeInt(packetNr);
			dataOut.writeInt(totalPacketNr);
			dataOut.writeInt(remainingPayloadSize);
			dataOut.write(payload);
		} catch (IOException ex) {
			System.out.println("Cannot write to output stream: " + ex.getLocalizedMessage());
		}
		
		encodedV2XMessage = new EncodedV2XMessage(byteArrayOut.toByteArray());
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
	@Nonnull
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
	
	

}
