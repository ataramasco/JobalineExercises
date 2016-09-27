package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation;

import org.json.JSONObject;
import scala.concurrent.Future;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Created by damian on 4/16/15.
 */
public interface IEntityCreationActor
{
	public String getEntityName();

	public int getDefaultNumberOfTries();

	public long getDefaultTryTimeout();

	public void setNumberOfTries(int tries);

	public void setTryTimeout(long tries);

	public Future<JSONObject> create(Supplier<JSONObject> dataProvider) throws TimeoutException, CouldNotCreateEntityException;

	public JSONObject createBlocking(Supplier<JSONObject> dataProvider) throws TimeoutException, CouldNotCreateEntityException;
}
