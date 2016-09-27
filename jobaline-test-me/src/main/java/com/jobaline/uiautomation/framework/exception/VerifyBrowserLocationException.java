package com.jobaline.uiautomation.framework.exception;

/**
 * This class must be used when the location of the browser is not correct. For example, after verify the title
 * or the url.
 * */
public class VerifyBrowserLocationException extends RuntimeException
{

	public VerifyBrowserLocationException(String message)
	{
		super(message);
	}
	
}
