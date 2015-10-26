/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.geopoint.client.configuration;

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
	 *  Interval for GeoUpdate (in ns)
	 */
	public long interval = 1_000_000_000L;
	
	/**
	 *  Offset for first GeoUpdate (in ns)
	 */
	public long offset = 2_000_000_000L;
	
}
