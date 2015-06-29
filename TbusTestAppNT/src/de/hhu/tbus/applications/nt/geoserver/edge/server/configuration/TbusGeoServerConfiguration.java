/**
 * 
 */
package de.hhu.tbus.applications.nt.geoserver.edge.server.configuration;

import java.io.Serializable;

/**
 * @author bialon
 *
 */
public class TbusGeoServerConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8647722646659474741L;
	
	/**
	 * Configuration file path
	 */
	public final static String configFilename = "GeoServerConfig";
	/**
	 * SUMO net file path
	 */
	public String sumoNetFile = "";
}
