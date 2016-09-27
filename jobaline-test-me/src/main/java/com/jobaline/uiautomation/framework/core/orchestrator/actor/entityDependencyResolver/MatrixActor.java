package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver;

import com.jobaline.uiautomation.framework.core.orchestrator.MatrixEntry;
import com.jobaline.uiautomation.framework.core.orchestrator.TestIoEntry;
import com.jobaline.uiautomation.framework.core.orchestrator.database.TestDatabaseFactory;

import java.util.List;

/**
 * Created by damian on 4/23/15.
 */
public class MatrixActor
	implements IMatrixActor
{

	@Override
	public MatrixEntry getTestEntryInMatrix(String testId)
	{
		MatrixEntry entry = TestDatabaseFactory.instance().getTestDefinition(testId);

		if(entry == null)
		{
			throw new RuntimeException(String.format("The test '%s' is not defined in the matrix.", testId));
		}

		return entry;
	}


	@Override
	public MatrixEntry getActorEntryInMatrix(String actorId)
	{
		MatrixEntry entry = TestDatabaseFactory.instance().getActorDefinition(actorId);

		if(entry == null)
		{
			throw new RuntimeException(String.format("The actor '%s' is not defined in the matrix.", actorId));
		}

		return entry;
	}


	@Override
	public TestIoEntry resolveActorThatCreatesEntity(String entityTypeName)
	{
		List<MatrixEntry> actorsEntries = TestDatabaseFactory.instance().getActorsDefinitions();

		TestIoEntry testIoEntry = null;
		for(MatrixEntry actorEntry : actorsEntries)
		{
			testIoEntry = actorEntry.getTestIoEntryThatCreatesEntity(entityTypeName);
			if(testIoEntry != null)
			{
				break;
			}
		}

		return testIoEntry;
	}


	@Override
	public TestIoEntry resolveTestThatCreatesEntity(String entityTypeName)
	{
		List<MatrixEntry> testsEntries = TestDatabaseFactory.instance().getTestsDefinitions();

		TestIoEntry testIoEntry = null;
		for(MatrixEntry testEntry : testsEntries)
		{
			testIoEntry = testEntry.getTestIoEntryThatCreatesEntity(entityTypeName);
			if(testIoEntry != null)
			{
				break;
			}
		}

		return testIoEntry;
	}

}
