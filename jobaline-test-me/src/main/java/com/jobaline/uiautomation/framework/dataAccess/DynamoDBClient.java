package com.jobaline.uiautomation.framework.dataAccess;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.*;
import com.jobaline.uiautomation.framework.loggers.FrameworkLoggerFactory;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by damian on 8/16/2015.
 */
public class DynamoDBClient
{
	public static final String KEY_TYPE_STRING = "S";
	public static final String KEY_TYPE_NUMBER = "N";

	private Map<String, List<String>> scanTableItems = new HashMap<>();

	private Map<String, String> itemsByHashAndRange = new HashMap<>();

	private AmazonDynamoDBClient dynamoDBClient;
	private DynamoDB             dynamoDB;


	public DynamoDBClient(AmazonDynamoDBClient dynamoDBClient)
	{
		this.dynamoDBClient = dynamoDBClient;
	}


	public AmazonDynamoDBClient getDynamoDBClient()
	{
		return dynamoDBClient;
	}


	public DynamoDB getDynamoDB()
	{
		if(dynamoDB == null)
		{
			dynamoDB = new DynamoDB(getDynamoDBClient());
		}

		return dynamoDB;
	}


	private Logger getLogger()
	{
		return FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB);
	}


	private List<String> parseResult(ScanResult result)
	{
		List<String> items_json = new ArrayList<>();

		if(result.getCount() != 0)
		{
			List<Map<String, AttributeValue>> items = result.getItems();

			for(Map<String, AttributeValue> map : items)
			{
				if(map == null)
				{
					continue;
				}

				Item item = Item.fromMap(InternalUtils.toSimpleMapValue(map));
				if(item != null)
				{
					items_json.add(item.toJSON());
				}
			}
		}

		return items_json;
	}


	private List<String> partialTableScan(String tableName)
	{
		List<String> items_json = new ArrayList<>();

		long startTime = System.currentTimeMillis();

		ScanRequest req = new ScanRequest();
		req.setTableName(tableName);

		ScanResult result = getDynamoDBClient().scan(req);

		items_json.addAll(parseResult(result));

		getLogger().debug(String.format("Time to execute scan command '%s': %d milliseconds", tableName, System.currentTimeMillis() - startTime));

		return items_json;
	}


	public List<String> scanTable(String tableName)
	{
		synchronized(tableName)
		{
			getLogger().debug(String.format("Scanning table: %s", tableName));

			if(!scanTableItems.containsKey(tableName))
			{
				long startTime = System.currentTimeMillis();

				List<String> items_json = new ArrayList<>();

				ScanResult result = null;
				do
				{
					ScanRequest req = new ScanRequest();
					req.setTableName(tableName);
					if(result != null)
					{
						req.setExclusiveStartKey(result.getLastEvaluatedKey());
					}

					result = getDynamoDBClient().scan(req);

					items_json.addAll(parseResult(result));

				} while(result.getLastEvaluatedKey() != null);

				getLogger().debug(String.format("Time to scan whole table '%s': %d milliseconds", tableName, System.currentTimeMillis() - startTime));

				scanTableItems.put(tableName, items_json);
			}
		}

		return scanTableItems.get(tableName);
	}


	/**
	 * hashName must be always specified. If hashValue is null, will match all hash values.
	 *
	 * If the table has a range, then rangeName must be specified. If rangeValue is null, will match all hash values.
	 *
	 * Examples:
	 *
	 *   deleteItems("myTable", "itemId", null, null, null); // Will delete all items. The table has only hash.
	 *   deleteItems("myTable", "itemId", null, "itemRange", null); // Will delete all items. The table has hash and range.
	 *   deleteItems("myTable", "itemId", "1", "itemRange", null); // Will delete all items with hash "1" and any range. The table has hash and range.
	 *   deleteItems("myTable", "itemId", "1", "itemRange", "0"); // Will delete the item with hash "1" and range "0". The table has hash and range.
	 *   deleteItems("myTable", "itemId", null, "itemRange", "0"); // Will delete all items with range "0" and any hash. The table has hash and range.
	 * */
	public void deleteItems(String tableName, String hashName, String hashValue, String rangeName, String rangeValue)
	{
		synchronized(tableName)
		{
			List<String> itemsJson = partialTableScan(tableName);
			while(itemsJson.size() != 0)
			{
				Table table = getDynamoDB().getTable(tableName);

				for(String itemJson : itemsJson)
				{
					String currentHashValue = new JSONObject(itemJson).getString(hashName);

					if(rangeName == null)
					{
						if(hashValue == null || hashValue.equals(currentHashValue))
						{
							getLogger().debug(String.format("Deleting item. Table name: %s, hashName: %s, hashValue: %s", tableName, hashName, currentHashValue));
							table.deleteItem(hashName, currentHashValue);
						}
					}
					else
					{
						String currentRangeValue = new JSONObject(itemJson).getString(rangeName);

						if((hashValue == null || hashValue.equals(currentHashValue)) && (rangeValue == null || rangeValue.equals(currentRangeValue)))
						{
							getLogger().debug(String.format("Deleting item. Table name: %s, hashName: %s, hashValue: %s, rangeName: %s, rangeValue: %s", tableName, hashName, currentHashValue, rangeName, currentRangeValue));
							table.deleteItem(hashName, currentHashValue, rangeName, currentRangeValue);
						}
					}
				}

				itemsJson = partialTableScan(tableName);
			}

			scanTableItems.remove(tableName);
		}
	}


	public String getItem(final String tableName, final String hashName, final String hashValue, final String rangeName, final String rangeValue)
	{
		final StringBuilder item_json = new StringBuilder();

		String itemKey = tableName + hashValue + rangeValue;

		synchronized(itemKey)
		{
			if(itemsByHashAndRange.containsKey(itemKey))
			{
				item_json.append(itemsByHashAndRange.get(itemKey));
			}
			else
			{
				long startTime = System.currentTimeMillis();

				Thread t = new Thread()
				{
					public void run()
					{
						Table table = getDynamoDB().getTable(tableName);

						if(rangeName == null)
						{
							Item item = table.getItem(hashName, hashValue);

							if(item != null)
							{
								item_json.append(item.toJSON());
							}
						}
						else
						{
							new UnsupportedOperationException("Not yet implemented");
						}
					}
				};

				t.start();

				long logInterval = 500;
				long lastLogTime = System.currentTimeMillis();
				while(t.isAlive())
				{
					Thread.yield();
					if(System.currentTimeMillis() - lastLogTime > logInterval)
					{
						FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB).debug(String.format("Waiting to get item from table '%s'. It's been %d milliseconds. {hashName: '%s', hashValue: '%s', rangeName: '%s', rangeValue: '%s'}", tableName, System.currentTimeMillis() - startTime, hashName, hashValue, rangeName, rangeValue));
						lastLogTime = System.currentTimeMillis();
					}
				}

				FrameworkLoggerFactory.getLogger(FrameworkLoggerFactory.LOGGER_DYNAMODB).debug(String.format("Time to get item from table '%s': %d milliseconds. {hashName: '%s', hashValue: '%s', rangeName: '%s', rangeValue: '%s'}", tableName, System.currentTimeMillis() - startTime, hashName, hashValue, rangeName, rangeValue));

				itemsByHashAndRange.put(itemKey, item_json.toString());
			}
		}

		return item_json.toString();
	}


	/**
	 * Creates a table with a hash key and no range key.
	 *
	 * hashType must be either KEY_TYPE_STRING or KEY_TYPE_NUMBER
	 * */
	public void createTable(String tableName, String hashName, String hashType, long readCapacity, long writeCapacity)
	{
		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();

		attributeDefinitions.add(new AttributeDefinition()
			.withAttributeName(hashName)
			.withAttributeType(hashType));

		keySchema.add(new KeySchemaElement()
			.withAttributeName(hashName)
			.withKeyType(KeyType.HASH));

		CreateTableRequest request = new CreateTableRequest()
			.withTableName(tableName)
			.withKeySchema(keySchema)
			.withAttributeDefinitions(attributeDefinitions)
			.withProvisionedThroughput(new ProvisionedThroughput()
				.withReadCapacityUnits(readCapacity)
				.withWriteCapacityUnits(writeCapacity));

		Table table = getDynamoDB().createTable(request);

		try
		{
			table.waitForActive();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}


	public boolean tableExists(String tableName)
	{
		Table table = getDynamoDB().getTable(tableName);
		return tableExists(table);
	}


	private boolean tableExists(Table table)
	{
		try
		{
			table.describe();
			return true;
		}
		catch(ResourceNotFoundException e)
		{
			return false;
		}
	}


	/**
	 * Creates a table with a hash and range keys.
	 *
	 * hashType and rangeType must be either KEY_TYPE_STRING or KEY_TYPE_NUMBER
	 * */
	public void createTable(String tableName, String hashName, String hashType, String rangeName, String rangeType, long readCapacity, long writeCapacity)
	{
		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();

		attributeDefinitions.add(new AttributeDefinition()
			.withAttributeName(hashName)
			.withAttributeType(hashType));

		keySchema.add(new KeySchemaElement()
			.withAttributeName(hashName)
			.withKeyType(KeyType.HASH));

		attributeDefinitions.add(new AttributeDefinition()
			.withAttributeName(rangeName)
			.withAttributeType(rangeType));

		keySchema.add(new KeySchemaElement()
			.withAttributeName(rangeName)
			.withKeyType(KeyType.RANGE));

		CreateTableRequest request = new CreateTableRequest()
			.withTableName(tableName)
			.withKeySchema(keySchema)
			.withAttributeDefinitions(attributeDefinitions)
			.withProvisionedThroughput(new ProvisionedThroughput()
				.withReadCapacityUnits(readCapacity)
				.withWriteCapacityUnits(writeCapacity));

		Table table = getDynamoDB().createTable(request);

		try
		{
			table.waitForActive();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}


	public void deleteTable(String tableName)
	{
		Table table = getDynamoDB().getTable(tableName);

		if(tableExists(table))
		{
			table.delete();

			try
			{
				table.waitForDelete();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

}
