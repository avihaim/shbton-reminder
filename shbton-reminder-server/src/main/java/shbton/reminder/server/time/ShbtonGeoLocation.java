package shbton.reminder.server.time;

import java.util.TimeZone;

public class ShbtonGeoLocation {

	private String locationName;
	private double latitude; // Lakewood, NJ
	private double longitude; // Lakewood, NJ
	private double elevation;
	private TimeZone timeZone;
	
	public ShbtonGeoLocation() {
	}
	
	public ShbtonGeoLocation(String locationName, double latitude,
			double longitude, double elevation, TimeZone timeZone) {
		super();
		this.locationName = locationName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
		this.timeZone = timeZone;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
}
