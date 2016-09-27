package com.jobaline.uiautomation.framework.core.orchestrator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by damian on 4/23/15.
 */
public class DataDependency
{
	private String testParameterName;
	private Object dependency;


	/**
	 *
	 * */
	public DataDependency(String testParameterName, Object dependency)
	{
		this.testParameterName = testParameterName;
		this.dependency = dependency;
	}


	public String getTestParameterName()
	{
		return testParameterName;
	}


	/**
	 * A data dependency may include the actual dictionary or an id that will act as a reference to a dictionary defined
	 * in the dictionaries table
	 * */
	public boolean isReferenceToDictionariesDatabase()
	{
		return String.class.isAssignableFrom(dependency.getClass());
	}


	public String getDependencyId()
	{
		if(!String.class.isAssignableFrom(dependency.getClass()))
		{
			throw new RuntimeException("Can not return the id of this dependency because it seems to not be defined. Is this dependency defining the actual dictionary instead of a reference to a dictionary?");
		}

		return (String)dependency;
	}


	public String getDataDictionary()
	{
		if(!JSONObject.class.isAssignableFrom(dependency.getClass()) && !JSONArray.class.isAssignableFrom(dependency.getClass()))
		{
			throw new RuntimeException("Can not return the dictionary of this dependency because it seems to not be defined. Is this dependency defining a reference to a dictionary instead of the actual dictionary?");
		}

		return dependency.toString();
	}
}
