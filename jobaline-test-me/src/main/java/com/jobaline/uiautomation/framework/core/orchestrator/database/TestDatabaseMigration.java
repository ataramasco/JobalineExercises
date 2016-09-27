package com.jobaline.uiautomation.framework.core.orchestrator.database;

/**
 * Created by damian.j on 9/21/15.
 */
public class TestDatabaseMigration
{
	/**
	 * This method allows to migrate one database from another.
	 * It should not be used by tests, this is intended to be called in some suite bootstrap process.
	 * */
	public synchronized static void instance(String databaseFrom, String databaseTo)
	{
		ITestDatabase testDatabaseFrom = TestDatabaseFactory.instance(databaseFrom);
		ITestDatabase testDatabaseTo = TestDatabaseFactory.instance(databaseTo);

		testDatabaseTo.migrateFrom(testDatabaseFrom);
	}
}
