package com.jobaline.uiautomation.framework.core.orchestrator.database;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.jobaline.uiautomation.framework.core.orchestrator.MatrixEntry;

/**
 * Created by damian on 9/20/2015.
 */
public interface ITestDatabase
{
	public MatrixEntry getTestDefinition(String testId);

	public List<MatrixEntry> getTestsDefinitions();

	public JSONObject getDictionary(String dictionaryId);

	public Map<String, JSONObject> getDictionaries();

	public MatrixEntry getActorDefinition(String testId);

	public List<MatrixEntry> getActorsDefinitions();

	public JSONObject getEntity(String environmentId, String entityTypeName, long expirationTime);

	public void saveEntity(String environmentId, String entityTypeName, JSONObject entityData);

	/**
	 * Migrates one database from another used as a source
	 * */
	public void migrateFrom(ITestDatabase source);

	public void clearCache();

}
