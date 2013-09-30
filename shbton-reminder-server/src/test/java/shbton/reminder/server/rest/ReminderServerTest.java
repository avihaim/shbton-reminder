package shbton.reminder.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import shbton.reminder.server.Main;
import shbton.reminder.server.database.ReminderCassandraManger;
import shbton.reminder.server.database.ReminderEventId;
import shbton.reminder.server.manger.ReminderMangerImpl;
import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.obj.ReminderEvent;
import shbton.reminder.server.time.ShbtonGeoLocation;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class ReminderServerTest {

	private static HttpServer server;
	private static WebTarget target;
	private static Keyspace keyspace;
	private static ObjectMapper mapper = new ObjectMapper();
	private static ColumnFamily<String, String> columnFamily;
	private static ColumnFamily<String, Long> userGeoLocationCF;
	private static ColumnFamily<String, ReminderEventId> userRemindersEventsCF;
	private static ColumnFamily<String, String> userNotificationIdsCF;
	 

	@BeforeClass
	public static void init() {
		try {
			initCassandra();

			// start the server
			server = Main.startServer();
			// create the client
			Client c = ClientBuilder
					.newBuilder()
					// The line bellow that registers MOXy feature can be
					// omitted if FEATURE_AUTO_DISCOVERY_DISABLE is
					// not disabled.
					.register(new Main.JsonMoxyConfigurationContextResolver())
					.build();
			target = c.target(Main.BASE_URI);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}

	private static void initCassandra() throws TTransportException,
			IOException, ConfigurationException, ConnectionException {

		EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra.yaml");

		AstyanaxContext<Keyspace> ctx = new AstyanaxContext.Builder()
				.forKeyspace(ReminderCassandraManger.KEYSPACE_NAME)
				.forCluster(ReminderCassandraManger.CLUSTER_NAME)
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl()
								.setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl(
								"reminderConnectionPool").setPort(9160)
								.setMaxConnsPerHost(1)
								.setSeeds("localhost:9160"))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		ctx.start();
		keyspace = ctx.getClient();

		keyspace.createKeyspace(ImmutableMap
				.<String, Object> builder()
				.put("strategy_options",
						ImmutableMap.<String, Object> builder()
								.put("replication_factor", "1").build())
				.put("strategy_class", "SimpleStrategy").build());

		columnFamily = ColumnFamily.newColumnFamily(
				ReminderCassandraManger.COLUMN_FAMILY, StringSerializer.get(),
				StringSerializer.get());

		keyspace.createColumnFamily(
				columnFamily,
				ImmutableMap.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type", "UTF8Type").build());
		
		userGeoLocationCF = ColumnFamily.newColumnFamily(
				ReminderCassandraManger.USER_GEO_LOCATION_CF, StringSerializer.get(),
				LongSerializer.get());

		keyspace.createColumnFamily(
				userGeoLocationCF,
				ImmutableMap.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type", "LongType").build());
		
		userRemindersEventsCF = ColumnFamily.newColumnFamily(
				ReminderCassandraManger.USER_REMINDERS_EVENTS_CF, StringSerializer.get(),
				ReminderCassandraManger.eventSerializer);

		keyspace.createColumnFamily(
				userRemindersEventsCF,
				ImmutableMap.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type", "CompositeType(UTF8Type, TimeUUIDType)").build());
		
		userNotificationIdsCF = ColumnFamily.newColumnFamily(
				ReminderCassandraManger.USER_NOTIFICATION_IDS_CF, StringSerializer.get(),
				StringSerializer.get());

		keyspace.createColumnFamily(
				userNotificationIdsCF,
				ImmutableMap.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type", "UTF8Type").build());
		
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}


	@Test
	public void testPutReminder() throws JsonGenerationException,
			JsonMappingException, IOException, ConnectionException {

		String userId = UUID.randomUUID().toString();
		String reminderId = UUID.randomUUID().toString();
		Reminder reminder = new Reminder(reminderId, "bla bla", true, false, true, 0, 1, 5);
		
		ShbtonGeoLocation shbtonGeoLocation = new ShbtonGeoLocation("Lakewood, NJ", 40.096, -74.222, 0, TimeZone.getTimeZone("America/New_York"));
		String shbtonGeoLocationString = mapper.writeValueAsString(shbtonGeoLocation);
		
		MutationBatch batch = keyspace.prepareMutationBatch();
		
		batch.withRow(userGeoLocationCF, userId).putColumn(System.currentTimeMillis(), shbtonGeoLocationString);
		batch.execute();
		
		
		Response responseMsg = target.path("users/" + userId + "/reminders").request()
				.put(Entity.entity(reminder, MediaType.APPLICATION_JSON_TYPE));
		assertEquals(Status.OK.getStatusCode(), responseMsg.getStatus());

		OperationResult<ColumnList<String>> result = keyspace
				.prepareQuery(columnFamily).getKey(userId).execute();
		ColumnList<String> columns = result.getResult();

		// Lookup columns in response by name
		String reminderString = columns.getColumnByName(reminder.getId())
				.getStringValue();
		
		assertEquals(mapper.writeValueAsString(reminder), reminderString);
		
		List<DateTime> candleLighting = getThisWeekCandleLighting(shbtonGeoLocation);
		List<ReminderEvent> remindersEvent = ReminderMangerImpl.calcThisWeekRemindersEvent(userId, reminder, candleLighting);
		
		assertEquals(1, remindersEvent.size());
		
		for (ReminderEvent reminderEvent : remindersEvent) {
			OperationResult<ColumnList<ReminderEventId>> userRemindersEventsResult = keyspace
					.prepareQuery(userRemindersEventsCF).getKey(makeKey(reminderEvent))
					.withColumnRange(
							ReminderCassandraManger.eventSerializer.buildRange()
												.greaterThanEquals(userId)
												.lessThanEquals(userId).build())
					.execute();
			
			ColumnList<ReminderEventId> userRemindersEventsColumns = userRemindersEventsResult.getResult();
		
			assertEquals(1, userRemindersEventsColumns.size());
			
			for (Column<ReminderEventId> column : userRemindersEventsColumns) {
				assertNotNull(column.getStringValue());
				ReminderEvent event = mapper.readValue(column.getStringValue(), ReminderEvent.class);
				assertEquals(reminder.getText(), event.getText());
				assertEquals(userId, event.getUserId());
			}
		}
	}
	
	private String makeKey(ReminderEvent reminderEvent) {
		DateTime dateTime = new DateTime(reminderEvent.getTime());
		return String.format("%s_%s_%s-%s_%s", dateTime.getYear(),dateTime.getMonthOfYear(),dateTime.getDayOfMonth(),dateTime.getHourOfDay(),dateTime.getMinuteOfHour());
	}
	
	private List<DateTime> getThisWeekCandleLighting(ShbtonGeoLocation shbtongeoLocation) {

		GeoLocation location = new GeoLocation(shbtongeoLocation.getLocationName(), shbtongeoLocation.getLatitude(),
				shbtongeoLocation.getLongitude(), shbtongeoLocation.getElevation(), shbtongeoLocation.getTimeZone());
		ComplexZmanimCalendar czc = new ComplexZmanimCalendar(location);

		czc.getCalendar().set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		DateTime candleLighting = new DateTime(czc.getCandleLighting());
		return Collections.singletonList(candleLighting);
	}
	
	@Test
	public void testNotificationId() {
		
		String userId = UUID.randomUUID().toString();
		
		Response responseMsg = target.path("/users/" + userId +"/notifications").request().post(Entity.entity("notificationId", MediaType.TEXT_PLAIN));
		assertNotNull(responseMsg);
		assertEquals(Status.OK.getStatusCode(), responseMsg.getStatus());
		
	}
}
