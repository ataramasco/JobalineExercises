package com.jobaline.uiautomation.framework.testng.retry;

import com.jobaline.uiautomation.framework.testng.BaseTest;
import com.jobaline.uiautomation.framework.testng.TestMethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.Map;

public abstract class RetryNTimes implements IRetryAnalyzer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RetryNTimes.class.getName().replace("com.jobaline.uiautomation.", ""));

	private Map<String, Integer> count = new HashMap<>();


	public synchronized boolean retry(ITestResult result)
	{
		if(willRetry(result))
		{
			increaseCount(result);

			BaseTest testInstance = (BaseTest)result.getInstance();
			testInstance.onRetry();

			String testId = TestMethodUtils.getMethodIdentifier(result.getMethod().getMethodName(), testInstance);

			LOGGER.warn("The previous test (" + result.getTestClass().getName() + "." + result.getMethod().getMethodName() + "()) has FAILED but will be marked as SKIP and retry. Retry number: " + count.get(testId) + " of " + getMaxCount());
			if(result.getThrowable() != null)
			{
				LOGGER.warn("Fail reason: " + result.getThrowable().getMessage());
			}

			return true;
		}

		return false;
	}


	/**
	 * It seems to be called twice per test:
	 *
	 * 	  - After the test method by our listener.
	 * 	  - After all AfterMethods by TestNG by calling retry(ITestResult result) to see if must retry.
	 *
	 * I implemented this method without increasing the counter.
	 * */
	public synchronized boolean willRetry(ITestResult result)
	{
		int count = getCount(result);

		return !result.isSuccess() && count < getMaxCount();
	}


	public synchronized void initializeCount(ITestResult result)
	{
		String testId = TestMethodUtils.getMethodIdentifier(result.getMethod().getMethodName(), (BaseTest)result.getInstance());
		if(!count.containsKey(testId))
		{
			count.put(testId, 1);
		}
	}


	public synchronized int getCount(ITestResult result)
	{
		initializeCount(result);

		String testId = TestMethodUtils.getMethodIdentifier(result.getMethod().getMethodName(), (BaseTest)result.getInstance());
		return count.get(testId);
	}


	public synchronized void increaseCount(ITestResult result)
	{
		initializeCount(result);

		String testId = TestMethodUtils.getMethodIdentifier(result.getMethod().getMethodName(), (BaseTest)result.getInstance());
		count.put(testId, count.get(testId) + 1);
	}


	public abstract int getMaxCount();

}
