package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver;

import com.jobaline.uiautomation.framework.core.orchestrator.CouldNotResolveEntityDependencyException;
import com.jobaline.uiautomation.framework.core.orchestrator.EntityDependency;
import com.jobaline.uiautomation.framework.core.orchestrator.TestIoEntry;
import com.jobaline.uiautomation.framework.core.orchestrator.TestSetup;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.OrchestratorActors;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation.CouldNotCreateEntityException;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation.IEntityCreationActor;
import com.jobaline.uiautomation.framework.EnvironmentUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * Created by damian on 4/23/15.
 */
public class EntityDependencyResolverActor
	implements IEntityDependencyResolverActor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityDependencyResolverActor.class.getName().replace("com.jobaline.uiautomation.", ""));

	private EntityDependency entityDependency = null;

	private JSONObject entity = null;


	public void setEntityDependency(EntityDependency entityDependency)
	{
		this.entityDependency = entityDependency;
	}


	public EntityDependency getEntityDependency()
	{
		return entityDependency;
	}


	private JSONObject tryToGetEntityFromTransientDB()
	{
		JSONObject entity = null;

		try
		{
			entity = OrchestratorActors.getTransientEntitiesActor().getEntity(getEntityDependency().getEntityTypeName());
		}
		catch(Exception e)
		{
			if(e.getCause() == null)
			{
				LOGGER.warn(String.format("There was an exception when tried to get the entity from the transient DB. Exception message: %s", e.getMessage()));
			}
			else
			{
				LOGGER.warn(String.format("There was an exception when tried to get the entity from the transient DB. Exception message: %s / Cause message: %s", e.getMessage(), e.getCause().getMessage()));
			}
		}

		return entity;
	}


	private JSONObject createEntityUsingActor(TestIoEntry testIoEntry)
	{
		JSONObject entity;

		IEntityCreationActor entityCreationActor;

		String actorClassName = EnvironmentUtils.getDataCreationActorsPackage() + "." + testIoEntry.getMatrixEntryId();
		Class actorClass;
		try
		{
			actorClass = Class.forName(actorClassName);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Could not find the actor class: " + actorClassName);
		}

		entityCreationActor = OrchestratorActors.getEntityCreationActor(actorClass, getEntityDependency());

		try
		{
			entity = entityCreationActor.createBlocking(() -> new TestSetup(testIoEntry).getData());
		}
		catch(TimeoutException e)
		{
			e.printStackTrace();
			throw new CouldNotResolveEntityDependencyException("Could not create the entity.", e);
		}
		catch(CouldNotCreateEntityException e)
		{
			throw new CouldNotResolveEntityDependencyException("Could not create the entity.", e);
		}

		return entity;
	}


	private JSONObject createEntityUsingTest(TestIoEntry testIoEntry)
	{
		throw new UnsupportedOperationException("Creating entities using tests is not implemented. Please, use an actor to create the entity.");
	}


	private JSONObject createEntity()
	{
		TestIoEntry testIoEntry = OrchestratorActors.getMatrixActor().resolveActorThatCreatesEntity(getEntityDependency().getEntityTypeName());

		if(testIoEntry != null)
		{
			entity = createEntityUsingActor(testIoEntry);
		}
		else
		{
			testIoEntry = OrchestratorActors.getMatrixActor().resolveTestThatCreatesEntity(getEntityDependency().getEntityTypeName());

			if(testIoEntry == null)
			{
				throw new CouldNotResolveEntityDependencyException("Could not find an actor/test that creates the entity: " + getEntityDependency().getEntityTypeName());
			}

			entity = createEntityUsingTest(testIoEntry);
		}

		return entity;
	}


	public JSONObject getEntity()
	{
		if(getEntityDependency().hasTestScope())
		{
			return createEntity();
		}
		else
		{
			if(entity == null)
			{
				if(getEntityDependency().hasGlobalScope())
				{
					entity = tryToGetEntityFromTransientDB();
				}

				if(entity == null)
				{
					entity = createEntity();

					if(getEntityDependency().hasGlobalScope())
					{
						OrchestratorActors.getTransientEntitiesActor().saveEntity(getEntityDependency().getEntityTypeName(), entity);
					}
				}
			}

			return entity;
		}
	}

}
