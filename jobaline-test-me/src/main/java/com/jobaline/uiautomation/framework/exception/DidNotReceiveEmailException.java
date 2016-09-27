package com.jobaline.uiautomation.framework.exception;

/**
 * Created by damian on 9/2/15.
 */
public class DidNotReceiveEmailException extends RuntimeException
{
	public DidNotReceiveEmailException(String message)
	{
		super(message);
	}


	public DidNotReceiveEmailException(String message, Throwable cause)
	{
		super(message, cause);
	}


	public DidNotReceiveEmailException(Throwable cause)
	{
		super(cause);
	}
}
