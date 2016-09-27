package com.jobaline.uiautomation.framework.selenium;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;

import com.jobaline.uiautomation.constants.GridsIds;
import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.lang.ListUtils;
import com.jobaline.uiautomation.framework.lang.MapUtils;

public class BrowserConfigurationFactory
{
	private static final long MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS = 1024l * 5000l;
	private static final long MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_PHANTOMJS = 1024l * 10000l;

	/**
	 * Tester local browsers
	 * */

	// Must use PhantomJS 1.98 or higher
	// PhantomJS 1.97 was not recognizing some Javascript functions that
	// Jobaline uses, for example: __doPostBack(). I think there is some name collision.
	public static final String TESTERLOCAL_PHANTOMJS = "TESTERLOCAL_PHANTOMJS";

	/**
	 * Browsers of a grid built by the tester to run tests while he develop them.
	 * The creation of the grid and the availability of the browsers is responsibility of the tester.
	 * */

	public static final String TESTERGRID_WINDOWS_FIREFOX = "TESTERGRID_WINDOWS_FIREFOX";
	public static final String TESTERGRID_WINDOWS_CHROME = "TESTERGRID_WINDOWS_CHROME";
	public static final String TESTERGRID_WINDOWS_IE = "TESTERGRID_WINDOWS_IE";
	public static final String TESTERGRID_WINDOWS_IE_8 = "TESTERGRID_WINDOWS_IE_8";
	public static final String TESTERGRID_WINDOWS_SAFARI = "TESTERGRID_WINDOWS_SAFARI";
	public static final String TESTERGRID_MAC_SAFARI = "TESTERGRID_MAC_SAFARI";

	/**
	 * The next id just helps to say in the config: I want the tests running in 1 browser selected by default.
	 * This class does not say which browser to select, that is defined in the pom.xml and processed in the EnvironmentUtils class.
	 * */
	public static final String DEFAULT_DESKTOP_BROWSER = "DEFAULT_DESKTOP_BROWSER";

	private static Map<String, BrowserConfiguration> browserConfigurations;


	private static synchronized Map<String, BrowserConfiguration> getBrowserConfigurations()
	{
		if(browserConfigurations == null)
		{
			browserConfigurations = new HashMap<>();

			browserConfigurations.put(TESTERLOCAL_PHANTOMJS, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"browserName", "phantomjs",
							"takesScreenshot", true,
							"phantomjs.cli.args", ListUtils.createList("--web-security=no", "--ignore-ssl-errors=yes"),
							PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, EnvironmentUtils.getLocalBrowserBinaryPath()
					),
					MapUtils.createStringObjectMap(
							"id", TESTERLOCAL_PHANTOMJS,
							"browserName", BrowserConfiguration.BROWSER_NAME_PHANTOMJS,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", false,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", true,
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_PHANTOMJS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", null
					)
			));

			browserConfigurations.put(TESTERGRID_WINDOWS_FIREFOX, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"platform", "WINDOWS",
							"browserName", "firefox",
							"takesScreenshot", true
					),
					MapUtils.createStringObjectMap(
							"id", TESTERGRID_WINDOWS_FIREFOX,
							"browserName", BrowserConfiguration.BROWSER_NAME_FIREFOX,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", true,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", true,
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", GridsIds.TESTERGRID
					)
			));

			browserConfigurations.put(TESTERGRID_WINDOWS_CHROME, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"platform", "WINDOWS",
							"browserName", "chrome",
							"takesScreenshot", true
					),
					MapUtils.createStringObjectMap(
							"id", TESTERGRID_WINDOWS_CHROME,
							"browserName", BrowserConfiguration.BROWSER_NAME_CHROME,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", true,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", true,
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", GridsIds.TESTERGRID
					)
			));

			browserConfigurations.put(TESTERGRID_WINDOWS_IE, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"platform", "WINDOWS",
							"browserName", "internet explorer",
							CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true,
							"ignoreZoomSetting", "true",
							"ignoreProtectedModeSettings", "true",
							"takesScreenshot", true
					),
					MapUtils.createStringObjectMap(
							"id", TESTERGRID_WINDOWS_IE,
							"browserName", BrowserConfiguration.BROWSER_NAME_IE,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", true,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", true,
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", GridsIds.TESTERGRID
					)
			));

			browserConfigurations.put(TESTERGRID_WINDOWS_IE_8, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"platform", "WINDOWS",
							"browserName", "internet explorer",
							"browserVersion", "8",
							CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true,
							"ignoreZoomSetting", "true",
							"ignoreProtectedModeSettings", "true",
							"takesScreenshot", true
					),
					MapUtils.createStringObjectMap(
							"id", TESTERGRID_WINDOWS_IE_8,
							"browserName", BrowserConfiguration.BROWSER_NAME_IE,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", true,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", true,
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", GridsIds.TESTERGRID
					)
			));

			browserConfigurations.put(TESTERGRID_WINDOWS_SAFARI, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"platform", "WINDOWS",
							"browserName", "safari",
							"takesScreenshot", false
					),
					MapUtils.createStringObjectMap(
							"id", TESTERGRID_WINDOWS_SAFARI,
							"browserName", BrowserConfiguration.BROWSER_NAME_SAFARI,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", true,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", false, // false by default, this case was not tested
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", GridsIds.TESTERGRID
					)
			));

			browserConfigurations.put(TESTERGRID_MAC_SAFARI, new BrowserConfiguration(
					MapUtils.createStringObjectMap(
							"platform", "MAC",
							"browserName", "safari",
							"takesScreenshot", false
					),
					MapUtils.createStringObjectMap(
							"id", TESTERGRID_MAC_SAFARI,
							"browserName", BrowserConfiguration.BROWSER_NAME_SAFARI,
							"supportsCookies", true,
							"supportsHttps", true,
							"supportsWindowSize", true,
							"supportsMaximize", true,
							"hasProblemsWithBrowserLocation", false,
							"supportsInHouseScreenshots", false, // false by default, this case was not tested
							"maximumPageSizeAllowedForScreenshot", MAXIMUM_PAGE_SIZE_ALLOWED_FOR_SCREENSHOTS_GRID_BROWSERS,
							"layout", BrowserConfiguration.LAYOUT_DESKTOP,
							"gridId", GridsIds.TESTERGRID
					)
			));
		}

		return browserConfigurations;
	}


	public static BrowserConfiguration getBrowserConfiguration(String id)
	{
		return getBrowserConfigurations().get(id);
	}

}
