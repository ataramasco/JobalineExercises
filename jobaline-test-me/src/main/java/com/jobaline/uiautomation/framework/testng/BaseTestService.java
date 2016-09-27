package com.jobaline.uiautomation.framework.testng;

import com.jobaline.uiautomation.framework.selenium.IHasAccessToSelenium;
import com.jobaline.uiautomation.framework.selenium.SeleniumWrapper;

public class BaseTestService implements IHasAccessToSelenium
{

	public SeleniumWrapper getSeleniumClient()
	{
		return SeleniumClientProxy.getSeleniumClient();
	}

}
