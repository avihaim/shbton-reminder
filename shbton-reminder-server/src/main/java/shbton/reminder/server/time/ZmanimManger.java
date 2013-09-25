package shbton.reminder.server.time;

import java.util.List;

import org.joda.time.DateTime;

public interface ZmanimManger {
	List<DateTime> getThisWeekCandleLighting(ShbtonGeoLocation shbtongeoLocation);
}
