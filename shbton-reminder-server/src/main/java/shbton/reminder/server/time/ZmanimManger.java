package shbton.reminder.server.time;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

public interface ZmanimManger {
	List<DateTime> getThisWeekCandleLighting(ShbtonGeoLocation shbtongeoLocation);

	Map<String, List<DateTime>> getNextWeekCandleLighting(
			Map<String, ShbtonGeoLocation> usersGeoLocation);
}
