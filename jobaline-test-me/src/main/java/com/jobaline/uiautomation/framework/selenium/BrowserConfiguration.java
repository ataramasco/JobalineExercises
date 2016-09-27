package com.jobaline.uiautomation.framework.selenium;

import java.util.Map;

/**
 * Class BrowserConfiguration.
 * User: damian
 * Date: 7/10/13 16:31
 */
public class BrowserConfiguration
{
	public static final String BROWSER_NAME_IE      = "ie";
	public static final String BROWSER_NAME_CHROME  = "chrome";
	public static final String BROWSER_NAME_FIREFOX = "firefox";
	public static final String BROWSER_NAME_SAFARI  = "safari";

	public static final String BROWSER_NAME_PHANTOMJS = "phantomjs";

	public static final String BROWSER_NAME_ANDROID = "android";
	public static final String BROWSER_NAME_IPHONE  = "iPhone";
	public static final String BROWSER_NAME_IPAD    = "iPad";

	public static final String BROWSER_VERSION_IE_8  = "8";
	public static final String BROWSER_VERSION_IE_9  = "9";
	public static final String BROWSER_VERSION_IE_10 = "10";
	public static final String BROWSER_VERSION_IE_11 = "11";

	public static final Integer LAYOUT_PHONE   = 0;
	public static final Integer LAYOUT_TABLET  = 1;
	public static final Integer LAYOUT_DESKTOP = 2;

	private Map<String, Object> capabilities;
	private Map<String, Object> properties;


	public BrowserConfiguration(Map<String, Object> capabilities, Map<String, Object> properties)
	{
		this.capabilities = capabilities;
		this.properties = properties;
	}


	public void setCapabilities(Map<String, Object> capabilities)
	{
		this.capabilities = capabilities;
	}


	public String getId()
	{
		return (String)properties.get("id");
	}


	public Integer getLayout()
	{
		return (Integer)properties.get("layout");
	}


	public Integer getGridId()
	{
		return (Integer)properties.get("gridId");
	}


	public String getBrowserName()
	{
		return (String)properties.get("browserName");
	}


	public String getBrowserVersion()
	{
		return (String)properties.get("browserVersion");
	}


	public boolean isPhone()
	{
		return getLayout().equals(LAYOUT_PHONE);
	}


	public boolean isTablet()
	{
		return getLayout().equals(LAYOUT_TABLET);
	}


	public boolean isDesktop()
	{
		return getLayout().equals(LAYOUT_DESKTOP);
	}


	public Map<String, Object> getCapabilities()
	{
		return capabilities;
	}


	public boolean supportsCookies()
	{
		return (Boolean)properties.get("supportsCookies");
	}


	public boolean supportsWindowSize()
	{
		return (Boolean)properties.get("supportsWindowSize");
	}


	public boolean supportsMaximize()
	{
		return (Boolean)properties.get("supportsMaximize");
	}


	public boolean supportsHttps()
	{
		return (Boolean)properties.get("supportsHttps");
	}


	/**
	 * Some iPhone emulators from Browserstack crash when tests try to open a url or get the current url. This is not happening all the times but is happening very often.
	 * With some particular urls this happens more often, for example, with the url of the search page: jobs.jobalineci.com/Search.
	 * I tested these actions manually in these emulators many times and they have no problem, so the issue seems to be when the actions are performed from Selenium.
	 */
	public boolean hasProblemsWithBrowserLocation()
	{
		return (Boolean)properties.get("hasProblemsWithBrowserLocation");
	}


	public boolean supportsInHouseScreenshots()
	{
		return (Boolean)properties.get("supportsInHouseScreenshots");
	}


	public long getMaximumScreenshotPageSize()
	{
		return (Long)properties.get("maximumPageSizeAllowedForScreenshot");
	}


	public boolean isIE8()
	{
		return getBrowserName().equals(BROWSER_NAME_IE) && getBrowserVersion().equals(BROWSER_VERSION_IE_8);
	}


	public boolean isIE9()
	{
		return getBrowserName().equals(BROWSER_NAME_IE) && getBrowserVersion().equals(BROWSER_VERSION_IE_9);
	}


	public boolean isPhantomJs()
	{
		return getBrowserName().equals(BROWSER_NAME_PHANTOMJS);
	}
}
