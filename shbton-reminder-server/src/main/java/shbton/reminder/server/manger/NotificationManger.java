package shbton.reminder.server.manger;

import java.util.List;

import shbton.reminder.server.obj.ReminderEvent;

public interface NotificationManger {

	void pushReminderEventsNotifications(List<ReminderEvent> nowReminderEvents);

	void updateNotificationId(String userId, String notificationId);

}
