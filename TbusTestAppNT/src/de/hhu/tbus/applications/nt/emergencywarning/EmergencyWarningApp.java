/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning;

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
	
	private void handleEmergencyWarningMessage(EmergencyWarningMessage msg) {
		getLog().info("Slowing down to " + config.slowDownSpeed + " for " + config.obeyTime + "ms");
		getOperatingSystem().slowDown(config.slowDownSpeed, config.obeyTime, null);
	};
	
	@Override
	protected void initConfig() {
		super.initConfig();
		
		try {
			config = (new TbusConfiguration<EmergencyWarningAppConfiguration>()).readConfiguration(EmergencyWarningAppConfiguration.class, EmergencyWarningAppConfiguration.configFilename, getOperatingSystem(), getLog());
		} catch (InstantiationException | IllegalAccessException ex) {
			getLog().error("Cannot instantiate configuration object, using default configuration: ", ex);
			
			config = new EmergencyWarningAppConfiguration();
		}
	}
	
	@Override
	public void setUp() {
		super.setUp();
		
		if (config.isEmergencyVehicle) {
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
