package com.jobaline.uiautomation.framework.core.orchestrator.actor.entityCreation;

import com.jobaline.uiautomation.framework.casperjs.CasperJS;
import com.jobaline.uiautomation.framework.casperjs.CasperJsResult;
import com.jobaline.uiautomation.framework.lang.Pause;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Created by damian.j on 1/20/16.
 */
public abstract class CasperJsEntityCreationActor extends EntityCreationActor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreationActor.class.getName().replace("com.jobaline.uiautomation.", ""));


	protected abstract String getScriptPath();

	protected abstract JSONObject getScriptArguments(JSONObject data);

	protected abstract JSONObject processScriptResultAndBuildEntityData(JSONObject data, JSONObject scriptResult);


	/**
	 * Override this method to create the entity using other tool rather than CasperJS
	 * */
	protected JSONObject createInternal(Supplier<JSONObject> dataProvider) throws CouldNotCreateEntityException
	{
		return createUsingCasperJS(dataProvider);
	}


	private JSONObject createUsingCasperJS(Supplier<JSONObject> dataProvider) throws CouldNotCreateEntityException
	{
		JSONObject data = dataProvider.get();

		CasperJsResult result = new CasperJS(true).executeScript(getScriptPath(), getScriptArguments(data), getTryTimeout());

		JSONObject entityData;

		if(result.getCode() == CasperJsResult.CODE_SUCCESS)
		{
			entityData = processScriptResultAndBuildEntityData(data, result.getData());
		}
		else if(result.getCode() == CasperJsResult.CODE_TEST_FAILURE)
		{
			LOGGER.error("Could not create the entity due to a CasperJS assertion failure: " + result.getErrorMessage());

			throw new CouldNotCreateEntityException(result.getErrorMessage());
		}
		else if(result.getCode() == CasperJsResult.CODE_ERROR)
		{
			if(result.getSubCode().compareTo(CasperJsResult.CODE_ERROR_OUT_OFMEMORY) == 0 ||
				result.getSubCode().compareTo(CasperJsResult.CODE_ERROR_PROCESS_CRASHED) == 0)
			{
				LOGGER.error("Could not create the entity due to lack of memory or a casperJS crash.");
				Pause.tenSeconds(); // Wait a while before trying again

				throw new CouldNotCreateEntityException("Could not create the entity due to lack of memory or a casperJS crash.");
			}
			else
			{
				throw new CouldNotCreateEntityException("There was a fatal error when tried to create the entity using CasperJS. Error message. " + result.getErrorMessage());
			}
		}
		else
		{
			throw new CouldNotCreateEntityException("The CasperJS script did not return a valid code.");
		}

		return entityData;
	}
}
