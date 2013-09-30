package shbton.reminder.server;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;

import shbton.reminder.server.database.ReminderCassandraManger;
import shbton.reminder.server.database.ReminderEventId;
import shbton.reminder.server.rest.ReminderServer;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * Main class.
 * 
 */
public class Main {
	
	// Base URI the Grizzly HTTP server will listen on
	public static final String BASE_URI = "http://192.168.1.100:8080/shbton/";

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in shbton.reminder.server.rest package

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI),
					createApp());
	
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ConnectionException 
	 * @throws TTransportException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationException, TTransportException, ConnectionException {
		
		HttpServer server = null;
		
		try {
		    initCassandra();
		
			server = startServer();
			
			ReminderServer.init();
			
			System.out.println(String.format(
					"Jersey app started with WADL available at "
							+ "%sapplication.wadl\nHit enter to stop it...",
					BASE_URI));
			System.in.read();
		} catch (Exception e) {
			
		} finally {
			ReminderServer.stop();
			
			if(server != null) {
				server.stop();
			}
			EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
		}
		
	}

	public static ResourceConfig createApp() {
		return new ResourceConfig().packages("shbton.reminder.server.rest")
				.registerInstances(new JsonMoxyConfigurationContextResolver());
	}

	@Provider
	public final static class JsonMoxyConfigurationContextResolver implements
			ContextResolver<MoxyJsonConfig> {

		public MoxyJsonConfig getContext(Class<?> type) {
			final MoxyJsonConfig configuration = new MoxyJsonConfig();

			Map<String, String> namespacePrefixMapper = new HashMap<String, String>(
					1);
			namespacePrefixMapper.put(
					"http://www.w3.org/2001/XMLSchema-instance", "xsi");

			configuration.setNamespacePrefixMapper(namespacePrefixMapper);
			configuration.setNamespaceSeparator(':');

			return configuration;
		}
	}

	private static void initCassandra() throws TTransportException,
			IOException, ConfigurationException, ConnectionException {
		
		ColumnFamily<String, String> columnFamily;
		ColumnFamily<String, Long> userGeoLocationCF;
		ColumnFamily<String, ReminderEventId> userRemindersEventsCF;

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
		Keyspace keyspace = ctx.getClient();

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
				ReminderCassandraManger.USER_GEO_LOCATION_CF,
				StringSerializer.get(), LongSerializer.get());

		keyspace.createColumnFamily(
				userGeoLocationCF,
				ImmutableMap.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type", "LongType").build());

		userRemindersEventsCF = ColumnFamily
				.newColumnFamily(
						ReminderCassandraManger.USER_REMINDERS_EVENTS_CF,
						StringSerializer.get(),
						ReminderCassandraManger.eventSerializer);

		keyspace.createColumnFamily(
				userRemindersEventsCF,
				ImmutableMap
						.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type",
								"CompositeType(UTF8Type, TimeUUIDType)")
						.build());
		
		ColumnFamily<String, String> userNotificationIdsCF = ColumnFamily.newColumnFamily(
				ReminderCassandraManger.USER_NOTIFICATION_IDS_CF, StringSerializer.get(),
				StringSerializer.get());

		keyspace.createColumnFamily(
				userNotificationIdsCF,
				ImmutableMap.<String, Object> builder()
						.put("default_validation_class", "UTF8Type")
						.put("key_validation_class", "UTF8Type")
						.put("comparator_type", "UTF8Type").build());

	}

}
