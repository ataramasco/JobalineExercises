package com.jobaline.uiautomation.framework.selenium.browsers;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Class IPadWrapper.
 * User: damian
 * Date: 27/09/13 1:02
 * To change this template use File | Settings | File Templates.
 */
public class IPadWrapper extends BrowserWrapper
{
	public void createCapabilities()
	{
		capabilities= DesiredCapabilities.ipad();
	}
}
