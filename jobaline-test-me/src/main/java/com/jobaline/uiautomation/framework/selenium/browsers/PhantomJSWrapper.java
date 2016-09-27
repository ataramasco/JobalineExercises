package com.jobaline.uiautomation.framework.selenium.browsers;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Class HtmlUnitWrapper.
 * User: damian
 * Date: 27/09/13 1:02
 * To change this template use File | Settings | File Templates.
 */
public class PhantomJSWrapper extends BrowserWrapper
{

    public void createCapabilities()
    {
        capabilities = DesiredCapabilities.phantomjs();
    }
    
}
