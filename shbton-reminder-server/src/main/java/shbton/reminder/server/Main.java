package shbton.reminder.server;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 * 
 */
public class Main {
	// Base URI the Grizzly HTTP server will listen on
	public static final String BASE_URI = "http://localhost:8080/shbton/";

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
	 */
	public static void main(String[] args) throws IOException {
		final HttpServer server = startServer();
		System.out.println(String.format(
				"Jersey app started with WADL available at "
						+ "%sapplication.wadl\nHit enter to stop it...",
				BASE_URI));
		System.in.read();
		server.stop();
	}
	
	public static ResourceConfig createApp() {
        return new ResourceConfig().
                packages("shbton.reminder.server.rest").
                registerInstances(new JsonMoxyConfigurationContextResolver());
    }
	
	@Provider
	public final static class JsonMoxyConfigurationContextResolver implements ContextResolver<MoxyJsonConfig> {

		public MoxyJsonConfig getContext(Class<?> type) {
			final MoxyJsonConfig configuration = new MoxyJsonConfig();

            Map<String, String> namespacePrefixMapper = new HashMap<String, String>(1);
            namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

            configuration.setNamespacePrefixMapper(namespacePrefixMapper);
            configuration.setNamespaceSeparator(':');

            return configuration;
		}
	}
}
