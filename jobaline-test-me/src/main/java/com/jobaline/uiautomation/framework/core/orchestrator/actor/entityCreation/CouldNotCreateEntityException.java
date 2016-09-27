package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation;

/**
 * Created by damian on 4/22/15.
 */
public class CouldNotCreateEntityException extends Exception
{

	public CouldNotCreateEntityException()
	{
	}


	public CouldNotCreateEntityException(String message)
	{
		super(message);
	}


	public CouldNotCreateEntityException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
