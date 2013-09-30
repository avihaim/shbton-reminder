package shbton.reminder.server.manger;

import shbton.reminder.server.obj.Reminder;
import shbton.reminder.server.time.ShbtonGeoLocation;

public interface ReminderManger {

	void addReminder(String userId, Reminder reminder);
	
	 void addUserGeoLocation(String userId,ShbtonGeoLocation shbtongeoLocation);

	void updateNotificationId(String userId, String notificationId);
}
