package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation;

/**
 * Created by damian on 4/22/15.
 */
public class InvalidInputDataToCreateEntityException extends RuntimeException
{

	public InvalidInputDataToCreateEntityException()
	{
	}


	public InvalidInputDataToCreateEntityException(String message)
	{
		super(message);
	}


	public InvalidInputDataToCreateEntityException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
