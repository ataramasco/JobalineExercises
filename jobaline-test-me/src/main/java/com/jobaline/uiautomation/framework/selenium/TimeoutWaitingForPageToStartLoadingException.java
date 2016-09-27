package com.jobaline.uiautomation.framework.selenium;

/**
 * Created by damian.j on 2/8/16.
 */

public class TimeoutWaitingForPageToStartLoadingException
	extends RuntimeException
{
	public TimeoutWaitingForPageToStartLoadingException()
	{
		super();
	}


	public TimeoutWaitingForPageToStartLoadingException(String message)
	{
		super(message);
	}
}
