package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver;

import org.json.JSONObject;

/**
 * Created by damian on 4/23/15.
 */
public interface IEntitiesCacheActor
{
	public JSONObject getEntity(String entityTypeName);

	public void saveEntity(String entityTypeName, JSONObject entity);
}
