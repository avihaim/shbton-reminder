package shbton.reminder.server.rest;

import java.io.IOException;
import java.util.TimeZone;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import shbton.reminder.server.time.ShbtonGeoLocation;

public class Tests {
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void test() throws JsonGenerationException, JsonMappingException, IOException {
		ShbtonGeoLocation shbtonGeoLocation = new ShbtonGeoLocation("Lakewood, NJ", 40.096, -74.222, 0, TimeZone.getTimeZone("America/New_York"));
		String shbtonGeoLocationString = mapper.writeValueAsString(shbtonGeoLocation);
		System.out.println(shbtonGeoLocationString);
		ShbtonGeoLocation value = mapper.readValue(shbtonGeoLocationString, ShbtonGeoLocation.class);
		Assert.assertEquals(shbtonGeoLocation.toString(), value.toString());
	}
	
}
