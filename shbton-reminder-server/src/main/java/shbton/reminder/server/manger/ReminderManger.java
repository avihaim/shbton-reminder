package shbton.reminder.server.manger;

import java.util.List;

import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.time.ShbtonGeoLocation;

public interface ReminderManger {

	void addReminder(String userId, Reminder reminder);
	
	void addUserGeoLocation(String userId,ShbtonGeoLocation shbtongeoLocation);

	void updateNotificationId(String userId, String notificationId);

	List<Reminder> getUserReminders(String userId);

	ShbtonGeoLocation getUserGeoLocation(String userId);
}
