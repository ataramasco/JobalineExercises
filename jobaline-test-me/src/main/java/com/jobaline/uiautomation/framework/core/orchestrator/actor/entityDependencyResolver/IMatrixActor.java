package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityDependencyResolver;

import com.jobaline.uiautomation.framework.core.orchestrator.MatrixEntry;
import com.jobaline.uiautomation.framework.core.orchestrator.TestIoEntry;

/**
 * Created by damian on 4/23/15.
 */
public interface IMatrixActor
{
	/**
	 * @throws java.lang.RuntimeException if the test does not exist in the database
	 * */
	public MatrixEntry getTestEntryInMatrix(String testId);

	/**
	 * @throws java.lang.RuntimeException if the actor does not exist in the database
	 * */
	public MatrixEntry getActorEntryInMatrix(String actorId);

	public TestIoEntry resolveActorThatCreatesEntity(String entityTypeName);

	public TestIoEntry resolveTestThatCreatesEntity(String entityTypeName);
}
