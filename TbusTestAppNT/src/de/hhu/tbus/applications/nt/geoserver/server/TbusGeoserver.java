/**
 * Implements a rudimentary Geoserver application running on a RSU
 */
package de.hhu.tbus.applications.nt.geoserver.server;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.RoadSideUnitApplication;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage;
import com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddress;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.SourceAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopologicalDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import de.hhu.tbus.applications.nt.geoserver.message.EmbeddedMessage;
import de.hhu.tbus.applications.nt.geoserver.message.GeoDistributeMessage;
import de.hhu.tbus.applications.nt.geoserver.message.GeoUpdateMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author bialon
 *
 */
public class TbusGeoserver extends RoadSideUnitApplication {
	private Connection connection = null;
	private PreparedStatement registerStatement = null;
	private PreparedStatement geoRadiusStatement = null;
	
	private static DestinationAddress address = null;
	
	private final static String tableName = "positions";
	private final static String createTableSql = "CREATE TABLE " + tableName + " (ip INTEGER(4) UNIQUE, timestamp INTEGER, longitude REAL, latitude REAL);";
	
	// 1: ip, 2: timestamp, 3: longitude, 4: latitude
	private final static String registerStatementSql = "INSERT OR REPLACE INTO " + tableName +" (ip, timestamp, longitude, latitude) " +
			"VALUES (?, ?, ?, ?);";
	// 1,2: other longitude, 3,4: other latitude, 5,6: radius, 7: timestamp now, 8: timeout
	private final static String geoRadiusStatementSql = "SELECT ip FROM " + tableName + " WHERE "+
			"((((longitude - ?) * (longitude - ?)) + ((latitude - ?) * (latitude - ?))) < (? * ?)) AND ((? - timestamp) <= ?);";
	
	/**
	 * @see com.dcaiti.vsimrti.rti.eventScheduling.EventProcessor#processEvent(com.dcaiti.vsimrti.rti.eventScheduling.Event)
	 */
	@Override
	public void processEvent(Event evt) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#afterGetAndResetUserTaggedValue()
	 */
	@Override
	public void afterGetAndResetUserTaggedValue() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#beforeGetAndResetUserTaggedValue()
	 */
	@Override
	public void beforeGetAndResetUserTaggedValue() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onApplicationSpecificMessage(com.dcaiti.vsimrti.rti.messages.ApplicationSpecificMessage)
	 */
	@Override
	public void onApplicationSpecificMessage(ApplicationSpecificMessage asm) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#onSumoTraciByteArrayMessageResponse(com.dcaiti.vsimrti.rti.objects.SumoTraciByteArrayMessageResponse)
	 */
	@Override
	public void onSumoTraciByteArrayMessageResponse(
			SumoTraciByteArrayMessageResponse stbamr) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#receiveV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage)
	 */
	@Override
	public void receiveV2XMessage(ReceivedV2XMessage recvMsg) {
		V2XMessage msg = recvMsg.getMessage();
		
		if (msg instanceof GeoUpdateMessage) {
			handleUpdateMessage((GeoUpdateMessage) msg);
		} else if (msg instanceof GeoDistributeMessage) {
			handleDistributeMessage((GeoDistributeMessage) msg);
		} else {
			// TODO
		}

	}
	
	private void handleUpdateMessage(GeoUpdateMessage msg) {
		double longitude = msg.getLongitude();
		double latitude = msg.getLatitude();
		long timestamp = msg.getTimestamp();
		int sourceIp = ipToInteger(msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address());
		
		try {
			registerStatement.setInt(1, sourceIp);
			registerStatement.setLong(2, timestamp);
			registerStatement.setDouble(3, longitude);
			registerStatement.setDouble(4, latitude);
			
			registerStatement.executeUpdate();
		} catch (SQLException ex) {
			getLog().error("Cannot update information from message " + msg.getId() + ", exception: " + ex.getLocalizedMessage());
		}
		
		getLog().info("Updated vehicle with address " + msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address() +
				" and position (" + longitude + ", " + latitude + ") sent at " + timestamp);
	}
	
	private void handleDistributeMessage(GeoDistributeMessage msg) {
		double longitude = msg.getLongitude();
		double latitude = msg.getLatitude();
		double radius = msg.getRadius();
		long timeout = msg.getTimeout();
		long msgTimestamp = msg.getTimestamp();
		long nowTimestamp = getOperatingSystem().getSimulationTime();
		InetAddress sourceIp = msg.getRouting().getSourceAddressContainer().getSourceAddress().getIPv4Address();
		EmbeddedMessage embeddedMessage = msg.getMessage();
		
		// Get registered IPs within range and timeout
		try {
			geoRadiusStatement.setDouble(1, longitude);
			geoRadiusStatement.setDouble(2, longitude);
			geoRadiusStatement.setDouble(3, latitude);
			geoRadiusStatement.setDouble(4, latitude);
			geoRadiusStatement.setDouble(5, radius);
			geoRadiusStatement.setDouble(6, radius);
			geoRadiusStatement.setLong(7, nowTimestamp);
			geoRadiusStatement.setLong(8, timeout);

			ResultSet results = geoRadiusStatement.executeQuery();

			while (results.next()) {
				InetAddress destinationIp = integerToIp(results.getInt(1));
				
				// Do not inform the sender
				if (destinationIp.equals(sourceIp)) {
					continue;
				}
				
				DestinationAddressContainer dac = DestinationAddressContainer.createTopologicalDestinationAddressAdHoc(new TopologicalDestinationAddress(new DestinationAddress(destinationIp), 1));
				SourceAddressContainer sac = getOperatingSystem().generateSourceAddressContainer();
				
				MessageRouting routing = new MessageRouting(dac, sac);
				
				V2XMessage forwardMsg = embeddedMessage.copy(routing, msgTimestamp);
				getOperatingSystem().sendV2XMessage(forwardMsg);
				
				getLog().info("Forwarded GeoDistributeMessage content " + forwardMsg.getClass() + " to " + destinationIp + " at " + getOperatingSystem().getSimulationTime());
			}
			
			results.close();
		} catch (SQLException ex) {
			getLog().error("Unable to retrieve IPs within range, exception: " + ex.getLocalizedMessage());
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#setUp()
	 */
	@Override
	public void setUp() {
		// Set own IP address
		address = new DestinationAddress(getOperatingSystem().getAddress().getIPv4Address());
		
		final String sqliteDriver = "org.sqlite.JDBC";
		
		try {
			Class.forName(sqliteDriver);
			connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			
			// Create table
			Statement createTable = connection.createStatement();
			createTable.executeUpdate(createTableSql);
			createTable.close();

			// Prepare statements
			registerStatement = connection.prepareStatement(registerStatementSql);
			geoRadiusStatement = connection.prepareStatement(geoRadiusStatementSql);
		} catch (ClassNotFoundException ex) {
			getLog().error("Class \"" + sqliteDriver + "\" not found, exception: " + ex.getMessage());
		} catch (SQLException ex) {
			getLog().error("Cannot open SQLite database, exception: " + ex.getMessage());
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#tearDown()
	 */
	@Override
	public void tearDown() {
		try {
			if (registerStatement != null) registerStatement.close();
			if (geoRadiusStatement != null) geoRadiusStatement.close();
			if (connection != null) connection.close();
		} catch (SQLException ex) {
			getLog().error("Error while closing database connection, exception: " + ex.getLocalizedMessage());
		}
	}

	/**
	 * @see com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.Application#unableToSendV2XMessage(com.dcaiti.vsimrti.rti.objects.v2x.UnableToSendV2XMessage)
	 */
	@Override
	public void unableToSendV2XMessage(UnableToSendV2XMessage msg) {
		// TODO Auto-generated method stub

	}

	private int ipToInteger(InetAddress inetAddress) {
		int result = 0;
		byte[] bytes = inetAddress.getAddress();
		
		if (bytes.length != 4) {
			getLog().error("Cannot convert addresses other than IPv4!");
			return 0;
		}
		
		result |= (bytes[3] & 0xff) << 24;
		result |= (bytes[2] & 0xff) << 16;
		result |= (bytes[1] & 0xff) << 8;
		result |= (bytes[0] & 0xff);
		
		return result;
	}
	
	private InetAddress integerToIp(int ip) {
		byte[] bytes = new byte[4];
		
		bytes[3] = (byte) ((ip >> 24) & 0xff);
		bytes[2] = (byte) ((ip >> 16) & 0xff);
		bytes[1] = (byte) ((ip >> 8) & 0xff);
		bytes[0] = (byte) (ip & 0xff);
		
		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			return null;
		}
	}
	
	public static final DestinationAddress getAddress() {
		return address;
	}
}
