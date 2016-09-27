package com.jobaline.uiautomation.framework.selenium;

/**
 * Created by damian.j on 2/8/16.
 */
public class TimeoutWaitingForPageToLoadException extends RuntimeException
{
	public TimeoutWaitingForPageToLoadException()
	{
		super();
	}


	public TimeoutWaitingForPageToLoadException(String message)
	{
		super(message);
	}
}
