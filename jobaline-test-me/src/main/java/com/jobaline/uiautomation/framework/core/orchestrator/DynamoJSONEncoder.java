package com.jobaline.uiautomation.framework.core.orchestrator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by damian on 5/23/2015.
 */
public class DynamoJSONEncoder
{
	private static String EMPTY_STRING_PLACEHOLDER = "[empty_str]";


	public static String encode(String json)
	{
		String encoded = json;

		while(encoded.contains("\"\""))
		{
			encoded = encoded.replace("\"\"", "\"" + EMPTY_STRING_PLACEHOLDER + "\"");
		}

		return encoded;
	}


	public static JSONObject encode(JSONObject json)
	{
		return new JSONObject(encode(json.toString()));
	}


	public static JSONArray encode(JSONArray json)
	{
		return new JSONArray(encode(json.toString()));
	}


	public static String decode(String json)
	{
		String encoded = json;

		while(encoded.contains(EMPTY_STRING_PLACEHOLDER))
		{
			encoded = encoded.replace(EMPTY_STRING_PLACEHOLDER, "");
		}

		return encoded;
	}


	public static JSONObject decode(JSONObject json)
	{
		return new JSONObject(decode(json.toString()));
	}


	public static JSONArray decode(JSONArray json)
	{
		return new JSONArray(decode(json.toString()));
	}
}
