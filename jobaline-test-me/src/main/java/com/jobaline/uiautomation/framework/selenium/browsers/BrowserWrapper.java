package com.jobaline.uiautomation.framework.selenium.browsers;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

/**
 * Class BrowserWrapper.
 * User: damian
 * Date: 27/09/13 1:03
 *
 * This class and their subclasses must be used only from SeleniumWrapper. The idea behind these classes was remove the browser-dependent code
 * from SeleniumWrapper.
 * Also, we can put here code related to manipulate the browser but not the page, for example, maximize or close the browser.
 */
public abstract class BrowserWrapper
{
	protected DesiredCapabilities capabilities;


	/**
	 * Returns an object that wraps the browser passed by parameter. The possible values of the parameters are those allowed by DesiredCapabilities.
	 * See: https://code.google.com/p/selenium/wiki/DesiredCapabilities
	 * Anyways this method must be called only by SeleniumWrapper.setBrowserConfiguration().
	 * */
	public static BrowserWrapper getBrowserWrapper(Map<String, Object> capabilities)
	{
		// The different Selenium saas providers that we support use different names for this capability
		// Browserstack uses "browserName" and "browser"
		// Saucelabs uses "browserName" and "deviceName"
		String browserName = capabilities.containsKey(BROWSER_NAME)?
			(String)capabilities.get(BROWSER_NAME) :
			capabilities.containsKey("browser")?
				(String)capabilities.get("browser") :
				(String)capabilities.get("deviceName");

		if(browserName == null)
		{
			throw new RuntimeException("The browser configuration must define the browser name");
		}

		BrowserWrapper browserWrapper;

		if(browserName.equalsIgnoreCase("android") || browserName.equalsIgnoreCase("android emulator"))
		{
			browserWrapper = new AndroidWrapper();
		}
		else if(browserName.equalsIgnoreCase("chrome"))
		{
			browserWrapper = new ChromeWrapper();
		}
		else if(browserName.equalsIgnoreCase("firefox"))
		{
			browserWrapper = new FirefoxWrapper();
		}
		else if(browserName.equalsIgnoreCase("phantomjs"))
		{
			browserWrapper = new PhantomJSWrapper();
		}
		else if(browserName.equalsIgnoreCase("internet explorer") || browserName.equalsIgnoreCase("ie"))
		{
			browserWrapper = new InternetExplorerWrapper();
		}
		else if(browserName.equalsIgnoreCase("iPhone") || browserName.equalsIgnoreCase("iphone simulator"))
		{
			browserWrapper = new IPhoneWrapper();
		}
		else if(browserName.equalsIgnoreCase("iPad") || browserName.equalsIgnoreCase("ipad simulator"))
		{
			browserWrapper = new IPadWrapper();
		}
		else if(browserName.equalsIgnoreCase("opera"))
		{
			browserWrapper = new OperaWrapper();
		}
		else if(browserName.equalsIgnoreCase("safari"))
		{
			browserWrapper = new SafariWrapper();
		}
		else
			throw new RuntimeException("The browser " + browserName + " is not supported.");

		browserWrapper.createCapabilities();

		for(Map.Entry<String, Object> entry : capabilities.entrySet())
		{
			browserWrapper.capabilities.setCapability(entry.getKey(), entry.getValue());
		}

		return browserWrapper;
	}


	/**
	 * Create the capabilities.
	 * Must be overridden by subclasses.
	 * This method is one of three methods that configure capabilities. They are called by getBrowserWrapper() in the next order:
	 *	  createCapabilities()
	 *	  configureCapabilities()
	 * */
	protected abstract void createCapabilities();


	public DesiredCapabilities getCapabilities()
	{
		return capabilities;
	}

}
