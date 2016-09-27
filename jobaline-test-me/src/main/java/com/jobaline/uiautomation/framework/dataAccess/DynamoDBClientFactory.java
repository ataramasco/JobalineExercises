package com.jobaline.uiautomation.framework.dataAccess;

import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by damian on 8/16/2015.
 *
 * Right now is not very useful to have factory because we can only connect to one account that is specified by system properties. But we should make this
 * more flexible and also add unit tests.
 */
public class DynamoDBClientFactory
{
	private static DynamoDBClient client;


	public static synchronized DynamoDBClient getClient()
	{
		if(client == null)
		{
			client = new DynamoDBClient(new AmazonDynamoDBClient(new SystemPropertiesCredentialsProvider()));
		}

		return client;
	}

}
