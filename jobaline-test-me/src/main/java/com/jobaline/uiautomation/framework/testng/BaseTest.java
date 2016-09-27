package com.jobaline.uiautomation.framework.testng;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.xml.XmlTest;

import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.lang.DateRange;

/**
 * Base class of core tests classes: BaseSeleniumTests and BaseGeneralTest.
 * You must not inherit from this class a test class with test methods. You must inherit from BaseSeleniumTests or BaseGeneralTest.
 * */
public abstract class BaseTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class.getName().replace("com.jobaline.uiautomation.", ""));

	private static final Object classLock = new Object();


	/**
	 * Each type of test may have specific conventions to name a test. In some cases, the test name may be the class name and in others the method name or both.
	 * */
	protected abstract String getTestName(Method method);


	protected String getTestResultText(ITestResult result)
	{
		if(result.getStatus() == ITestResult.SUCCESS)
		{
			return "SUCCESS";
		}
		else if(result.getStatus() == ITestResult.FAILURE)
		{
			return "FAILED";
		}
		else if(result.getStatus() == ITestResult.SKIP)
		{
			return "SKIP";
		}
		else
		{
			return "" + result.getStatus();
		}
	}


	protected DateRange getTestExecutionTime(ITestResult result)
	{
		DateRange dateRange;
		if(result.getEndMillis() > 0 && result.getStartMillis() > 0 && (result.getEndMillis() >= result.getStartMillis())) // We must assure that TestNG return correct times
		{
			dateRange = new DateRange(result.getStartMillis(), result.getEndMillis());
		}
		else
		{
			LOGGER.warn(String.format("The test start and end times returned by TestNG are not correct. Start %d, end: %d.", result.getStartMillis(), result.getEndMillis()));
			dateRange = new DateRange(0, 0);
		}

		return dateRange;
	}


	@BeforeSuite(alwaysRun = true)
	public void beforeSuite(ITestContext context)
	{
		// Get the tests that will be executed

		String testNames = EnvironmentUtils.testNames;
		if(testNames == null || testNames.isEmpty())
		{
			throw new RuntimeException("The tests names are not specified. Is the parameter 'tests.testnames' set with comma-separated tests names?. The tests names are those defined in test tags in the testng configuration file.");
		}

		String[] testNamesArray = testNames.split(",");

		// Get the tests configured

		List<XmlTest> testsDefinedInTestNGConfigurationFile = context.getSuite().getXmlSuite().getTests();
		List<String> testNamesDefinedInTestNGConfigurationFile = testsDefinedInTestNGConfigurationFile.stream().map(XmlTest::getName).collect(Collectors.toList());

		// Check that the tests that will be executed are configured

		for(String testName : testNamesArray)
		{
			if(!testNamesDefinedInTestNGConfigurationFile.contains(testName))
			{
				System.out.println("The test name " + testName + " was not found in the testng configuration file. Make sure that the testng.xml has a test tag for this method.");
				System.exit(1);
			}
		}
	}


	@AfterSuite(alwaysRun = true)
	public void afterSuite(ITestContext context)
	{
	}


	/**
	 * This method perform basic actions to initialize the test such as variables initialization.
	 * A correct prefix is needed for all BeforeMethod/AfterMethod because TestNG run them by alphabetical order.
	 * */
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod_InitTest(Method method, Object[] parameters)
	{
		synchronized(classLock)
		{
			LOGGER.info("");
			LOGGER.info("");
			LOGGER.info("Starting: " + getTestName(method));
		}
	}


	/**
	 * This method basic actions to end the test but does not close the browser.
	 * A correct prefix is needed for all BeforeMethod/AfterMethod because TestNG run them by alphabetical order.
	 * */
	@AfterMethod(alwaysRun = true)
	public void afterMethod_EndTest(ITestResult result, Method method, Object[] parameters)
	{
	}


	public abstract void onRetry();
}
