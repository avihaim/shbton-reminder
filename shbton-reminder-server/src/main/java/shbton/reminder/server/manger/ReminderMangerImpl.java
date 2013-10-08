package shbton.reminder.server.manger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shbton.reminder.server.database.ReminderDataBaseManger;
import shbton.reminder.server.database.ReminderEventId;
import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.obj.ReminderEvent;
import shbton.reminder.server.time.ShbtonGeoLocation;
import shbton.reminder.server.time.ZmanimManger;

public class ReminderMangerImpl implements ReminderManger, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ReminderMangerImpl.class);
	private boolean running = true; 
	private ReminderDataBaseManger dataBaseManger; 
	private ZmanimManger zmanimManger;
	private NotificationManger notificationManger;
	private long MINUTE= 1000*60;

	public ReminderMangerImpl(ReminderDataBaseManger dataBaseManger, ZmanimManger zmanimManger,NotificationManger notificationManger) {
		super();
		this.dataBaseManger = dataBaseManger;
		this.zmanimManger = zmanimManger;
		this.notificationManger = notificationManger;
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
	//	executor.submit(this);
	}
	
	public void addUserGeoLocation(String userId,ShbtonGeoLocation shbtongeoLocation) {
		
		ShbtonGeoLocation userOldGeoLocation = dataBaseManger.getUserGeoLocation(userId);
		dataBaseManger.addUserGeoLocation(userId,shbtongeoLocation);
		
		if(userOldGeoLocation != null) {
			
			List<DateTime> oldCandleLightingTime = zmanimManger.getThisWeekCandleLighting(userOldGeoLocation);
			List<DateTime> newCandleLightingTime = zmanimManger.getThisWeekCandleLighting(shbtongeoLocation);
			
			List<Reminder> reminders =  dataBaseManger.getUserReminders(userId);
			
			List<ReminderEvent> oldRemindersEvent = new ArrayList<>();
			
			for (Reminder reminder : reminders) {
				oldRemindersEvent.addAll(calcThisWeekRemindersEvent(userId,reminder,oldCandleLightingTime));
			}
			
			List<ReminderEventId> reminderEventIds = dataBaseManger.getAllUserReminderEventsIds(userId,oldRemindersEvent);
			
			dataBaseManger.deleteReminderEvents(reminderEventIds);
			
			List<ReminderEvent> newRemindersEvent = new ArrayList<>();
			
			for (Reminder reminder : reminders) {
				newRemindersEvent.addAll(calcThisWeekRemindersEvent(userId,reminder,newCandleLightingTime));
			}
			
			logger.debug("addUserGeoLocation for userId : {} newRemindersEvent : {} ",userId,newRemindersEvent);
			
			dataBaseManger.addReminderEvents(userId,newRemindersEvent);
		}
		
		
	}

	public void addReminder(String userId, Reminder reminder) {
		
		ShbtonGeoLocation shbtongeoLocation = dataBaseManger.getUserGeoLocation(userId);
		List<DateTime> candleLighting = zmanimManger.getThisWeekCandleLighting(shbtongeoLocation);
		
		List<ReminderEvent> remindersEvent = calcThisWeekRemindersEvent(userId,reminder,candleLighting);
		
		if("1".equals(reminder.getId())) {
			reminder.setId( UUID.randomUUID().toString());
		}
		
		logger.debug("addUserGeoLocation for userId : {} remindersEvent : {} ",userId,remindersEvent);
		
		dataBaseManger.addReminder(userId, reminder,remindersEvent);
	}

	public static List<ReminderEvent> calcThisWeekRemindersEvent(String userId,Reminder reminder,
			List<DateTime> candleLighting) {
		
		List<ReminderEvent> reminderEvents = new ArrayList<>();
		
		for (DateTime dateTime : candleLighting) {
			if(reminder.getIsBefore()) {
				long reminderEventTime = dateTime.minusDays(reminder.getDays()).minusHours(reminder.getHours()).minusMinutes(reminder.getMinutes()).getMillis();
				reminderEvents.add( new ReminderEvent(userId, reminderEventTime, reminder.getText()));
			}
		}
		
		return reminderEvents;
		
	}

	public ReminderDataBaseManger getDataBaseManger() {
		return dataBaseManger;
	}

	public void setDataBaseManger(ReminderDataBaseManger dataBaseManger) {
		this.dataBaseManger = dataBaseManger;
	}

	public ZmanimManger getZmanimManger() {
		return zmanimManger;
	}

	public void setZmanimManger(ZmanimManger zmanimManger) {
		this.zmanimManger = zmanimManger;
	}

	@Override
	public void run() {
		startPushEvents();
	}

	private void startPushEvents() {
		logger.debug("startPushEvents");
		
		long startTime = System.currentTimeMillis();
		
		while (running) {
			List<ReminderEvent> nowReminderEvents = dataBaseManger.getNowReminderEvents();
			notificationManger.pushReminderEventsNotifications(nowReminderEvents);
			//dataBaseManger.updateLastPushRun();
			
			long endTime = System.currentTimeMillis();
			
			long left = MINUTE - (endTime - 	startTime);
			
			if (left > 0) {
				System.out.println("sleep");
				logger.debug("startPushEvents sleep");
				sleep(left);
				System.out.println("wake up");
				logger.debug("startPushEvents wake up");
			}
			
			
			startTime = System.currentTimeMillis();
		}
	}

	private void sleep(long left) {
		
		try {
			Thread.sleep(left);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void updateNotificationId(String userId, String notificationId) {
		notificationManger.updateNotificationId(userId,notificationId);
		
	}
	
	public void stop() {
		running = false;
		//object.notify();
	}

	@Override
	public List<Reminder> getUserReminders(String userId) {
		logger.debug("getUserReminders - {}" ,userId);
		List<Reminder> userReminders = dataBaseManger.getUserReminders(userId);
		
		logger.debug("getUserReminders - {}",userReminders);
		
		return userReminders;
	}

	@Override
	public ShbtonGeoLocation getUserGeoLocation(String userId) {
		return dataBaseManger.getUserGeoLocation(userId);
	}

}
