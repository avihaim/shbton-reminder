package shbton.reminder.server.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shbton.reminder.server.Main;
import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.obj.ReminderEvent;
import shbton.reminder.server.time.ShbtonGeoLocation;

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
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.query.ColumnFamilyQuery;
import com.netflix.astyanax.serializers.AnnotatedCompositeSerializer;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.netflix.astyanax.util.TimeUUIDUtils;

public class ReminderCassandraManger implements ReminderDataBaseManger {

	private static final Logger logger = LoggerFactory
			.getLogger(ReminderCassandraManger.class);

	public static final String COLUMN_FAMILY = "usersReminders";
	public static final String USER_GEO_LOCATION_CF = "userGeoLocation";
	public static final String USER_REMINDERS_EVENTS_CF = "userRemindersEvents";
	public static final String USER_NOTIFICATION_IDS_CF = "userNotificationIds";
	// public static final String LAST_PUSH_RUN_CF = "lastPushRun";
	public static final String KEYSPACE_NAME = "remindersKeyspace";
	public static final String CLUSTER_NAME = "remindersCluster";
	private int WEEK = 60 * 60 * 24 * 7;

	public static AnnotatedCompositeSerializer<ReminderEventId> eventSerializer = new AnnotatedCompositeSerializer<ReminderEventId>(
			ReminderEventId.class);
	private static ObjectMapper mapper = new ObjectMapper();

	private Keyspace keyspace;
	private ColumnFamily<String, String> usersRemindersCF;
	private ColumnFamily<String, Long> userGeoLocationCF;
	private ColumnFamily<String, ReminderEventId> userRemindersEventsCF;
	private ColumnFamily<String, String> userNotificationIdsCF;

	// private ColumnFamily<String, String> lastPushRun;
 
	public ReminderCassandraManger() {
		AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
				.forCluster(CLUSTER_NAME)
				.forKeyspace(KEYSPACE_NAME)
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl()
								.setDiscoveryType(NodeDiscoveryType.NONE))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl(
								"reminderConnectionPool").setPort(9160)
								.setMaxConnsPerHost(5)
								.setSeeds(Main.CASSANDRA_HOST))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		context.start();
		keyspace = context.getClient();

		usersRemindersCF = new ColumnFamily<String, String>(COLUMN_FAMILY, // Column
																			// Family
																			// Name
				StringSerializer.get(), // Key Serializer
				StringSerializer.get()); // Column Serializer

		userGeoLocationCF = new ColumnFamily<String, Long>(
				USER_GEO_LOCATION_CF, StringSerializer.get(),
				LongSerializer.get());

		userRemindersEventsCF = new ColumnFamily<String, ReminderEventId>(
				USER_REMINDERS_EVENTS_CF, StringSerializer.get(),
				eventSerializer);
		
		userNotificationIdsCF = new ColumnFamily<String, String>(
				USER_NOTIFICATION_IDS_CF, StringSerializer.get(),
				StringSerializer.get());
		// lastPushRun =
		// new ColumnFamily<String, String>(
		// LAST_PUSH_RUN_CF, StringSerializer.get(), StringSerializer.get());

	}

	@Override
	public void addReminder(String userId, Reminder reminder,
			List<ReminderEvent> remindersEvent) {

		MutationBatch m = keyspace.prepareMutationBatch();

		try {
			m.withRow(usersRemindersCF, userId).putColumn(reminder.getId(),
					mapper.writeValueAsString(reminder));

			for (ReminderEvent reminderEvent : remindersEvent) {

				String key = makeKey(reminderEvent);

				m.withRow(userRemindersEventsCF, key).putColumn(
						new ReminderEventId(userId,
								TimeUUIDUtils.getTimeUUID(reminderEvent
										.getTime())),
						mapper.writeValueAsString(reminderEvent), WEEK * 2);
			}

			m.execute();

		} catch (JsonGenerationException | JsonMappingException e) {
			logger.error("JsonException ", e);
		} catch (IOException e) {
			logger.error("IOException ", e);
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}

	}

	private String makeKey(ReminderEvent reminderEvent) {
		DateTime dateTime = new DateTime(reminderEvent.getTime());
		return format(dateTime);
	}

	private String format(DateTime dateTime) {
		return String.format("%s_%s_%s-%s_%s", dateTime.getYear(),
				dateTime.getMonthOfYear(), dateTime.getDayOfMonth(),
				dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
	}

	private String makeKey(ReminderEventId reminderEvent) {
		DateTime dateTime = new DateTime(reminderEvent.getTimestamp()
				.timestamp());
		return format(dateTime);
	}

	@Override
	public ShbtonGeoLocation getUserGeoLocation(String userId) {
		try {
			OperationResult<ColumnList<Long>> result = keyspace
					.prepareQuery(userGeoLocationCF).getKey(userId)
					.withColumnRange(Long.MAX_VALUE, 0l, true, 1).execute();

			ColumnList<Long> columnList = result.getResult();

			if (!columnList.isEmpty()) {
				String userGeoLocationString = columnList.getColumnByIndex(0)
						.getStringValue();
				return mapper.readValue(userGeoLocationString,
						ShbtonGeoLocation.class);

			}

		} catch (JsonGenerationException | JsonMappingException e) {
			logger.error("JsonException ", e);
		} catch (IOException e) {
			logger.error("IOException ", e);
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}

		return null;
	}

	@Override
	public void addUserGeoLocation(String userId,
			ShbtonGeoLocation shbtongeoLocation) {
		try {
			String shbtonGeoLocationString = mapper
					.writeValueAsString(shbtongeoLocation);
			MutationBatch batch = keyspace.prepareMutationBatch();

			batch.withRow(userGeoLocationCF, userId).putColumn(
					System.currentTimeMillis(), shbtonGeoLocationString);
			batch.execute();
		} catch (IOException e) {
			logger.error("IOException ", e);
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}

	}

	@Override
	public List<ReminderEvent> getNowReminderEvents() {

		List<ReminderEvent> reminderEvents = new ArrayList<>();

		try {
			OperationResult<ColumnList<ReminderEventId>> nowRemindersEventsResult = keyspace
					.prepareQuery(userRemindersEventsCF).getKey(getKey())
					.execute();

			ColumnList<ReminderEventId> result = nowRemindersEventsResult
					.getResult();
			for (Column<ReminderEventId> column : result) {
				reminderEvents.add(mapper.readValue(column.getStringValue(),
						ReminderEvent.class));
			}

		} catch (JsonGenerationException | JsonMappingException e) {
			logger.error("JsonException ", e);
		} catch (IOException e) {
			logger.error("IOException ", e);
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}
		return reminderEvents;
	}

	private String getKey() {
		DateTime dateTime = DateTime.now();
		return format(dateTime);
	}

	@Override
	public List<Reminder> getUserReminders(String userId) {

		List<Reminder> reminders = new ArrayList<>();

		try {
			OperationResult<ColumnList<String>> nowRemindersEventsResult = keyspace
					.prepareQuery(usersRemindersCF).getKey(userId).execute();

			ColumnList<String> result = nowRemindersEventsResult.getResult();
			for (Column<String> column : result) {
				reminders.add(mapper.readValue(column.getStringValue(),
						Reminder.class));
			}

		} catch (JsonGenerationException | JsonMappingException e) {
			logger.error("JsonException ", e);
		} catch (IOException e) {
			logger.error("IOException ", e);
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}
		return reminders;
	}

	@Override
	public void addReminderEvents(String userId,
			List<ReminderEvent> newRemindersEvent) {

		try {
			MutationBatch batch = keyspace.prepareMutationBatch();

			for (ReminderEvent reminderEvent : newRemindersEvent) {

				batch.withRow(userRemindersEventsCF, makeKey(reminderEvent))
						.putColumn(
								new ReminderEventId(userId,
										TimeUUIDUtils.getTimeUUID(reminderEvent
												.getTime())),
								mapper.writeValueAsString(reminderEvent));
			}

			batch.execute();
		} catch (IOException e) {
			logger.error("IOException ", e);
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}
	}

	@Override
	public void deleteReminderEvents(List<ReminderEventId> reminderEventIds) {
		MutationBatch batch = keyspace.prepareMutationBatch();
		
		try {
			for (ReminderEventId reminderEventId : reminderEventIds) {
				batch.withRow(userRemindersEventsCF, makeKey(reminderEventId))
						.deleteColumn(reminderEventId);
			}

			batch.execute();
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}
	}

	@Override
	public List<ReminderEventId> getAllUserReminderEventsIds(String userId,
			List<ReminderEvent> oldRemindersEvent) {
		
		List<ReminderEventId> reminderEventIds = new ArrayList<>();
		
		List<String> keys = new ArrayList<>();
		
		for (ReminderEvent reminderEvent : oldRemindersEvent) {
			keys.add(makeKey(reminderEvent));
		}
		
		ColumnFamilyQuery<String, ReminderEventId> query = keyspace.prepareQuery(userRemindersEventsCF);
		try {
			OperationResult<Rows<String, ReminderEventId>> result = query.getKeySlice(keys)
				 .withColumnRange(
						 eventSerializer.buildRange()
						 	.greaterThanEquals(userId)
						 	.lessThanEquals(userId).build()
						 	).execute();
			Rows<String,ReminderEventId> rows = result.getResult();
			
			for (Row<String, ReminderEventId> row : rows) {
				reminderEventIds.addAll(row.getColumns().getColumnNames());
			}
			
		} catch (ConnectionException e) {
			logger.error("ConnectionException ", e);
		}
		return reminderEventIds;
	}

	@Override
	public void updateNotificationId(String userId, String notificationId) {
		MutationBatch batch = keyspace.prepareMutationBatch();
		batch.withRow(userNotificationIdsCF, userId).putColumn(userId, notificationId);
		try {
			batch.execute();
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public Map<String, String> getUsersNotificationIds(List<String> users) {
		
		Map<String, String> userNotificationIds = new HashMap<String, String>();
		ColumnFamilyQuery<String, String> query = keyspace.prepareQuery(userNotificationIdsCF);
		OperationResult<Rows<String, String>> result;
		try {
			result = query.getRowSlice(users).execute();

			for (Row<String, String> row : result.getResult()) {
				userNotificationIds.put(row.getKey(), row.getColumns().getColumnByName(row.getKey()).getStringValue());
			}
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		
		return userNotificationIds;
	}

	@Override
	public Map<String, ShbtonGeoLocation> getUsersGeoLocations(
			List<String> userIds)  {
		
		
		Map<String, ShbtonGeoLocation> usersGeoLocations = new HashMap<String, ShbtonGeoLocation>();
		
		ColumnFamilyQuery<String, Long> query = keyspace.prepareQuery(userGeoLocationCF);
		OperationResult<Rows<String, Long>> result;
		try {
			result = query.getRowSlice(userIds).withColumnRange(Long.MAX_VALUE, 0l, true, 1).execute();

			for (Row<String, Long> row : result.getResult()) {
				usersGeoLocations.put(row.getKey(), mapper.readValue(row.getColumns().getColumnByIndex(0).getStringValue(),ShbtonGeoLocation.class));
			}
		} catch (ConnectionException | IOException e) {
			e.printStackTrace();
		}
		
		return usersGeoLocations;
	}

	@Override
	public Reminder getUserReminder(String userId, String id) {
		ColumnFamilyQuery<String, String> query = keyspace.prepareQuery(usersRemindersCF);
		try {
			OperationResult<Column<String>> result = query.getKey(userId).getColumn(id).execute();
			return mapper.readValue(result.getResult().getStringValue(),Reminder.class);
		} catch (ConnectionException |  IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
