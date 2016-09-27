package com.jobaline.uiautomation.framework.generators;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by damian on 4/22/15.
 */
public class IdGenerator
{

	/**
	 * Generate a random alphanumeric id.
	 * */
	public static String generateTiny()
	{
		char[] chars = new char[4];

		// The first one is going to be a letter
		chars[0] = (char)(new Random().nextInt(26) + (byte)'a');

		for(int i = 1; i < chars.length; i++)
		{
			if(Math.random() < .5)
			{
				chars[i] = (char)(new Random().nextInt(10) + (byte)'0');
			}
			else
			{
				chars[i] = (char)(new Random().nextInt(26) + (byte)'a');
			}
		}

		return new String(chars);
	}


	/**
	 * Generate a pseudo-unique alphabetic identifier.
	 * */
	public static String generateShort()
	{
		String id = generate();

		while(id.length() > 10)
		{
			int pos = (int)(Math.random() * id.length());

			id = id.substring(0, pos) + id.substring(pos + 1);
		}

		return addTime(id);
	}


	/**
	 * Generate a pseudo-unique alphabetic identifier.
	 *
	 * It is a pseudo UUID, the uniqueness is not guaranteed, although, the the probability of getting repeated values is very low. If the client is going to generate id
	 * for 100 entities that will exist in a context, the probability of getting repeated values can be considered 0.
	 * */
	public static String generate()
	{
		String id = UUID.randomUUID().toString().replaceAll("-", "");

		return addTime(id);
	}


	/** Add a timestamp to reduce the probability of duplicates. Will use the number of milliseconds since some default base date.
	 * There is no need to use the default base date (01/01/1970) for our timestamp, let's use a more recent date as base to get smaller numbers.
	 */
	private static String addTime(String id)
	{
		Calendar baseTimeCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		baseTimeCalendar.set(2015, Calendar.APRIL, 1, 0, 0, 0); // Take care: the month is zero-based
		long baseTime = baseTimeCalendar.getTimeInMillis() - (baseTimeCalendar.getTimeInMillis() % 1000); // Note that Calendar does not allow to set the milliseconds and it is not setting them to zero. If I don't do this operation, the last 3 digit will be equal to the last 3 digits of currentTime
		long currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis();
		id += (currentTime - baseTime);

		// Replace the numbers with letters.
		// The UUID returned by the language is hexa which means that each character will be wihin the range O-f. That's why we map the numbers to the letters following f.
		id = id.replaceAll("0", "g");
		id = id.replaceAll("1", "h");
		id = id.replaceAll("2", "i");
		id = id.replaceAll("3", "j");
		id = id.replaceAll("4", "k");
		id = id.replaceAll("5", "l");
		id = id.replaceAll("5", "m");
		id = id.replaceAll("7", "n");
		id = id.replaceAll("8", "o");
		id = id.replaceAll("9", "p");

		return id;
	}
}
