package com.jobaline.uiautomation.framework.core.ui;

import com.jobaline.uiautomation.framework.selenium.IHasAccessToSelenium;
import com.jobaline.uiautomation.framework.selenium.SeleniumWrapper;
import com.jobaline.uiautomation.framework.testng.SeleniumClientProxy;

/**
 * It is the base class of all the objects following the Page Pattern, representing a page fragment, for example, a header or footer.
 */
public abstract class BasePageFragment implements IHasAccessToSelenium
{
	public BasePage page;


	public BasePageFragment(BasePage page)
	{
		this.page = page;
	}


	protected BasePage getPage()
	{
		return page;
	}


	public SeleniumWrapper getSeleniumClient()
	{
		return SeleniumClientProxy.getSeleniumClient();
	}
}
