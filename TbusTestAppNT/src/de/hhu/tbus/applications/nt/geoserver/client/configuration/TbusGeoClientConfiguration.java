/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.client.configuration;

import java.io.Serializable;

/**
 * @author bialon
 *
 */
public class TbusGeoClientConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2593506890841706231L;

	/**
	 * Emergency vehicle flag
	 */
	public boolean isEmergencyVehicle = false;
	
	/**
	 * Broadcast interval
	 */
	public long interval = 1_000_000_000L;
	
	/**
	 * Broadcast radius
	 */
	public double radius = 1.0;
}
