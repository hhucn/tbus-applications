/**
 * 
 */
package de.hhu.tbus.applications.nt.emergencywarning;

import java.util.UUID;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.OperatingSystem;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.util.UnitLogger;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.configuration.TbusConfiguration;
import de.hhu.tbus.applications.nt.emergencywarning.configuration.EmergencyWarningAppConfiguration;
import de.hhu.tbus.applications.nt.emergencywarning.message.EmergencyWarningMessage;
import de.hhu.tbus.applications.nt.emergencywarning.message.EmergencyWarningMessage.EmergencyType;
import de.hhu.tbus.applications.nt.geoserver.edge.client.TbusGeoclient;

/**
 * @author bialon
 *
 */
public class EmergencyWarningApp extends TbusGeoclient {	
	private EmergencyWarningAppConfiguration config;
	
	private void handleEmergencyWarningMessage(EmergencyWarningMessage msg) {		
		if (config.isEmergencyVehicle) {
			// If this is an emergency vehicle, ignore EV messages
			return;
		}
		
		long delay = getOperatingSystem().getSimulationTime() - msg.originalTimestamp; 
		if (delay > msg.getTimeout()) {
			getLog().info("EmergencyWarningMessage timed out - Delay " + delay + "ns (" + (delay - msg.getTimeout()) + "ns too late)");
		} else {
			getLog().info("Slowing down to " + config.slowDownSpeed + " for " + config.obeyTime + "ms");
			getOperatingSystem().slowDown(config.slowDownSpeed, config.obeyTime, null);
		}
	}
	
	@Override
	public void processEvent(Event evt) throws Exception {
		super.processEvent(evt);
		
		if (evt.getResource() == null) {
			long now = getOperatingSystem().getSimulationTime();
			
			getLog().info("Received event at simulation time " + now);
			EmergencyWarningMessage msg = new EmergencyWarningMessage(getDefaultRouting(), EmergencyType.AMBULANCE, now, config.timeout, UUID.randomUUID());
			startGeoBroadcast(msg, config.offset, config.interval, config.radius);
		}
	}
	
	@Override
	protected void initConfig() {
		super.initConfig();
		
		OperatingSystem os = getOperatingSystem();
		UnitLogger log = getLog();
		
		try {
			config = (new TbusConfiguration<EmergencyWarningAppConfiguration>()).readConfiguration(EmergencyWarningAppConfiguration.class, EmergencyWarningAppConfiguration.configFilename, os, log);
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
			// Add start event
			getOperatingSystem().getEventManager().addEvent(new Event(getOperatingSystem().getSimulationTime() + config.offset, this));
		}
	}
	
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage recMsg) {
		super.receiveV2XMessage(recMsg);
		V2XMessage embMsg = recMsg.getMessage();
		
		if (embMsg instanceof EmergencyWarningMessage) {			
			handleEmergencyWarningMessage((EmergencyWarningMessage) embMsg);
		}
	}
	
}
