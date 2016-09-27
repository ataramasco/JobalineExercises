package com.jobaline.uiautomation.framework.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Factory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all classes containing tests that are not Selenium tests.
 */
public class BaseGeneralTest extends BaseTest
{
	static final Logger LOGGER = LoggerFactory.getLogger(BaseGeneralTest.class.getName().replace("com.jobaline.uiautomation.", ""));


	@Override public void onRetry()
	{
	}


	public String getTestName(Method method)
	{
		return method.getName();
	}


	@Factory
	public Object[] create() throws IOException
	{
		List<Object> result = new ArrayList<>();

		try
		{
			BaseGeneralTest test = getClass().newInstance();
			result.add(test);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return result.toArray();
	}


	/**
	 * Prints a footer with valuable information about the result.
	 * */
	@AfterMethod(alwaysRun = true)
	public void afterMethod_PrintTestFooter(ITestResult result, Method method, Object[] parameters)
	{
		String text = "Result: " + getTestResultText(result) + ", ";
		text += String.format("test: %s, time: %s", getTestName(method), getTestExecutionTime(result).toString());

		LOGGER.info("");
		LOGGER.info(text);
	}
}
