package com.jobaline.uiautomation.framework.selenium.browsers;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Class ChromeWrapper.
 * User: damian
 * Date: 27/09/13 0:59
 * To change this template use File | Settings | File Templates.
 */
public class ChromeWrapper extends BrowserWrapper
{
    public void createCapabilities()
    {
        this.capabilities= DesiredCapabilities.chrome();
    }
}
