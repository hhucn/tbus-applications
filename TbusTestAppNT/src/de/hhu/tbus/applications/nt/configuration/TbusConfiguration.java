/**
 * 
 */
package de.hhu.tbus.applications.nt.configuration;

import java.io.File;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.OperatingSystem;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.util.UnitLogger;

import de.fraunhofer.fokus.eject.ObjectInstantiation;
import de.hhu.tbus.applications.nt.emergencywarning.configuration.EmergencyWarningAppConfiguration;

/**
 * @author bialon
 *
 */
public class TbusConfiguration<C> {
	/**
	 * Get the configuration for class C in this order:
	 * 1. Node-based configuration
	 * 2. General configuration
	 * 3. Default class configuration
	 * 
	 * @param aClass Class of configuration
	 * @param configFilename Configuration filename
	 * @param os Node's operating system
	 * @param log log reference
	 * @return An instantiated and read configuration
	 * @throws InstantiationException If Serialization failed
	 * @throws IllegalAccessException If constructor visibility is hidden
	 */
	public C readConfiguration(Class<C> aClass, String configFilename, OperatingSystem os, UnitLogger log) throws InstantiationException, IllegalAccessException {
		final ObjectInstantiation<C> oi = new ObjectInstantiation<C>(aClass);
		C config;
		
		File appVehConfig = new File(os.getConfigurationPath().getAbsolutePath() + File.separator + configFilename + "-" + os.getId() + ".json");
		File appConfig = new File(os.getConfigurationPath().getAbsolutePath() + File.separator + configFilename + ".json");

		if (appVehConfig.exists()) {
			config = (C) oi.readFile(appVehConfig);
			log.info("Using vehicle configuration");
			log.info(oi.getLogMessage());
		} else if (appConfig.exists()) {
			config = (C) oi.readFile(appConfig);
			log.info("Using class configuration");
			log.info(oi.getLogMessage());
		} else {
			config = aClass.newInstance();
			log.info("Using default configuration");
		}
		
		if (config instanceof EmergencyWarningAppConfiguration) {
			log.info("Is emergency vehicle: " + os.getId() + " - " + ((EmergencyWarningAppConfiguration) config).isEmergencyVehicle);
		}
		
		return config;
	}
}
