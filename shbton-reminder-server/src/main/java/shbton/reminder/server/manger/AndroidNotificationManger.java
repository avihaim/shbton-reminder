package shbton.reminder.server.manger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shbton.reminder.server.database.ReminderDataBaseManger;
import shbton.reminder.server.obj.ReminderEvent;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

public class AndroidNotificationManger implements NotificationManger {

	private static final Logger logger = LoggerFactory.getLogger(AndroidNotificationManger.class);
	// The SENDER_ID here is the "Browser Key" that was generated when I
    // created the API keys for my Google APIs project.
    private static final String SENDER_ID = "AIzaSyDn4E4ejd_Jf1CLX-0ek4v4uoj_ZaqDIYk";
    private static ObjectMapper mapper = new ObjectMapper();
    private ReminderDataBaseManger dataBaseManger; 
    
    
	public AndroidNotificationManger(ReminderDataBaseManger dataBaseManger) {
		this.dataBaseManger = dataBaseManger;
	}

	@Override
	public void pushReminderEventsNotifications(
			List<ReminderEvent> nowReminderEvents) {
		
		List<String> users = new ArrayList<>();
		
		Map<String,String> usersNotificationIds = dataBaseManger.getUsersNotificationIds(users);
		
		Map<String,List<ReminderEvent>> reminderEventsMap = new HashMap<>();
		
		for (ReminderEvent reminderEvent : nowReminderEvents) {
			List<ReminderEvent> list = reminderEventsMap.get(usersNotificationIds.get(reminderEvent.getUserId()));
			list.add(reminderEvent);
			reminderEventsMap.put(usersNotificationIds.get(reminderEvent.getUserId()), list);
		}
		
		Set<Entry<String,List<ReminderEvent>>> entrySet = reminderEventsMap.entrySet();
		for (Entry<String, List<ReminderEvent>> entry : entrySet) {
			try {
				sendNotification(entry.getKey(),entry.getValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void sendNotification(String notificationId,List<ReminderEvent> reminderEvents) throws JsonGenerationException, JsonMappingException, IOException {
		List<String> androidTargets = new ArrayList<String>();
		
		androidTargets.add(notificationId);
		// Instance of com.android.gcm.server.Sender, that does the
        // transmission of a Message to the Google Cloud Messaging service.
        Sender sender = new Sender(SENDER_ID);
        
        for (ReminderEvent reminderEvent : reminderEvents) {

            // This Message object will hold the data that is being transmitted
            // to the Android client devices.  For this demo, it is a simple text
            // string, but could certainly be a JSON object.
            Message message = new Message.Builder()

            // If multiple messages are sent using the same .collapseKey()
            // the android target device, if it was offline during earlier message
            // transmissions, will only receive the latest message for that key when
            // it goes back on-line.
            .collapseKey("GCM_Message")
            .timeToLive(30)
            .delayWhileIdle(true)
            .addData("message", mapper.writeValueAsString(reminderEvent))
            .build();
             
            try {
                // use this for multicast messages.  The second parameter
                // of sender.send() will need to be an array of register ids.
                MulticastResult result = sender.send(message, androidTargets, 1);
                 
                if (result.getResults() != null) {
                    int canonicalRegId = result.getCanonicalIds();
                    if (canonicalRegId != 0) {
                         
                    }
                } else {
                    int error = result.getFailure();
                    System.out.println("Broadcast failure: " + error);
                }
                 
            } catch (Exception e) {
                e.printStackTrace();
            }
		} 

 
	}

	@Override
	public void updateNotificationId(String userId, String notificationId) {
		dataBaseManger.updateNotificationId(userId,notificationId);
		
	}
}
