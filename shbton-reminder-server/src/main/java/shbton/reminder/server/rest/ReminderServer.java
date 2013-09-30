package shbton.reminder.server.rest;

import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import shbton.reminder.server.database.ReminderCassandraManger;
import shbton.reminder.server.database.ReminderDataBaseManger;
import shbton.reminder.server.manger.AndroidNotificationManger;
import shbton.reminder.server.manger.NotificationManger;
import shbton.reminder.server.manger.ReminderManger;
import shbton.reminder.server.manger.ReminderMangerImpl;
import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.time.ShbtonGeoLocation;
import shbton.reminder.server.time.ShbtonZmanimCalendar;
import shbton.reminder.server.time.ZmanimManger;

@Path("/users")
public class ReminderServer {

	private static ReminderManger reminderManger;
	private static ZmanimManger zmanimManger;
	
	public static void init() {
		ReminderDataBaseManger dataBaseManger = new ReminderCassandraManger();
		zmanimManger = new ShbtonZmanimCalendar();
		NotificationManger notificationManger = new AndroidNotificationManger(dataBaseManger);
		reminderManger = new ReminderMangerImpl(dataBaseManger,zmanimManger,notificationManger);
	}
	
	public static void stop() {
		((ReminderMangerImpl)reminderManger).stop();
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{userId}/reminders")
	public Response putNewReminder(@PathParam("userId") String userId,
			Reminder reminder) {
		
		reminderManger.addReminder(userId, reminder);
		return Response.ok().build();

	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{userId}/geolocations")
	public Response addUserGeoLocation(@PathParam("userId") String userId,
			ShbtonGeoLocation shbtongeoLocation) {
		
		reminderManger.addUserGeoLocation(userId, shbtongeoLocation);
		return Response.ok().build();

	}
	
	@POST
	@Path("/{userId}/notifications")
	public Response postNotificationId(@PathParam("userId") String userId,String notificationId) {
		System.out.println("notificationId " + notificationId);
		System.out.println("userId " + userId);
		
		reminderManger.updateNotificationId(userId,notificationId);
		
		return Response.ok().build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	@GET
	@Path("candlelighting")
	public Response getCandleLighting(@QueryParam("locationName") String locationName,
									 @QueryParam("latitude") double latitude,
									 @QueryParam("longitude") double longitude,
									 @QueryParam("elevation") @DefaultValue(value="0") double elevation) {
		
		zmanimManger.getThisWeekCandleLighting(new ShbtonGeoLocation(locationName,latitude,longitude,elevation,null));
		return Response.ok().build();
	}

}
