package shbton.reminder.server.database;

import java.util.List;

import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.obj.ReminderEvent;
import shbton.reminder.server.time.ShbtonGeoLocation;

public interface ReminderDataBaseManger {
	void addReminder(String userId, Reminder reminder, List<ReminderEvent> remindersEvent);

	ShbtonGeoLocation getUserGeoLocation(String userId);

	void addUserGeoLocation(String userId,ShbtonGeoLocation shbtongeoLocation);
	
	List<ReminderEvent> getNowReminderEvents();

	void updateLastPushRun();

	List<Reminder> getUserReminders(String userId);

	void deleteReminderEvents(List<ReminderEventId> reminderEventIds);

	void addReminderEvents(String userId, List<ReminderEvent> newRemindersEvent);

	List<ReminderEventId> getAllUserReminderEventsIds(String userId,
			List<ReminderEvent> oldRemindersEvent);

}
