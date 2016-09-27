package com.jobaline.uiautomation.framework.lang;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateRange
{
	private long startDateTime;
	private long endDateTime;
	
	public DateRange(Date start, Date end)
	{
		if(end.getTime() < start.getTime())
			throw new RuntimeException("end Date must be greater than start Date");
			
		this.startDateTime= start.getTime();
		this.endDateTime= end.getTime();
	}
	
	
	public DateRange(long startDateTime, long endDateTime)
	{
		if(endDateTime < startDateTime)
			throw new RuntimeException("end Date must be greater than start Date");
		
		this.startDateTime= startDateTime;
		this.endDateTime= endDateTime;
	}
	
	
	public String toString()
	{
		long seconds= (long) ((endDateTime - startDateTime) / 1000);
		
		int days = (int)TimeUnit.SECONDS.toDays(seconds);
		seconds-= days * 24 * 60 * 60;
		
		long hours = TimeUnit.SECONDS.toHours(seconds);
		seconds-= hours * 60 * 60;
		
		long minutes= TimeUnit.SECONDS.toMinutes(seconds);
		
		seconds-= minutes * 60;
		
		String value= "";
		
		if(days > 0)
			value= days + " days ";
		
		if(hours < 10)
			value= value + "0";
		
		value= value + hours + ":";
		
		if(minutes < 10)
			value= value + "0";
		value= value + minutes + ":";
		
		if(seconds < 10)
			value= value + "0";
		value= value + seconds;
		
		return value;
	}
	
	 
}
