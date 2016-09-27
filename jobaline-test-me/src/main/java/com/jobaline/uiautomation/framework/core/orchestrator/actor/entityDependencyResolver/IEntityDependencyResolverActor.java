package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver;

import com.jobaline.uiautomation.framework.core.orchestrator.EntityDependency;
import org.json.JSONObject;

/**
 * Created by damian on 4/23/15.
 */
public interface IEntityDependencyResolverActor
{
	public void setEntityDependency(EntityDependency entityDependency);

	public EntityDependency getEntityDependency();

	public JSONObject getEntity();
}
