package com.jobaline.uiautomation.framework.selenium;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

/**
 * Created by damian on 3/4/15.
 */
public class OWebElement extends RemoteWebElement
{
	private JSONObject elementModel;


	public OWebElement(JSONObject elementModel, String webDriverElementId, RemoteWebDriver parent)
	{
		super();

		setId(webDriverElementId);
		setParent(parent);

		this.elementModel = elementModel;
	}


	public String getTagName()
	{
		return super.getText();
	}


	private JSONObject getAttributes()
	{
		try
		{
			return elementModel.getJSONObject("attributes");
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			throw new RuntimeException("The element model is not valid or it does not contain the attribute variable. Please, fix it.");
		}
	}


	public boolean hasAttribute(String attributeName)
	{
		JSONObject attributes = getAttributes();
		return attributes.has(attributeName);
	}


	public String getAttribute(String attributeName)
	{
		if(hasAttribute(attributeName))
		{
			JSONObject attributes = getAttributes();
			try
			{
				return attributes.getString(attributeName);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("The element model does not contain the attribute " + attributeName + " but the method hasAttribute() returned true. Please, fix it.");
			}
		}
		else
		{
			return null;
		}
	}


	public boolean isSelected()
	{
		return super.isSelected();
	}


	public boolean isEnabled()
	{
		return super.isEnabled();
	}


	public String getText()
	{
		try
		{
			return elementModel.getString("text");
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			throw new RuntimeException("The element model is not valid or it does not contain the text variable. Please, fix it.");
		}
	}
}
