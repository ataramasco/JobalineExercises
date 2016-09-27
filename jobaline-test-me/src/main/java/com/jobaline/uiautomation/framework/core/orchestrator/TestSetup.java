package com.jobaline.uiautomation.framework.core.orchestrator;

import com.jobaline.uiautomation.framework.core.orchestrator.actor.OrchestratorActors;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver.IEntityDependencyResolverActor;
import com.jobaline.uiautomation.framework.util.JSONObjectUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

/**
 * Created by damian on 4/15/15.
 */
public class TestSetup
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSetup.class.getName().replace("com.jobaline.uiautomation.", ""));

	private TestIoEntry testIoSpec;


	public TestSetup(TestIoEntry testIoSpec)
	{
		this.testIoSpec = testIoSpec;
	}


	private JSONObject resolveDataDependencies()
	{
		JSONObject dictionaries = new JSONObject();

		if(testIoSpec.getDataDependencies() != null && testIoSpec.getDataDependencies().size() != 0)
		{
			try
			{
				dictionaries = OrchestratorActors.getDataDependencyResolverActor().resolveDependencies(testIoSpec.getDataDependencies());
			}
			catch(UndeclaredThrowableException e)
			{
				if(e.getCause() == null)
				{
					LOGGER.warn(String.format("There was an exception when tried to resolve the data dependencies of the test/actor. Exception message: %s", e.getMessage()));
				}
				else
				{
					LOGGER.warn(String.format("There was an exception when tried to resolve the data dependencies of the test/actor. Exception message: %s / Cause message: %s", e.getMessage(), e.getCause().getMessage()));
				}

				throw e;
			}
		}

		return dictionaries;
	}


	private JSONObject resolveEntitiesDependencies()
	{
		JSONObject entities = new JSONObject();

		List<EntityDependency> entityDependencies = testIoSpec.getEntitiesDependencies();

		if(entityDependencies != null)
		{
			for(EntityDependency entityDependency : entityDependencies)
			{
				IEntityDependencyResolverActor entityResolverActor = OrchestratorActors.getEntityDependencyResolverActor(entityDependency);

				JSONObject entity = entityResolverActor.getEntity();

				entities.put(entityDependency.getTestParameterName(), entity);
			}
		}

		return entities;
	}


	/**
	 * Resolve the data and entities dependencies of the specified io entry of the test.
	 * */
	private JSONObject resolveDependencies()
	{
		JSONObject dictionaries = resolveDataDependencies();

		JSONObject entities = resolveEntitiesDependencies();

		return JSONObjectUtils.merge(dictionaries, entities);
	}


	/**
	 * */
	public JSONObject getData()
	{
		return resolveDependencies();
	}

}
