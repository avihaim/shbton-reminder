package shbton.reminder.server.time;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.joda.time.DateTime;

public class ShbtonZmanimCalendar implements ZmanimManger {

	@Override
	public List<DateTime> getThisWeekCandleLighting(ShbtonGeoLocation shbtongeoLocation) {

		GeoLocation location = new GeoLocation(shbtongeoLocation.getLocationName(), shbtongeoLocation.getLatitude(),
				shbtongeoLocation.getLongitude(), shbtongeoLocation.getElevation(), shbtongeoLocation.getTimeZone());
		ComplexZmanimCalendar czc = new ComplexZmanimCalendar(location);

		czc.getCalendar().set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		DateTime candleLighting = new DateTime(czc.getCandleLighting());
		return Collections.singletonList(candleLighting);
	}
}
