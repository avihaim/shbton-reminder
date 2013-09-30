package shbton.reminder.server.obj;

public class ReminderEvent {
	
	long time;
	String text;
	String userId;
	
	public ReminderEvent() {
	}
	
	public ReminderEvent(String userId, long time,
			String text) {
		super();
		this.userId = userId;
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

	@Override
	public String toString() {
		return "ReminderEvent [time=" + time + ", text=" + text + ", userId="
				+ userId +  "]";
	}

}
