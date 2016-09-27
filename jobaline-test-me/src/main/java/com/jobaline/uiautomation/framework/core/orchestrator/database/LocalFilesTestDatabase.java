package com.jobaline.uiautomation.framework.core.orchestrator.database;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.core.orchestrator.MatrixEntry;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by damian on 9/20/2015.
 */
class LocalFilesTestDatabase implements ITestDatabase
{

	private List<MatrixEntry> getMatrixEntries(String folder)
	{
		List<MatrixEntry> matrixEntries = new ArrayList<>();

		File actorsMatrixFolder = new File(ResourceManager.getResourceAbsolutePath(folder));

		ArrayList<File> files = new ArrayList<>(Arrays.asList(actorsMatrixFolder.listFiles()));

		for(File file : files)
		{
			String fileContent = null;
			try
			{
				fileContent = FileUtils.readFileToString(file);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			if(fileContent != null)
			{
				try
				{
					JSONArray matrixEntries_json = new JSONArray(fileContent);
					for(int i = 0; i < matrixEntries_json.length(); i++)
					{
						matrixEntries.add(new MatrixEntry(matrixEntries_json.getJSONObject(i).toString()));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return matrixEntries;
	}


	private MatrixEntry getMatrixEntry(String folder, String entryId)
	{
		MatrixEntry matrixEntry = null;

		File matrixFolder = new File(ResourceManager.getResourceAbsolutePath(folder));

		ArrayList<File> files = new ArrayList<>(Arrays.asList(matrixFolder.listFiles()));

		mainLoop:
		for(File file : files)
		{
			String fileContent = null;
			try
			{
				fileContent = FileUtils.readFileToString(file);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			if(fileContent != null)
			{
				try
				{
					JSONArray testDefinitions = new JSONArray(fileContent);
					for(int i = 0; i < testDefinitions.length(); i++)
					{
						MatrixEntry matrixEntry_aux = new MatrixEntry(testDefinitions.getJSONObject(i).toString());

						if(matrixEntry_aux.getTestId().equals(entryId))
						{
							matrixEntry = matrixEntry_aux;
							break mainLoop;
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return matrixEntry;
	}


	private String getEntitiesFilePath()
	{
		String fileSep = System.getProperty("file.separator");
		return System.getProperty("user.home") + fileSep + "test-entities-cache-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".json";
	}


	@Override public MatrixEntry getTestDefinition(String testId)
	{
		return getMatrixEntry("testsDatabase/matrix", testId);
	}


	@Override public List<MatrixEntry> getTestsDefinitions()
	{
		return getMatrixEntries("testsDatabase/matrix");
	}


	@Override public JSONObject getDictionary(String dictionaryId)
	{
		JSONObject dictionary = null;

		File dictionariesFolder = new File(ResourceManager.getResourceAbsolutePath("testsDatabase/dictionaries"));

		ArrayList<File> files = new ArrayList<>(Arrays.asList(dictionariesFolder.listFiles()));

		mainLoop:
		for(File file : files)
		{
			String fileContent = null;
			try
			{
				fileContent = FileUtils.readFileToString(file);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			if(fileContent != null)
			{
				try
				{
					JSONArray dictionaries = new JSONArray(fileContent);
					for(int i = 0; i < dictionaries.length(); i++)
					{
						if(dictionaries.getJSONObject(i).getString("id").equals(dictionaryId))
						{
							dictionary = dictionaries.getJSONObject(i).getJSONObject("data");
							break mainLoop;
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return dictionary;
	}


	@Override public Map<String, JSONObject> getDictionaries()
	{
		Map<String, JSONObject> dictionaries = new HashMap<>();

		File dictionariesFolder = new File(ResourceManager.getResourceAbsolutePath("testsDatabase/dictionaries"));

		ArrayList<File> files = new ArrayList<>(Arrays.asList(dictionariesFolder.listFiles()));

		mainLoop:
		for(File file : files)
		{
			String fileContent = null;
			try
			{
				fileContent = FileUtils.readFileToString(file);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}

			if(fileContent != null)
			{
				try
				{
					JSONArray dictionaries_aux = new JSONArray(fileContent);
					for(int i = 0; i < dictionaries_aux.length(); i++)
					{
						dictionaries.put(dictionaries_aux.getJSONObject(i).getString("id"), dictionaries_aux.getJSONObject(i).getJSONObject("data"));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return dictionaries;
	}


	@Override public MatrixEntry getActorDefinition(String testId)
	{
		return getMatrixEntry("testsDatabase/actors", testId);
	}


	@Override public List<MatrixEntry> getActorsDefinitions()
	{
		return getMatrixEntries("testsDatabase/actors");
	}


	/**
	 * Save the entity to be reused later.
	 * Note that in this implementation of the test database, entities will be reused only within the same say. So if expirationTime is bigger than 24hours, it will
	 * no have any effect, any entity returned by this method would be older than 25 hs.
	 * */
	@Override public JSONObject getEntity(String environmentId, String entityTypeName, long expirationTime)
	{
		// This implementation will return always the first entity found, even when saveEntity() is saving entities several entities per type

		JSONObject entityData = null;

		File entitiesFile = new File(getEntitiesFilePath());
		if(entitiesFile.exists())
		{
			// For the sake of performance, the entities file content will be a pseudo json array:
			// ,{},{},{}
			try
			{
				StringBuilder fileContent = new StringBuilder();
				fileContent.append(FileUtils.readFileToString(entitiesFile));
				fileContent.replace(0, 1, "[");
				fileContent.append("]");

				JSONArray jsonArray = new JSONArray(fileContent.toString());
				for(int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject entityWrapper = jsonArray.getJSONObject(i);

					if(entityWrapper.getString("environmentId").equals(environmentId)
						&& entityWrapper.getString("entityTypeName").equals(entityTypeName)
						&& System.currentTimeMillis() - entityWrapper.getLong("creationTime") < expirationTime
						)
					{
						entityData = entityWrapper.getJSONObject("data");
						break;
					}
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		return entityData;
	}


	/**
	 * Save the entity to be reused later.
	 * Note that in this implementation of the test database, entities will be reused only within the same say.
	 * */
	@Override public void saveEntity(String environmentId, String entityTypeName, JSONObject entityData)
	{
		// For the sake of performance, the entities file content will be a pseudo json array:
		// ,{},{},{}

		JSONObject entityWrapper = new JSONObject();
		entityWrapper.put("environmentId", environmentId);
		entityWrapper.put("entityTypeName", entityTypeName);
		entityWrapper.put("creationTime", System.currentTimeMillis());
		entityWrapper.put("data", entityData);

		try
		{
			StringBuilder fileContentToAppend = new StringBuilder();
			fileContentToAppend.append(",");
			fileContentToAppend.append(entityWrapper.toString());

			FileUtils.write(new File(getEntitiesFilePath()), fileContentToAppend.toString(), Charset.forName("UTF-8"), true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}


	@Override public void clearCache()
	{
		throw new UnsupportedOperationException("Not implemented yet");
	}


	@Override public void migrateFrom(ITestDatabase source)
	{
		throw new UnsupportedOperationException("Can not migrateFrom to this database");
	}
}
