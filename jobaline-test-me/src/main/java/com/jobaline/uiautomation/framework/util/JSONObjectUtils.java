package com.jobaline.uiautomation.framework.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by damian on 4/27/2015.
 */
public class JSONObjectUtils
{
	/**
	 * Converts a JSONObject whose all properties are JSONObjects to a Map of JSONObject
	 * */
	public static Map<String, JSONObject> toMapOfJsonObjects(JSONObject jsonObject)
	{
		Map<String, JSONObject> map = new HashMap<>();
		Iterator<String> keys = jsonObject.keys();
		while(keys.hasNext())
		{
			String key = keys.next();
			JSONObject value;
			try
			{
				value = jsonObject.getJSONObject(key);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("The item with key " + key + " is not a json object");
			}

			map.put(key, value);
		}

		return map;
	}


	/**
	 * Converts a JSONObject to a Map of Object
	 * */
	public static Map<String, Object> toMapOfObjects(JSONObject jsonObject)
	{
		Map<String, Object> map = new HashMap<>();
		Iterator<String> keys = jsonObject.keys();
		while(keys.hasNext())
		{
			String key = keys.next();
			Object value;
			try
			{
				value = jsonObject.get(key);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("The item with key " + key + " is not a json object");
			}

			map.put(key, value);
		}

		return map;
	}


	/**
	 * Returns a json object that is the merge of the json objects passed by parameter.
	 * Note that in case of repeated, the value of the rightmost json object will prevail
	 * */
	public static JSONObject merge(JSONObject... jsonObjects)
	{
		JSONObject union = new JSONObject();

		for(int i = 0; i < jsonObjects.length; i++)
		{
			Iterator<String> keys = jsonObjects[i].keys();
			while(keys.hasNext())
			{
				try
				{
					String key = keys.next();
					Object value = jsonObjects[i].get(key);
					union.put(key, value);
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		return union;
	}


	public static <T> List<T> toList(JSONArray jsonArray, Class<T> elementsType)
	{
		List<T> list = new ArrayList<>();

		for(int i = 0; i < jsonArray.length(); i++)
		{
			list.add((T)jsonArray.get(i));
		}

		return list;
	}
}
