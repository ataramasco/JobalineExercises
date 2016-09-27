package com.jobaline.uiautomation.framework.core.orchestrator;

/**
 * Created by damian on 4/23/15.
 */
public class CouldNotResolveEntityDependencyException extends RuntimeException
{
	public CouldNotResolveEntityDependencyException(String message)
	{
		super(message);
	}


	public CouldNotResolveEntityDependencyException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
