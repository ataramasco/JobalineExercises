package com.jobaline.uiautomation.framework.core.orchestrator.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.jobaline.uiautomation.framework.core.orchestrator.DynamoJSONEncoder;
import com.jobaline.uiautomation.framework.core.orchestrator.MatrixEntry;
import com.jobaline.uiautomation.framework.dataAccess.DynamoDBClient;
import com.jobaline.uiautomation.framework.dataAccess.DynamoDBClientFactory;
import com.jobaline.uiautomation.framework.lang.ListUtils;
import com.jobaline.uiautomation.framework.lang.MapUtils;
import com.jobaline.uiautomation.framework.loggers.FrameworkLoggerFactory;

/**
 * Created by damian on 9/20/2015.
 */
class DynamoTestDatabase implements ITestDatabase
{
	private final static String TABLE_MATRIX = "tests-matrix";
	private final static String TABLE_ACTORS = "tests-actor-matrix";
	private final static String TABLE_DICTIONARIES = "tests-dictionaries";
	private final static String TABLE_ENTITIES_CACHE = "tests-entities-cache";

	private final static int TABLE_MATRIX_READ_CAPACITIES = 10;
	private final static int TABLE_MATRIX_WRITE_CAPACITIES = 2;
	private final static int TABLE_ACTORS_READ_CAPACITIES = 10;
	private final static int TABLE_ACTORS_WRITE_CAPACITIES = 2;
	private final static int TABLE_DICTIONARIES_READ_CAPACITIES = 10;
	private final static int TABLE_DICTIONARIES_WRITE_CAPACITIES = 2;
	private final static int TABLE_ENTITIES_READ_CAPACITIES = 50;
	private final static int TABLE_ENTITIES_WRITE_CAPACITIES = 50;


	private DynamoDBClient getDynamoDbClient()
	{
		return DynamoDBClientFactory.getClient();
	}


	@Override public MatrixEntry getTestDefinition(String testId)
	{
		MatrixEntry matrixEntry = null;

		List<String> matrixEntries_json = DynamoDBClientFactory.getClient().scanTable(TABLE_MATRIX);

		for(String matrixEntry_json : matrixEntries_json)
		{
			MatrixEntry matrixEntry_aux = new MatrixEntry(matrixEntry_json);

			if(matrixEntry_aux.getTestId().equals(testId))
			{
				matrixEntry = matrixEntry_aux;
			}
		}

		return matrixEntry;
	}


	@Override public List<MatrixEntry> getTestsDefinitions()
	{
		List<MatrixEntry> matrixEntries = new ArrayList<>();

		DynamoDBClientFactory.getClient().scanTable(TABLE_MATRIX).stream().forEach(matrixEntryJson -> matrixEntries.add(new MatrixEntry(matrixEntryJson)));

		return matrixEntries;
	}


	@Override public JSONObject getDictionary(String dictionaryId)
	{
		String json_source = DynamoDBClientFactory.getClient().getItem(TABLE_DICTIONARIES, "id", dictionaryId, null, null);

		if(json_source == null)
		{
			throw new RuntimeException("The dictionary '" + dictionaryId + "' is not defined in the db of dictionaries.");
		}

		JSONObject jsonObject;

		// Parse the dictionary as a json object
		{
			try
			{
				jsonObject = new JSONObject(json_source);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("Could not parse an item read from the DynamoDB, it seems that it could not be converted to a JSON object.");
			}
		}

		// Verify the data object is defined and read it. The actual dictionary is contained by this object.
		{
			if(!jsonObject.has("data"))
			{
				throw new RuntimeException("The dictionary '" + dictionaryId + "' defined in the db of dictionaries does not have the property 'data'.");
			}

			try
			{
				jsonObject = jsonObject.getJSONObject("data");
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("The 'data' property of the dictionary '" + dictionaryId + "' defined in the db of dictionaries is not a json object.");
			}
		}

		return jsonObject;
	}


	@Override public Map<String, JSONObject> getDictionaries()
	{
		Map<String, JSONObject> dictionaries = new HashMap<>();

		List<String> dictionaries_raw = DynamoDBClientFactory.getClient().scanTable(TABLE_DICTIONARIES);

		for(String dictionary_raw : dictionaries_raw)
		{
			JSONObject dictionary_wrapper = new JSONObject(dictionary_raw);

			dictionaries.put(dictionary_wrapper.getString("id"), dictionary_wrapper.getJSONObject("data"));
		}

		return dictionaries;
	}


	@Override public MatrixEntry getActorDefinition(String testId)
	{
		MatrixEntry matrixEntry = null;

		List<String> matrixEntries_json = DynamoDBClientFactory.getClient().scanTable(TABLE_ACTORS);

		for(String matrixEntry_json : matrixEntries_json)
		{
			MatrixEntry matrixEntry_aux = new MatrixEntry(matrixEntry_json);

			if(matrixEntry_aux.getTestId().equals(testId))
			{
				matrixEntry = matrixEntry_aux;
			}
		}

		return matrixEntry;
	}


	@Override public List<MatrixEntry> getActorsDefinitions()
	{
		List<MatrixEntry> matrixEntries = new ArrayList<>();

		DynamoDBClientFactory.getClient().scanTable(TABLE_ACTORS).stream().forEach(matrixEntryJson -> matrixEntries.add(new MatrixEntry(matrixEntryJson)));

		return matrixEntries;
	}


	private static final String RANGE_SEPARATOR = "-";


	private String createRange(String entityTypeName, Integer entityId)
	{
		return entityTypeName + RANGE_SEPARATOR + entityId;
	}


	private String createRangeBeginsWithCondition(String entityTypeName)
	{
		return entityTypeName + RANGE_SEPARATOR;
	}


	private String formatDate(Date date)
	{
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatGmt.format(date);
	}


	@Override public JSONObject getEntity(String environmentId, String entityTypeName, long expirationTime)
	{
		String tableName = TABLE_ENTITIES_CACHE;

		long startTime = System.currentTimeMillis();

		JSONObject entityData = null;

		String dateLimit = formatDate(new Date(System.currentTimeMillis() - expirationTime));

		QueryRequest queryRequest = new QueryRequest()
				.withTableName(tableName)
				.withKeyConditionExpression("environmentId = :v1 AND begins_with(entityTypeNameAndId, :v2)")
				.withFilterExpression("creationTime > :v3")
				.withExpressionAttributeValues(new MapUtils<String, AttributeValue>().createMap(
						":v1", new AttributeValue(environmentId),
						":v2", new AttributeValue(createRangeBeginsWithCondition(entityTypeName)),
						":v3", new AttributeValue(dateLimit)
				));

		QueryResult result = DynamoDBClientFactory.getClient().getDynamoDBClient().query(queryRequest);

		if(result.getCount() > 0)
		{
			Map<String, AttributeValue> map = ListUtils.getElementRandomly(result.getItems());
			Item item = Item.fromMap(InternalUtils.toSimpleMapValue(map));

			if(item == null)
			{
				throw new RuntimeException("Could not read the data of the entity from the transient db.");
			}

			try
			{
				entityData = new JSONObject(item.getJSON("data"));
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("The data of the entity read from the transient db is invalid, it can not be parsed as JSON.");
			}
		}

		FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB).debug(String.format("Time to get item from table '%s': %d milliseconds", tableName, System.currentTimeMillis() - startTime));

		return entityData;
	}


	@Override public void saveEntity(String environmentId, String entityTypeName, JSONObject entityData)
	{
		// The following cases shouldn't happen and if they do they must be fixed, but for now won't throw an exception because the only problem will be that
		// the system will not reuse entities, although it will not break anything.
		if(entityData == null)
		{
			FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB).debug("The entity to save in the transient db is null");
			return;
		}
		else if(entityData.length() == 0)
		{
			FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB).debug("Can not save the entity in the transient db because it is empty");
			return;
		}
		else if(!entityData.has("id") || entityData.isNull("id"))
		{
			FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB).debug("Can not save the entity in the transient db because it does not have an id");
			return;
		}

		String creationTime = formatDate(new Date());

		JSONObject item_json = new JSONObject();
		item_json.put("environmentId", environmentId);
		item_json.put("entityTypeNameAndId", createRange(entityTypeName, entityData.getInt("id")));
		item_json.put("creationTime", creationTime);
		item_json.put("data", DynamoJSONEncoder.encode(entityData));

		Item item = Item.fromJSON(item_json.toString());

		DynamoDBClientFactory.getClient().getDynamoDB().getTable(TABLE_ENTITIES_CACHE).putItem(item);
	}


	@Override public void clearCache()
	{
		getDynamoDbClient().deleteTable(TABLE_ENTITIES_CACHE);
		getDynamoDbClient().createTable(TABLE_ENTITIES_CACHE, "environmentId", DynamoDBClient.KEY_TYPE_STRING, "entityTypeNameAndId", DynamoDBClient.KEY_TYPE_STRING, TABLE_ENTITIES_READ_CAPACITIES, TABLE_ENTITIES_WRITE_CAPACITIES);
	}


	@Override public void migrateFrom(ITestDatabase source)
	{
		clearCache();
		migrateMatrix(source);
		migrateActorsMatrix(source);
		migrateDictionaries(source);
	}


	private void migrateMatrix(ITestDatabase source)
	{
		getDynamoDbClient().deleteTable(TABLE_ACTORS);
		getDynamoDbClient().createTable(TABLE_ACTORS, "id", DynamoDBClient.KEY_TYPE_STRING, TABLE_ACTORS_READ_CAPACITIES, TABLE_ACTORS_WRITE_CAPACITIES);

		List<MatrixEntry> actorDefinitions = source.getActorsDefinitions();
		for(MatrixEntry actorDefinition : actorDefinitions)
		{
			Item item = Item.fromJSON(actorDefinition.getJsonSource());
			DynamoDBClientFactory.getClient().getDynamoDB().getTable(TABLE_ACTORS).putItem(item);
		}
	}


	private void migrateActorsMatrix(ITestDatabase source)
	{
		getDynamoDbClient().deleteTable(TABLE_MATRIX);
		getDynamoDbClient().createTable(TABLE_MATRIX, "id", DynamoDBClient.KEY_TYPE_STRING, TABLE_MATRIX_READ_CAPACITIES, TABLE_MATRIX_WRITE_CAPACITIES);

		List<MatrixEntry> testDefinitions = source.getTestsDefinitions();
		for(MatrixEntry testDefinition : testDefinitions)
		{
			Item item = Item.fromJSON(testDefinition.getJsonSource());
			DynamoDBClientFactory.getClient().getDynamoDB().getTable(TABLE_MATRIX).putItem(item);
		}
	}


	private void migrateDictionaries(ITestDatabase source)
	{
		getDynamoDbClient().deleteTable(TABLE_DICTIONARIES);
		getDynamoDbClient().createTable(TABLE_DICTIONARIES, "id", DynamoDBClient.KEY_TYPE_STRING, TABLE_DICTIONARIES_READ_CAPACITIES, TABLE_DICTIONARIES_WRITE_CAPACITIES);

		Map<String, JSONObject> dictionaries = source.getDictionaries();
		for(String id : dictionaries.keySet())
		{
			JSONObject dictionaryData = dictionaries.get(id);
			JSONObject dictionary = new JSONObject()
					.put("id", id)
					.put("data", dictionaryData);

			Item item = Item.fromJSON(dictionary.toString());
			DynamoDBClientFactory.getClient().getDynamoDB().getTable(TABLE_DICTIONARIES).putItem(item);
		}
	}
}
