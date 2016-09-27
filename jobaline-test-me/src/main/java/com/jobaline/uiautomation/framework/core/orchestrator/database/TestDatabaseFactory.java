package com.jobaline.uiautomation.framework.core.orchestrator.database;

/**
 * Created by damian.j on 9/21/15.
 */
public class TestDatabaseFactory
{
	private static ITestDatabase testDatabase;

	public static String DATABASE_LOCAL_FILES = "1";
	public static String DATABASE_DYNAMODB    = "2";


	public synchronized static ITestDatabase instance()
	{
		if(testDatabase == null)
		{
			String database = System.getProperty("tests.testdatabase", DATABASE_DYNAMODB);
			testDatabase = instance(database);
		}

		return testDatabase;
	}


	/**
	 * This method does not have an access modifier so it can only be accessed from within the package
	 * */
	synchronized static ITestDatabase instance(String database)
	{
		if(database.equals(DATABASE_DYNAMODB))
		{
			testDatabase = new DynamoTestDatabase();
		}
		else if(database.equals(DATABASE_LOCAL_FILES))
		{
			testDatabase = new LocalFilesTestDatabase();
		}
		else
		{
			throw new RuntimeException(String.format("The test database %s is invalid", database));
		}

		return testDatabase;
	}
}
