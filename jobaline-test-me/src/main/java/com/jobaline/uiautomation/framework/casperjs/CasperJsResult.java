package com.jobaline.uiautomation.framework.casperjs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by damian on 3/11/15.
 */
public class CasperJsResult
{
	public static final int CODE_SUCCESS      = 0;
	public static final int CODE_ERROR        = 1;
	public static final int CODE_TEST_FAILURE = 2;

	public static final int CODE_ERROR_INVALID_OUTPUT  = 0;
	public static final int CODE_ERROR_PROCESS_CRASHED = 1;
	public static final int CODE_ERROR_OUT_OFMEMORY    = 2;
	public static final int CODE_ERROR_OTHER           = 9999;

	public static final int CODE_TEST_FAILURE_TIMEOUT = 0;
	public static final int CODE_TEST_FAILURE_OTHER   = 9999;

	private int        code;
	private Integer    subCode;
	private String     errorMessage;
	private JSONObject data;


	public CasperJsResult(int code, Integer subCode, String errorMessage, JSONObject data)
	{
		this.code = code;
		this.subCode = subCode;
		this.errorMessage = errorMessage;
		this.data = data;
	}


	public static CasperJsResult parse(String rawResult)
	{
		Integer resultCode = null;
		Integer resultSubCode = null;
		String errorMessage = null;
		JSONObject data = null;

		if(rawResult == null || rawResult.equals(""))
		{
			resultCode = CasperJsResult.CODE_ERROR;
			resultSubCode = CasperJsResult.CODE_ERROR_INVALID_OUTPUT;
			errorMessage = "The CasperJS script did not return a result.";
		}
		else
		{
			JSONObject result_json = null;

			try
			{
				result_json = new JSONObject(rawResult);
			}
			catch(JSONException e)
			{
				e.printStackTrace();

				resultCode = CasperJsResult.CODE_ERROR;
				resultSubCode = CasperJsResult.CODE_ERROR_INVALID_OUTPUT;
				errorMessage = "The CasperJS script executed returned a result but it is not valid json object: " + rawResult;
			}

			if(resultCode == null)
			{
				try
				{
					resultCode = result_json.has("code")? result_json.getInt("code") : null;
					resultSubCode = result_json.has("subCode")? result_json.getInt("subCode") : null;
					errorMessage = result_json.has("errorMessage")? result_json.get("errorMessage").toString() : null;
					data = result_json.has("data")? result_json.getJSONObject("data") : null;
				}
				catch(JSONException e)
				{
					e.printStackTrace();

					resultCode = CasperJsResult.CODE_ERROR;
					resultSubCode = CasperJsResult.CODE_ERROR_INVALID_OUTPUT;
					errorMessage = "The CasperJS script executed returned a result that is a valid object, but the structure is not valid: " + rawResult;
				}
			}

			if(resultCode == null)
			{
				resultCode = CasperJsResult.CODE_ERROR;
				resultSubCode = CasperJsResult.CODE_ERROR_INVALID_OUTPUT;
				errorMessage = errorMessage != null? errorMessage : "The CasperJS script did not return the result code.";
			}
			else if(resultCode.equals(CasperJsResult.CODE_TEST_FAILURE))
			{
				resultSubCode = resultSubCode != null? resultSubCode : CasperJsResult.CODE_TEST_FAILURE_OTHER;
				errorMessage = errorMessage != null? "The CasperJS script reported a failed assert: " + errorMessage : "The CasperJS script reported a failed assert (failure message not available)";
			}
			else if(resultCode.equals(CasperJsResult.CODE_ERROR))
			{
				resultSubCode = resultSubCode != null? resultSubCode : CasperJsResult.CODE_ERROR_OTHER;
				errorMessage = errorMessage != null? "Could not execute the CasperJS script successfully. Error message: " + errorMessage : "Could not execute the CasperJS script successfully (error message not available)";
			}
			else if(resultCode.equals(CasperJsResult.CODE_SUCCESS) && data == null)
			{
				resultCode = CasperJsResult.CODE_ERROR;
				resultSubCode = CasperJsResult.CODE_ERROR_INVALID_OUTPUT;
				errorMessage = "The CasperJS script seems to have run successfully but did not return any data. Verify that the json object returned contains the 'data' property: " + result_json;
			}
		}

		return new CasperJsResult(resultCode, resultSubCode, errorMessage, data);
	}


	public Integer getCode()
	{
		return code;
	}


	public void setCode(Integer code)
	{
		this.code = code;
	}


	public Integer getSubCode()
	{
		return subCode;
	}


	public void setSubCode(Integer subCode)
	{
		this.subCode = subCode;
	}


	public String getErrorMessage()
	{
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}


	public JSONObject getData()
	{
		return data;
	}


	public void setData(JSONObject data)
	{
		this.data = data;
	}
}
