package com.jobaline.uiautomation.framework.core.orchestrator;

/**
 * Created by damian on 4/23/15.
 */
public class EntityDependency
{
	public static final String SCOPE_GLOBAL = "global";
	public static final String SCOPE_LOCAL  = "local";
	public static final String SCOPE_TEST   = "test";

	private String testParameterName;
	private String entityTypeName;
	private String scope;


	public EntityDependency(String testParameterName, String entityTypeName, String scope)
	{
		this.testParameterName = testParameterName;
		this.entityTypeName = entityTypeName;

		if(scope != null)
		{
			this.scope = scope;
		}
		else
		{
			this.scope = EntityDependency.SCOPE_GLOBAL;
		}
	}


	public String getTestParameterName()
	{
		return testParameterName;
	}



	public String getEntityTypeName()
	{
		return entityTypeName;
	}



	public String getScope()
	{
		return scope;
	}




	public boolean hasGlobalScope()
	{
		return this.scope.equals(SCOPE_GLOBAL);
	}


	public boolean hasLocalScope()
	{
		return this.scope.equals(SCOPE_LOCAL);
	}


	public boolean hasTestScope()
	{
		return this.scope.equals(SCOPE_TEST);
	}
}
