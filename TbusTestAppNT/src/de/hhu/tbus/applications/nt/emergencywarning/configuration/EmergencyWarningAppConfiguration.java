/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning.configuration;

import java.io.Serializable;

/**
 * @author bialon
 *
 */
public class EmergencyWarningAppConfiguration implements Serializable {
	
	/**
	 * Config file name 
	 */
	public static final String configFilename = "emergencyWarningApp";
	
	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = -336117990286037291L;

	/**
	 * Is the current unit an emergency vehicle?
	 */
	public final boolean isEmergencyVehicle = false;
	
	/**
	 * Interval for message sending
	 */
	public final long interval = 1_000_000_000L;
	
	/**
	 * Message timeout
	 * When the receiver gets the message and the following condition is met:
	 * 	now > send_timestamp + timeout
	 * , the message is invalidated and discarded by this client.
	 */
	public final long timeout = 1_500_000_000L;
	
	/**
	 * Offset for first message distribution
	 */
	public final long offset = 2_000_000_000L; 
	
	/**
	 * Broadcast geo radius
	 */
	public final double radius = 10.0;
	
	/**
	 * The speed an affected vehicle shall slow down to
	 */
	public final float slowDownSpeed = 0.0f;
	
	/**
	 * Duration of emergency message obedience (in ms)
	 */
	public final int obeyTime = 2_000;
	
}
