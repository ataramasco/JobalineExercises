package com.jobaline.uiautomation.framework.core.orchestrator.actor;

import com.jobaline.uiautomation.framework.core.actor.TestsActorSystem;
import com.jobaline.uiautomation.framework.core.orchestrator.EntityDependency;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.dataDependencyResolver.DataDependencyResolverActor;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.dataDependencyResolver.IDataDependencyResolverActor;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation.IEntityCreationActor;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver.*;

/**
 * Created by damian on 4/27/2015.
 */
public class OrchestratorActors
{
	private static long DEFAULT_TIMEOUT_FOR_ACTORS_GETTING_DATA_FROM_TEST_DATABASE = 30000;

	private static long DEFAULT_TIMEOUT_FOR_ACTORS_CREATING_ENTITIES = 900000;


	public synchronized static IMatrixActor getMatrixActor()
	{
		return TestsActorSystem.getSingletonActor(MatrixActor.class, DEFAULT_TIMEOUT_FOR_ACTORS_GETTING_DATA_FROM_TEST_DATABASE);
	}


	public synchronized static IEntityCreationActor getEntityCreationActor(Class<? extends IEntityCreationActor> actorClass, EntityDependency entityDependency)
	{
		IEntityCreationActor actor;
		if(entityDependency.hasGlobalScope() || entityDependency.hasLocalScope())
		{
			actor = TestsActorSystem.getSingletonActor(actorClass, entityDependency.getEntityTypeName(), DEFAULT_TIMEOUT_FOR_ACTORS_CREATING_ENTITIES);
		}
		else if(entityDependency.hasTestScope())
		{
			actor = TestsActorSystem.getActor(actorClass, DEFAULT_TIMEOUT_FOR_ACTORS_CREATING_ENTITIES);
		}
		else
		{
			throw new RuntimeException("Entity dependency scope not valid: " + entityDependency.getScope());
		}

		return actor;
	}


	public synchronized static IDataDependencyResolverActor getDataDependencyResolverActor()
	{
		IDataDependencyResolverActor actor = TestsActorSystem.getSingletonActor(DataDependencyResolverActor.class, DEFAULT_TIMEOUT_FOR_ACTORS_GETTING_DATA_FROM_TEST_DATABASE);
		return actor;
	}


	public synchronized static IEntitiesCacheActor getTransientEntitiesActor()
	{
		return TestsActorSystem.getSingletonActor(EntitiesCacheActor.class, DEFAULT_TIMEOUT_FOR_ACTORS_GETTING_DATA_FROM_TEST_DATABASE);
	}


	public synchronized static IEntityDependencyResolverActor getEntityDependencyResolverActor(EntityDependency entityDependency)
	{
		IEntityDependencyResolverActor actor;
		if(entityDependency.hasGlobalScope() || entityDependency.hasLocalScope())
		{
			actor = TestsActorSystem.getSingletonActor(EntityDependencyResolverActor.class, entityDependency.getEntityTypeName(), DEFAULT_TIMEOUT_FOR_ACTORS_CREATING_ENTITIES);
			actor.setEntityDependency(entityDependency);
		}
		else if(entityDependency.hasTestScope())
		{
			actor = TestsActorSystem.getActor(EntityDependencyResolverActor.class, DEFAULT_TIMEOUT_FOR_ACTORS_CREATING_ENTITIES);
			actor.setEntityDependency(entityDependency);
		}
		else
		{
			throw new RuntimeException("Entity dependency scope not valid: " + entityDependency.getScope());
		}

		return actor;
	}

}
