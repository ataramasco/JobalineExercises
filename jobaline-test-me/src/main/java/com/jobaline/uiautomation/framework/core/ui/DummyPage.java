package com.jobaline.uiautomation.framework.core.ui;

/**
 * This is a dummy page that may be used in cases where we don't know/don't care about the current page, for example, if some widget needs some generic functionality like for example, all the methods inherited from BasePage.
 * */
public class DummyPage extends BasePage
{

	public DummyPage()
	{
		// Do nothing
	}


	@Override protected String getPath()
	{
		throw new RuntimeException("This method shouldn't be called");
	}
}
