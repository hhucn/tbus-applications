package com.dcaiti.vsimrti.app.TbusPingApp;

import com.dcaiti.vsimrti.fed.app.DefaultObjectSerialization;
import com.dcaiti.vsimrti.fed.app.api.helper.SafeTimerLong;
import com.dcaiti.vsimrti.fed.app.api.interfaces.Application;
import com.dcaiti.vsimrti.fed.app.api.interfaces.ApplicationLayer;
import com.dcaiti.vsimrti.fed.app.api.interfaces.CommunicationModule;
import com.dcaiti.vsimrti.fed.app.api.interfaces.unitaccess.controller.VehicleController;
import com.dcaiti.vsimrti.fed.app.api.interfaces.unitaccess.provider.VehicleProvider;
import com.dcaiti.vsimrti.fed.app.enums.CamBodyType;
import com.dcaiti.vsimrti.fed.app.messages.CAMessage;
import com.dcaiti.vsimrti.fed.app.messages.RsuAwarenessData;
import com.dcaiti.vsimrti.fed.app.messages.TypedV2XMessage;
import com.dcaiti.vsimrti.fed.app.api.util.ReceivedV2XMessage;
import com.dcaiti.vsimrti.fed.app.messages.VehicleAwarenessData;
import com.dcaiti.vsimrti.rti.objects.MessageType;
import org.slf4j.Logger;

public class TbusPingApp implements Application {
	private ApplicationLayer appLayer;
	private VehicleProvider vp;
	private VehicleController vc;
	private CommunicationModule cm;
	private Logger log;

	private final static long TIME_INTERVAL = 1000000000L;

	private SafeTimerLong safeTimer = new SafeTimerLong(this);

	@Override
	public void initialize(ApplicationLayer appLayer) {
		this.appLayer = appLayer;

		log = appLayer.getApplicationToFacility().getLogger();
		cm = appLayer.getApplicationToFacility().getCommunicationModuleReference();
		vc = appLayer.getApplicationToFacility().getPoolAccessReference().getVehicleControlReference();
		vp = appLayer.getApplicationToFacility().getPoolAccessReference().getVehicleProviderReference();

		log.info("TbusPingApp initialized!");
	}

	@Override
	public void dispose() {
		log.info("TbusPingApp disposed!");
	}

	@Override
	public void receiveMessage(ReceivedV2XMessage msg) {
		log.info("TbusPingApp Received Message!");
	}

	@Override
	public void timerCall(long time) {
		log.info("TbusPingApp timer call!");
	}

	@Override
	public long getMinimalTimerCallInterval() {
		return TIME_INTERVAL;
	}
}
