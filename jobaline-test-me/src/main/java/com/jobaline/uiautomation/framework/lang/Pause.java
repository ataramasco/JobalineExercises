package com.jobaline.uiautomation.framework.lang;

import java.util.concurrent.TimeUnit;

public class Pause
{

	/**
	 * Pauses the current Thread for one second.
	 */
	public static void oneSecond()
	{
		seconds(1);
	}


	/**
	 * Pause the current Thread for two seconds.
	 */
	public static void twoSeconds()
	{
		seconds(2);
	}


	/**
	 * Pause the current Thread for three seconds.
	 */
	public static void threeSeconds()
	{
		seconds(3);
	}


	/**
	 * Pause the current Thread for five seconds.
	 */
	public static void fiveSeconds()
	{
		seconds(5);
	}


	/**
	 * Pause the current Thread for ten seconds.
	 */
	public static void tenSeconds()
	{
		seconds(10);
	}


	/**
	 * Pause the current Thread for fifteen seconds.
	 */
	public static void fifteenSeconds()
	{
		seconds(15);
	}


	/**
	 * Pause the current Thread the specified number of seconds.
	 */
	public static void seconds(final int seconds)
	{
		try
		{
			TimeUnit.SECONDS.sleep(seconds);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Pause the current Thread the specified number of milliseconds.
	 */
	public static void milliseconds(final long milliseconds)
	{
		try
		{
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
