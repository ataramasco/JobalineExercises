package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation;

import akka.dispatch.Futures;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Created by damian on 4/16/15.
 */
public abstract class EntityCreationActor implements IEntityCreationActor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreationActor.class.getName().replace("com.jobaline.uiautomation.", ""));

	private static int STATUS_NOT_CREATED     = 0;
	private static int STATUS_CREATED         = 1;
	private static int STATUS_CREATION_FAILED = 2;

	protected int createStatus = STATUS_NOT_CREATED;

	private int  tries      = -1;
	private long tryTimeout = -1;

	private JSONObject entity;

	private String    couldNotCreateEntityExceptionMessage;
	private Exception couldNotCreateEntityCause;


	/**
	 * The actor message timeout must be specified before creating the actor, it can not be an instance method.
	 * */
	public static long getActorMessageTime()
	{
		return 480000;
	}


	public void setNumberOfTries(int tries)
	{
		this.tries = tries;
	}


	public int getNumberOfTries()
	{
		if(tries == -1)
		{
			return getDefaultNumberOfTries();
		}
		else
		{
			return tries;
		}
	}


	public void setTryTimeout(long trieTimeout)
	{
		this.tryTimeout = trieTimeout;
	}


	public long getTryTimeout()
	{
		if(tryTimeout == -1)
		{
			return getDefaultTryTimeout();
		}
		else
		{
			return tryTimeout;
		}
	}


	/**
	 * Implement this method to create the entity.
	 * */
	protected abstract JSONObject createInternal(Supplier<JSONObject> dataProvider) throws CouldNotCreateEntityException;


	private JSONObject createInternalWithRetry(Supplier<JSONObject> dataProvider) throws CouldNotCreateEntityException
	{
		if(createStatus == STATUS_NOT_CREATED)
		{
			for(int i = 0; i < getNumberOfTries(); i++)
			{
				LOGGER.info("");
				LOGGER.info(String.format("Creating entity: '%s'. Try number: %d", getEntityName(), (i + 1)));
				LOGGER.info("");

				try
				{
					entity = createInternal(dataProvider);
					createStatus = STATUS_CREATED;
					break;
				}
				catch(Exception e)
				{
					if(i + 1 == getNumberOfTries())
					{
						createStatus = STATUS_CREATION_FAILED;
						couldNotCreateEntityCause = e;
						couldNotCreateEntityExceptionMessage = e.getMessage();
					}
					else
					{
						e.printStackTrace();
					}
				}
			}
		}

		if(createStatus == STATUS_CREATION_FAILED)
		{
			if(couldNotCreateEntityCause == null)
			{
				throw new CouldNotCreateEntityException(couldNotCreateEntityExceptionMessage);
			}
			else
			{
				throw new CouldNotCreateEntityException(couldNotCreateEntityExceptionMessage, couldNotCreateEntityCause);
			}
		}

		return entity;
	}


	public final Future<JSONObject> create(Supplier<JSONObject> dataProvider) throws TimeoutException, CouldNotCreateEntityException
	{
		return Futures.successful(createInternalWithRetry(dataProvider));
	}


	public final JSONObject createBlocking(Supplier<JSONObject> dataProvider) throws TimeoutException, CouldNotCreateEntityException
	{
		return createInternalWithRetry(dataProvider);
	}

}
