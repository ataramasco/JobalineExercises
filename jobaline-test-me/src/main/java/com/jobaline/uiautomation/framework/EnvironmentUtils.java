package com.jobaline.uiautomation.framework;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jobaline.uiautomation.constants.GridsIds;
import com.jobaline.uiautomation.framework.selenium.BrowserConfiguration;
import com.jobaline.uiautomation.framework.selenium.BrowserConfigurationFactory;

/**
 * Environments Properties retrieved from POM
 */
public class EnvironmentUtils
{
	/**
	 * For simplicity, we can define boolean property values as "0" or "1". Obviously also as "true" or "false".
	 * This method converts these values in the corresponding java Boolean value.
	 * @return the java Boolean value of the property or null if is not defined.
	 * */
	private static Boolean parseBooleanSystemProperty(String name)
	{
		String valueStr = System.getProperty(name, null);

		Boolean value;

		if(valueStr == null)
		{
			value = null;
		}
		else if(valueStr.equals("0"))
		{
			value = false;
		}
		else if(valueStr.equals("1"))
		{
			value = true;
		}
		else
		{
			throw new RuntimeException("The value must be null, \"0\" or \"1\"");
		}

		return value;
	}


	public static String getApplicationUnderTestDomain()
	{
		String autDomain = System.getProperty("tests.autDomain");

		if(autDomain.contains("jobaline.com"))
		{
			if(!EnvironmentUtils.isProdTesting())
			{
				System.out.println("The application under test domain seems to be PROD: " + autDomain + ". Have you pointed the tests against PROD??!! Please, set the parameter to \"CI\", \"Stage\" or other testing environment.");
				System.exit(1);
			}
		}

		return autDomain;
	}


	public static boolean isProdTesting()
	{
		String defaultValue = "false";
		String prodTesting = System.getProperty("tests.prodTesting", defaultValue);
		return prodTesting.equals("true");
	}


	/**
	 * Id of the environment where tests are running.
	 * For example, the tests may be running against stage, ci or dev environments.
	 * */
	public static String getEnvironmentId()
	{
		return getApplicationUnderTestDomain();
	}


	public static String testNames = System.getProperty("tests.testnames");
	
	
	/*
	 * Note that by default, tests will be run in a CI machine, in a Selenium grid, which will be Sauce Labs grid. This is:
	 * 
	 * isTesterMachine ->	false
	 * isCIMachine -> 		true
	 * 
	 * isGrid -> 			true
	 * 
	 * gridId -> 		GridIds.SAUCELABS
	 * 
	 * */

	public static boolean isTesterMachine = parseBooleanSystemProperty("tests.isTesterMachine") != null? parseBooleanSystemProperty("tests.isTesterMachine") : false;

	public static boolean isCIMachine = parseBooleanSystemProperty("tests.isCIMachine") != null? parseBooleanSystemProperty("tests.isCIMachine") : true;

	public static boolean isGrid = parseBooleanSystemProperty("tests.isGrid") != null? parseBooleanSystemProperty("tests.isGrid") : true;


	/**
	 * This method will return the grid id specified in the environment property: tests.gridId.
	 * If you don't define this property, a default value will be read from the pom.xml file. But always the environment property has priority over the pom.xml file.
	 * Browser configurations also define the grid id they belong (See BrowserConfigurationDefinitions class).
	 * If you want that the grid id be obtained from the browser configuration, specify the environment property tests.gridId= "null" (note that this is a string, not the null symbol).
	 * This way, you can define:
	 *
	 * 		tests.gridId= "null"
	 * 		tests.browserConfigurations= SAUCE_MACMOUNTAINLION_SAFARI_6,BS_MACMOUNTAINLION_SAFARI_6_1
	 *
	 * And you will be able to run in the same Jenkins job, tests against Sauce Labs and tests against Browser Stack. In this case you can not specify in tests.browserConfigurations, groups of browsers configurations or another indirectly browsers configurations such as DEFAULT_DESKTOP_BROWSER. 
	 *
	 * But if the grid id is read from the environment property or from the pom file, all the browser configuration must belong to that grid.
	 * */
	public static Integer getGridId()
	{
		String gridId_aux = System.getProperty("tests.gridId");

		if(gridId_aux == null || gridId_aux.equalsIgnoreCase("null"))
		{
			return null;
		}
		else
		{
			return Integer.parseInt(gridId_aux);
		}
	}


	public static String getLocalBrowserBinaryPath()
	{
		return System.getProperty("tests.localBrowserBinaryPath", "/home/ubuntu/phantomjs-1.9.8-linux-x86_64/bin/phantomjs");
	}


	public static String getCasperJSPath()
	{
		return System.getProperty("tests.casperJSPath", "/home/ubuntu/casperjs-1.1-beta3/n1k0-casperjs-4f105a9/bin/casperjs");
	}


	public static String getPhantomJSPath()
	{
		return System.getProperty("tests.phantomJSPath", "/home/ubuntu/phantomjs-1.9.8-linux-x86_64/bin/phantomjs");
	}


	public static String getScreenshotsDirectoryPath()
	{
		return getScreenshotsDirectoryPath(false);
	}


	public static synchronized String getScreenshotsDirectoryPath(boolean separatorAtEnd)
	{
		// I'm not using File interface because File(dir).exists() always returns false when tests run in Jenkins. It must be the combination of Java version and OS.

		String sep = System.getProperty("file.separator");

		String dir = "target" + sep + "screenshots";

		Path path = Paths.get(dir);

		if(!Files.exists(path))
		{
			try
			{
				Files.createDirectory(path);

				long startTime = System.currentTimeMillis();
				long timeout = 30000;
				long wait = 1000;
				while(!Files.exists(path) && System.currentTimeMillis() <= startTime + timeout)
				{
					System.out.println("Waiting for screenshots directory to be created");
					try
					{
						Thread.sleep(wait);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		String pathAsString = path.toAbsolutePath().toString();
		if(separatorAtEnd)
		{
			return pathAsString.endsWith("/")? pathAsString : pathAsString + "/";
		}
		else
		{
			return !pathAsString.endsWith("/")? pathAsString : pathAsString.substring(0, pathAsString.length() - 1);
		}
	}


	/**
	 * If running in a CI server (currently Jenkins), will contain the job name.
	 * */
	public static String ciJobName = System.getProperty("tests.ciJobName");

	/**
	 * If running in a CI server (currently Jenkins), will contain the build number.
	 * */
	public static Integer ciBuildNumber = System.getProperty("tests.ciBuildNumber", null) != null? Integer.parseInt(System.getProperty("tests.ciBuildNumber")) : null;

	/**
	 * If running in a CI server (currently Jenkins), will contain the job name and the build number.
	 * */
	public static String ciJobNameAndBuildNumber = System.getProperty("tests.ciJobNameAndBuildNumber", "Tester");

	/**
	 * How long Selenium commands can delay.
	 * */
	public static String seleniumCommandTimeout = System.getProperty("tests.seleniumCommandTimeout");

	/**
	 * How long Selenium must wait for next command.
	 * */
	public static String seleniumIdleTimeout = System.getProperty("tests.seleniumIdleTimeout");

	/**
	 * Maximum execution time of the Selenium test
	 * */
	public static String seleniumMaxDurationTimeout = System.getProperty("tests.seleniumMaxDurationTimeout");

	private static List<BrowserConfiguration> browserConfigurations;


	public static List<BrowserConfiguration> getBrowserConfigurations()
	{
		if(browserConfigurations == null)
		{
			// I need the String in the next format: ",BC1,BC2," so will remove any whitespace and will add the leading and trailing "," if they are missing.
			// The algorithm in this method needs that all browser configuration be enclosed by two ","
			String browsersConfigurationIds = System.getProperty("tests.browserConfigurations").replaceAll("\\s", "");
			if(browsersConfigurationIds.charAt(0) != ',')
			{
				browsersConfigurationIds = "," + browsersConfigurationIds;
			}
			if(browsersConfigurationIds.charAt(browsersConfigurationIds.length() - 1) != ',')
			{
				browsersConfigurationIds = browsersConfigurationIds + ",";
			}

			// Now check if there is another no directly browser id.
			if(browsersConfigurationIds.contains("," + BrowserConfigurationFactory.DEFAULT_DESKTOP_BROWSER + ","))
			{
				if(getGridId() == null)
				{
					throw new RuntimeException("You must specify the grid id if you want to specify DEFAULT_DESKTOP_BROWSER in tests.browserConfigurations");
				}

				// Replace the group id with the browsers ids. The browsers belonging to a group are defined in the pom.xml
				String propertyWithDefaultBrowser = "tests.";
				if(getGridId() == GridsIds.TESTERGRID)
				{
					propertyWithDefaultBrowser += "TESTERGRID_" + BrowserConfigurationFactory.DEFAULT_DESKTOP_BROWSER;
				}

				browsersConfigurationIds = browsersConfigurationIds.replaceAll(BrowserConfigurationFactory.DEFAULT_DESKTOP_BROWSER, System.getProperty(propertyWithDefaultBrowser));
			}

			// Removing leading and trailing ",". They will be always present because this method will add them if they are not present
			browsersConfigurationIds = browsersConfigurationIds.substring(1, browsersConfigurationIds.length() - 1);

			browserConfigurations = new ArrayList<>();

			String[] browsersConfigurationIdsArray = browsersConfigurationIds.split(",");
			// Comment the line above and uncomment the next line below to enforce all tests to run in some specific browsers 
			// String[] browserConfigurationsKeys= {"SAUCE_WIN8_FIREFOX_25"};

			for(String key : browsersConfigurationIdsArray)
			{
				BrowserConfiguration browserConfiguration = BrowserConfigurationFactory.getBrowserConfiguration(key);
				if(browserConfiguration == null)
				{
					throw new RuntimeException("The browser configuration does not exists: " + key);
				}
				browserConfigurations.add(browserConfiguration);
			}
		}
		return browserConfigurations;
	}


	public static String getDataCreationActorsPackage()
	{
		return "com.jobaline.uiautomation.application.setup";
	}
}
