package com.jobaline.uiautomation.framework.testng.exception;

import org.testng.SkipException;

/**
 * This class can be used to mark tests as failed.
 * */
public class FailException extends SkipException
{
	/**
	 * Randomly generated serialVersionUID
	 */
	private static final long serialVersionUID = -4309119505505201400L;


	public FailException(String message)
	{
		super(message);
	}


	public FailException(String message, Throwable cause)
	{
		super(message, cause);
	}


	/**
	 * Please, see the documentation of SkipException.isSkip()
	 */
	public boolean isSkip()
	{
		return false;
	}

}
