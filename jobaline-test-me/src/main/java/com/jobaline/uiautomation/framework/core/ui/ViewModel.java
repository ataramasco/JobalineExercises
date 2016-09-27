package com.jobaline.uiautomation.framework.core.ui;

import org.json.JSONObject;

/**
 * Created by damian.j on 11/11/15.
 */
public class ViewModel
{
	private JSONObject data;


	public ViewModel(JSONObject data)
	{
		this.data = data;
	}


	protected JSONObject getData()
	{
		return data;
	}


	public JSONObject toJSONObject()
	{
		return getData();
	}
}
