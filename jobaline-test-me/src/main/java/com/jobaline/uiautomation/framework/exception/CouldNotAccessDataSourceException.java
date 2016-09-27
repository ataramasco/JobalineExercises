package com.jobaline.uiautomation.framework.exception;

/**
 * This exception is intended to be thrown when a DataService is requested to get an entity that does not exist. For example the job with id 1.
 *
 * It is not intended to verify that a particular entity exists. This is, it is not intended for tests asserts. This is an Exception that will be thrown on rare unexpected conditions
 * that shouldn't happen.
 *
 * Created by damian on 11/22/2014.
 */
public class CouldNotAccessDataSourceException extends RuntimeException
{
	public CouldNotAccessDataSourceException(Throwable cause)
	{
		super("Could not access a data source. See the console output for more details.", cause);

		cause.printStackTrace();
	}
}
