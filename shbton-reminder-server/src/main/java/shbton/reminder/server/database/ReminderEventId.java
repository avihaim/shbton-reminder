package shbton.reminder.server.database;

import java.util.UUID;

import com.netflix.astyanax.annotations.Component;

public class ReminderEventId {
	private @Component(ordinal = 0) String userId;
	private @Component(ordinal = 1) UUID timestamp;

	public ReminderEventId() {
	}
	
	

	public ReminderEventId(String userId, UUID timestamp) {
		super();
		this.userId = userId;
		this.timestamp = timestamp;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public UUID getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(UUID timestamp) {
		this.timestamp = timestamp;
	}


//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((timestamp == null) ? 0 : timestamp.hashCode());
//		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
//		return result;
//	}
//
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ReminderEventId other = (ReminderEventId) obj;
//		if (timestamp == null) {
//			if (other.timestamp != null)
//				return false;
//		} else if (!timestamp.equals(other.timestamp))
//			return false;
//		if (userId == null) {
//			if (other.userId != null)
//				return false;
//		} else if (!userId.equals(other.userId))
//			return false;
//		return true;
//	}
//	
//	 public int compareTo(ReminderEventId eventId) {
//		 int userCompareTo = userId.compareTo(eventId.getUserId());
//		 if(userCompareTo == 0) {
//			 return timestamp.compareTo(eventId.getTimestamp());
//		 } else {
//			 return userCompareTo;
//		 }
//	 }
//	
}
