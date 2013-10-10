package shbton.reminder.server.time;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.joda.time.DateTime;

public class ShbtonZmanimCalendar implements ZmanimManger {

	@Override
	public List<DateTime> getThisWeekCandleLighting(ShbtonGeoLocation shbtongeoLocation) {

		DateTime candleLighting = getTime(shbtongeoLocation,false);
		
		return Collections.singletonList(candleLighting);
	}

	private DateTime getTime(ShbtonGeoLocation shbtongeoLocation,boolean addWeek) {
		GeoLocation location = new GeoLocation(shbtongeoLocation.getLocationName(), shbtongeoLocation.getLatitude(),
				shbtongeoLocation.getLongitude(), shbtongeoLocation.getElevation(), shbtongeoLocation.getTimeZone());
		ComplexZmanimCalendar czc = new ComplexZmanimCalendar(location);

		if(addWeek) {
			czc.getCalendar().add(Calendar.DATE, 7);
		} 
		czc.getCalendar().set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		
		DateTime candleLighting = new DateTime(czc.getCandleLighting());
		return candleLighting;
	}

	@Override
	public Map<String, List<DateTime>> getNextWeekCandleLighting(
			Map<String, ShbtonGeoLocation> usersGeoLocation) {
		
		Map<String, List<DateTime>> map = new HashMap<String, List<DateTime>>();
		Set<Entry<String, ShbtonGeoLocation>> entrySet = usersGeoLocation.entrySet();
		for (Entry<String, ShbtonGeoLocation> entry : entrySet) {
			map.put(entry.getKey(), getThisWeekCandleLighting(entry.getValue()));
		}
		
		return map;
	}
}
