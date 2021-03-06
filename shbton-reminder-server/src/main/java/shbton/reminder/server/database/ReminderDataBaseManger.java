package shbton.reminder.server.database;

import java.util.List;
import java.util.Map;

import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.obj.ReminderEvent;
import shbton.reminder.server.time.ShbtonGeoLocation;

public interface ReminderDataBaseManger {
	
	void addReminder(String userId, Reminder reminder, List<ReminderEvent> remindersEvent);
	
	List<Reminder> getUserReminders(String userId);
	
	ShbtonGeoLocation getUserGeoLocation(String userId);

	void addUserGeoLocation(String userId,ShbtonGeoLocation shbtongeoLocation);
	
	Map<String, ShbtonGeoLocation> getUsersGeoLocations(List<String> userIds);
	
	List<ReminderEvent> getNowReminderEvents();

	void deleteReminderEvents(List<ReminderEventId> reminderEventIds);

	void addReminderEvents(String userId, List<ReminderEvent> newRemindersEvent);

	List<ReminderEventId> getAllUserReminderEventsIds(String userId,
			List<ReminderEvent> oldRemindersEvent);

	
	void updateNotificationId(String userId, String notificationId);

	Map<String, String> getUsersNotificationIds(List<String> users);

	Reminder getUserReminder(String userId, String id);

	

}
