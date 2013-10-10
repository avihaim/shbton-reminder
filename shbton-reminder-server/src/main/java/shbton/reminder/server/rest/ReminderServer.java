package shbton.reminder.server.rest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shbton.reminder.server.database.ReminderCassandraManger;
import shbton.reminder.server.database.ReminderDataBaseManger;
import shbton.reminder.server.manger.AndroidNotificationManger;
import shbton.reminder.server.manger.NotificationManger;
import shbton.reminder.server.manger.ReminderManger;
import shbton.reminder.server.manger.ReminderMangerImpl;
import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.time.CandleLightingTime;
import shbton.reminder.server.time.ShbtonGeoLocation;
import shbton.reminder.server.time.ShbtonZmanimCalendar;
import shbton.reminder.server.time.ZmanimManger;

@Path("/users")
public class ReminderServer {
	private static final Logger logger = LoggerFactory.getLogger(ReminderServer.class);
	private static ObjectMapper mapper = new ObjectMapper();
	
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
	
	@POST
	@Path("/{userId}/reminders")
	public Response putNewReminder(@PathParam("userId") String userId,
			String reminderString) {
		
		try {
			logger.debug("Start in putNewReminder for user {}",userId);
			Reminder reminder = mapper.readValue(reminderString, Reminder.class);
			
			reminderManger.addReminder(userId, reminder);
			
			logger.debug("End in putNewReminder for user {}",userId);
		} catch (IOException e) {
			logger.error("error in putNewReminder for user {}",userId,e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return Response.ok().build();

	}
	
	@GET
	@Path("/{userId}/reminders")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Reminder> getReminders(@PathParam("userId") String userId) {
		logger.debug(" getReminders - userId : {} ",userId);
		return reminderManger.getUserReminders(userId);
	}
	
	
	@POST
	@Path("/{userId}/geolocations")
	public Response addUserGeoLocation(@PathParam("userId") String userId,
			String shbtongeoLocationString) {
		
		ShbtonGeoLocation shbtonGeoLocation;
		
		try {
			shbtonGeoLocation = mapper.readValue(shbtongeoLocationString, ShbtonGeoLocation.class);
			logger.debug(" addUserGeoLocation - userId : {} data : {}",userId,shbtonGeoLocation.toString());
			reminderManger.addUserGeoLocation(userId, shbtonGeoLocation);
			logger.debug(" addUserGeoLocation - userId : {} ",userId);
		} catch (IOException e) {
			logger.error("error in addUserGeoLocation for user {}",userId,e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		
		
		return Response.ok().build();

	}
	
	@POST
	@Path("/{userId}/notifications")
	public Response postNotificationId(@PathParam("userId") String userId,String notificationId) {
		logger.debug("Start in postNotificationId for user {} and notificationId",userId);
		
		reminderManger.updateNotificationId(userId,notificationId);
		
		return Response.ok().build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	@GET
	@Path("/{userId}/candlelighting")
	public Response getCandleLighting(@PathParam("userId") String userId) {
		logger.debug("getCandleLighting for userId : {} ",userId);
		ShbtonGeoLocation shbtonGeoLocation = reminderManger.getUserGeoLocation(userId);
		List<DateTime> candleLighting = zmanimManger.getThisWeekCandleLighting(shbtonGeoLocation);
		logger.debug("getCandleLighting for userId : {} are : {}",userId,candleLighting);
		
		return Response.ok(new CandleLightingTime(candleLighting.get(0).getMillis())).build();
	}
}
