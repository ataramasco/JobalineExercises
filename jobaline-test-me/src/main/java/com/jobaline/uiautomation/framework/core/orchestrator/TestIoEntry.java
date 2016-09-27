package com.jobaline.uiautomation.framework.core.orchestrator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by damian on 4/23/15.
 */
public class TestIoEntry
{
	private String matrixEntryId;

	public JSONObject data;

	private List<DataDependency>   dataDependencies;
	private List<EntityDependency> entitiesDependencies;

	private List<String> outputEntities;


	public TestIoEntry(String matrixEntryId, JSONObject data)
	{
		this.matrixEntryId = matrixEntryId;
		this.data = data;
	}


	public String getMatrixEntryId()
	{
		return matrixEntryId;
	}


	public boolean isEnabled()
	{
		return !data.has("enabled") || data.getBoolean("enabled");
	}


	public String getDescription()
	{
		return data.has("description")? data.getString("description") : null;
	}


	public synchronized List<DataDependency> getDataDependencies()
	{
		if(dataDependencies == null && data.has("input-data"))
		{
			dataDependencies = new ArrayList<>();

			JSONObject dataDependencies_json = data.getJSONObject("input-data");
			Iterator<String> it = dataDependencies_json.keys();
			while(it.hasNext())
			{
				String testParameterName = it.next();
				dataDependencies.add(new DataDependency(testParameterName, dataDependencies_json.get(testParameterName)));
			}
		}

		return dataDependencies;
	}


	public synchronized List<EntityDependency> getEntitiesDependencies()
	{
		if(entitiesDependencies == null && data.has("input-entities"))
		{
			entitiesDependencies = new ArrayList<>();

			JSONObject entitiesDependencies_json = data.getJSONObject("input-entities");
			Iterator<String> it = entitiesDependencies_json.keys();
			while(it.hasNext())
			{
				String testParameterName = it.next();
				Object entityInfo = entitiesDependencies_json.get(testParameterName);

				EntityDependency entityDependency;
				if(String.class.isAssignableFrom(entityInfo.getClass()))
				{
					String entityName = (String)entityInfo;
					entityDependency = new EntityDependency(testParameterName, entityName, null);
				}
				else if(JSONObject.class.isAssignableFrom(entityInfo.getClass()))
				{
					String entityName = ((JSONObject)entityInfo).getString("name");
					String entityScope = ((JSONObject)entityInfo).getString("scope");
					entityDependency = new EntityDependency(testParameterName, entityName, entityScope);
				}
				else
				{
					throw new RuntimeException("The entity dependency format is invalid");
				}

				entitiesDependencies.add(entityDependency);
			}
		}

		return entitiesDependencies;
	}


	public boolean hasOutputEntities()
	{
		return data.has("output") && data.getJSONArray("output").length() != 0;
	}


	public synchronized List<String> getOutputEntities()
	{
		if(outputEntities == null && data.has("output"))
		{
			outputEntities = new ArrayList<>();

			JSONArray output_json = data.getJSONArray("output");
			for(int i = 0; i < output_json.length(); i++)
			{
				outputEntities.add(output_json.getString(i));
			}
		}

		return outputEntities;
	}

}
