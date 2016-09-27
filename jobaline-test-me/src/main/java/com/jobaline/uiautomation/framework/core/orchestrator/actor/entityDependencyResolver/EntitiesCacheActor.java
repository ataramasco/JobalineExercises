package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver;

import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.core.orchestrator.database.TestDatabaseFactory;
import org.json.JSONObject;

/**
 * Created by damian on 4/27/2015.
 */
public class EntitiesCacheActor implements IEntitiesCacheActor
{

	/**
	 * Default time in seconds for an entity will be still valid.
	 * */
	public static final long DEFAULT_EXPIRATION_TIME = 24 * 60 * 60 * 1000;


	@Override public JSONObject getEntity(String entityTypeName)
	{
		return TestDatabaseFactory.instance().getEntity(EnvironmentUtils.getEnvironmentId(), entityTypeName, DEFAULT_EXPIRATION_TIME);
	}


	public void saveEntity(String entityTypeName, JSONObject entityData)
	{
		TestDatabaseFactory.instance().saveEntity(EnvironmentUtils.getEnvironmentId(), entityTypeName, entityData);
	}

}
