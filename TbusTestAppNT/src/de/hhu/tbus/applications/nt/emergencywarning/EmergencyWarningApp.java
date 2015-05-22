/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.OperatingSystem;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.util.UnitLogger;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.configuration.TbusConfiguration;
import de.hhu.tbus.applications.nt.emergencywarning.configuration.EmergencyWarningAppConfiguration;
import de.hhu.tbus.applications.nt.emergencywarning.message.EmergencyWarningMessage;
import de.hhu.tbus.applications.nt.emergencywarning.message.EmergencyWarningMessage.EmergencyType;
import de.hhu.tbus.applications.nt.geoserver.client.TbusGeoclient;

/**
 * @author bialon
 *
 */
public class EmergencyWarningApp extends TbusGeoclient {	
	private EmergencyWarningAppConfiguration config;
	
	/**
	 * Config file name 
	 */
	public static final String configFilename = "emergencyWarningApp";
	
	private void handleEmergencyWarningMessage(EmergencyWarningMessage msg) {
		getLog().info("Slowing down to " + config.slowDownSpeed + " for " + config.obeyTime + "ms");
		getOperatingSystem().slowDown(config.slowDownSpeed, config.obeyTime, null);
	};
	
	@Override
	protected void initConfig() {
		super.initConfig();
		
		OperatingSystem os = getOperatingSystem();
		UnitLogger log = getLog();
		
		try {
			config = (new TbusConfiguration<EmergencyWarningAppConfiguration>()).readConfiguration(EmergencyWarningAppConfiguration.class, configFilename, os, log);
		} catch (InstantiationException | IllegalAccessException ex) {
			getLog().error("Cannot instantiate configuration object, using default configuration: ", ex);
			
			config = new EmergencyWarningAppConfiguration();
		}
	}
	
	@Override
	public void setUp() {
		super.setUp();
		
		getLog().info("Is emergency vehicle: " + config.isEmergencyVehicle);
		
		if (config.isEmergencyVehicle) {
			getLog().info("Now acting as emergency vehicle, geobroadcast starting in " + config.offset + "ns");
			EmergencyWarningMessage msg = new EmergencyWarningMessage(getDefaultRouting(), EmergencyType.AMBULANCE, getOperatingSystem().getSimulationTime(), config.timeout);
			startGeoBroadcast(msg, config.offset, config.interval, config.radius);
		}
	}
	
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage recMsg) {
		V2XMessage embMsg = recMsg.getMessage();
		
		if (embMsg instanceof EmergencyWarningMessage) {
			getLog().info("Received emergency warning message at " + getOperatingSystem().getSimulationTime());
			
			handleEmergencyWarningMessage((EmergencyWarningMessage) embMsg);
		}
	}
	
}
