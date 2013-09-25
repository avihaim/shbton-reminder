package shbton.reminder.server.obj;


public class Reminder {
	
	private String id;
	private String text;
	private Boolean isShbat;
	private Boolean isHoliday;
	private Boolean isBefore;
	private int days;
	private int hours;
	private int minutes;
	
	
	public Reminder() {
	}

	
	public Reminder(String id, String text, Boolean isShbat,
			Boolean isHoliday, Boolean isBefore, int days, int hours,
			int minutes) {
		super();
		this.id = id;
		this.text = text;
		this.isShbat = isShbat;
		this.isHoliday = isHoliday;
		this.isBefore = isBefore;
		this.days = days;
		this.hours = hours;
		this.minutes = minutes;
	}


	public Reminder(String id) {
		super();
		this.id = id;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getIsShbat() {
		return isShbat;
	}


	public void setIsShbat(Boolean isShbat) {
		this.isShbat = isShbat;
	}


	public Boolean getIsHoliday() {
		return isHoliday;
	}


	public void setIsHoliday(Boolean isHoliday) {
		this.isHoliday = isHoliday;
	}


	public Boolean getIsBefore() {
		return isBefore;
	}


	public void setIsBefore(Boolean isBefore) {
		this.isBefore = isBefore;
	}


	public int getDays() {
		return days;
	}


	public void setDays(int days) {
		this.days = days;
	}


	public int getHours() {
		return hours;
	}


	public void setHours(int hours) {
		this.hours = hours;
	}


	public int getMinutes() {
		return minutes;
	}


	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}
	
	

}
