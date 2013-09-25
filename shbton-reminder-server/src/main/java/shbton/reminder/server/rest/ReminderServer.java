package shbton.reminder.server.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
	
	static {
		ReminderDataBaseManger dataBaseManger = new ReminderCassandraManger();
		ZmanimManger zmanimManger = new ShbtonZmanimCalendar();
		NotificationManger notificationManger = new AndroidNotificationManger();
		reminderManger = new ReminderMangerImpl(dataBaseManger,zmanimManger,notificationManger);
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
	
}
