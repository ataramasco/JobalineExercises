package com.jobaline.uiautomation.framework.selenium.browsers;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Class AndroidWrapper.
 * User: damian
 * Date: 27/09/13 1:02
 * To change this template use File | Settings | File Templates.
 */
public class AndroidWrapper extends BrowserWrapper
{

    public void createCapabilities()
    {
        this.capabilities= DesiredCapabilities.android();
    }
    
    
    /**
	 * Maximize the browser. Not supported in android.
	 */
	public void maximize(){}
}
