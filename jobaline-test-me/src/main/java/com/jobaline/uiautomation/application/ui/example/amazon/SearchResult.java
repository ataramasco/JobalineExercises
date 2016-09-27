package com.jobaline.uiautomation.application.ui.example.amazon;

import com.jobaline.uiautomation.framework.core.ui.BasePage;
import com.jobaline.uiautomation.framework.core.ui.BasePageFragment;
import org.json.JSONObject;

/**
 * Created by damian.j on 3/14/16.
 */
public class SearchResult extends BasePageFragment
{
	private JSONObject data;


	public SearchResult(BasePage page, JSONObject data)
	{
		super(page);

		this.data = data;
	}


	public boolean hasTitle()
	{
		return data.has("title");
	}


	public String getTitle()
	{
		return data.getString("title");
	}


	public boolean hasPrice()
	{
		return data.has("price");
	}


	public String getPrice()
	{
		return data.getString("price");
	}

}
