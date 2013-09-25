package shbton.reminder.server.manger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shbton.reminder.server.obj.ReminderEvent;

public class AndroidNotificationManger implements NotificationManger {

	private static final Logger logger = LoggerFactory.getLogger(AndroidNotificationManger.class);
	
	@Override
	public void pushReminderEventsNotifications(
			List<ReminderEvent> nowReminderEvents) {
		
		for (ReminderEvent reminderEvent : nowReminderEvents) {
			logger.debug("start to push reminderEvent {} ",reminderEvent);
		}
	}
}
