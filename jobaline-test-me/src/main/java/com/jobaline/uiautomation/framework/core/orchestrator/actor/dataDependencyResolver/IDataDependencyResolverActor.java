package com.jobaline.uiautomation.framework.core.orchestrator.actor.dataDependencyResolver;

import com.jobaline.uiautomation.framework.core.orchestrator.DataDependency;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by damian on 4/16/15.
 */
public interface IDataDependencyResolverActor
{
	public JSONObject resolveDependencies(List<DataDependency> dataDependencies);
}
