/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning.configuration;

import java.io.Serializable;

/**
 * @author bialon
 *
 */
public class EmergencyWarningAppETSIConfiguration implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7210227704065980107L;
	
	/**
	 * Config file name 
	 */
	public static final String configFilename = "emergencyWarningAppETSI";

	/**
	 * Is the current unit an emergency vehicle?
	 */
	public boolean isEmergencyVehicle = false;
	
	/**
	 * Interval for message sending
	 */
	public long interval = 100_000_000L;
	
	/**
	 * Message timeout
	 * When the receiver gets the message and the following condition is met:
	 * 	now > send_timestamp + timeout
	 * , the message is invalidated and discarded by this client.
	 */
	public long timeout = 150_000_000L;
	
	/**
	 * Offset for first message distribution
	 */
	public long offset = 5_000_000_000L; 
	
	/**
	 * Broadcast geo radius
	 */
	public double radius = 10.0;
	
	/**
	 * The speed an affected vehicle shall slow down to
	 */
	public float slowDownSpeed = 0.0f;
	
	/**
	 * Duration of emergency message obedience (in ms)
	 */
	public int obeyTime = 200;
}
