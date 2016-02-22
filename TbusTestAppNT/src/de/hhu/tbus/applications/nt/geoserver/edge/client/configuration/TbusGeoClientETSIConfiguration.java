/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.edge.client.configuration;

import java.io.Serializable;

/**
 * @author bialon
 *
 */
public class TbusGeoClientETSIConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2593506890841706231L;
	
	public final static String configFilename = "GeoClientConfig";
	
	/**
	 *  Interval for GeoUpdate (in ns)
	 */
	public long interval = 1_000_000_000L;
	
	/**
	 *  Offset for first GeoUpdate (in ns)
	 */
	public long offset = 5_000_000_000L;
	
	/**
	 * Default value for roadId
	 */
	public String defaultRoadId = "defaultroadid";
	
	/**
	 * Default value for lanePos
	 */
	public double defaultLanePos = 0.0d;
	
	/**
	 * Default value for shouldTransmit (i.e. if this node should send position update data)
	 */
	public boolean shouldTransmit = true;
	
}
