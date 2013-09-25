package shbton.reminder.server.obj;

public class ReminderEvent {
	
	long time;
	String text;
	String userId;
	String userNotfictionId;
	
	public ReminderEvent() {
	}
	
	public ReminderEvent(String userId, String userNotfictionId, long time,
			String text) {
		super();
		this.userId = userId;
		this.userNotfictionId = userNotfictionId;
		this.time = time;
		this.text = text;
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserNotfictionId() {
		return userNotfictionId;
	}
	public void setUserNotfictionId(String userNotfictionId) {
		this.userNotfictionId = userNotfictionId;
	}

	@Override
	public String toString() {
		return "ReminderEvent [time=" + time + ", text=" + text + ", userId="
				+ userId + ", userNotfictionId=" + userNotfictionId + "]";
	}

}
