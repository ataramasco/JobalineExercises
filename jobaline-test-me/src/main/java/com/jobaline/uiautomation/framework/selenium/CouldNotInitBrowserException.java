package com.jobaline.uiautomation.framework.selenium;

import com.jobaline.uiautomation.framework.testng.exception.FailException;

public class CouldNotInitBrowserException extends FailException
{
	private static final long serialVersionUID = 6785090626008046409L;


	/**
	 * Create an instance with the default message.
	 * */
	public CouldNotInitBrowserException()
	{
		super("The browser could not initialize. Possible causes are: a bug, bad configuration, problems connecting with the Selenium grid (Sauce Labs for example), 0 minutes available in Sauce Labs or Browserstack, maximum number of parallel browsers exceeded in Sauce Labs or Browserstack.");
	}


	public CouldNotInitBrowserException(Throwable cause)
	{
		super("The browser could not initialize. Exception: " + cause.getMessage(), cause);
	}

}
