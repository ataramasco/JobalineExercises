package com.jobaline.uiautomation.framework.testng.retry;

import org.openqa.selenium.remote.UnreachableBrowserException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * I created this class to mark retried tests as skipped. The idea is to consider only the result of the last execution of a retried test.
 *
 * I found that reports mess up when I change the status.
 *
 * I found the cause and fixed it: when I change the status from failure to skipped, the test result is moved to the skipped list but the status keeps as failure!! 
 *
 * What happens removing the fix??
 * -------------------------------
 *
 * TestNG reports are messed up when there are retries and this class mark the first tests executions as skipped.
 *
 * The reports of TestNG are the generated by the next reporters: TestHTMLReporter, XMLReporter, JUnitXMLReporter, TextReporter
 *
 * Imagine that we run testA and first it fails (we mark it as skipped), then in a retry it pass. In the HTML report,  you will see:

 - 2 methods, 1 skipped, 1 passed
 - Failed methods
 testA
 - Passed methods
 testA
 *
 * The summary is ok because the reporter does something like:
 *
 * 		print(allResults.size() + " methods, " +  skippedList.size() + " skipped, " + passedList.size())
 *
 * But then, the reporter iterates skippedList and it contains a result for testA marked as failed!!, so it will show testA under the "Failed methods" title.
 *
 * Fortunately, this can be fixed in onFinish().
 * */
public class RetryNTimesTestListener extends TestListenerAdapter
{
	private boolean isTestPlatformError(Throwable testException)
	{
		return (testException instanceof UnreachableBrowserException) ||
			testException.getMessage() != null && testException.getMessage().contains("SO_TIMEOUT");
	}


	public synchronized void onTestFailure(ITestResult result)
	{
		super.onTestFailure(result);

		RetryNTimes retryAnalyzer = (RetryNTimes)result.getMethod().getRetryAnalyzer();

		// If the test failed because of a test platform issue we will retry 1 time.
		if(retryAnalyzer == null && isTestPlatformError(result.getThrowable()))
		{
			retryAnalyzer = new RetryTwoTimes();
			result.getMethod().setRetryAnalyzer(retryAnalyzer);
		}

		if(retryAnalyzer != null && retryAnalyzer.willRetry(result))
		{
			// As says below, if we set the result as skipped, html reports of TestNG mess up, but html reports of ReportNG are ok.
			// Fortunately, this can be fixed in onFinish().
			result.setStatus(ITestResult.SKIP);

			// Don't execute any of the next 2 because reports might break
			// Reporter.setCurrentTestResult(result); // This will add a new result!
			// Reporter.setCurrentTestResult(null); // This seems to do nothing for us
		}
	}


	public synchronized void onTestSkipped(ITestResult result)
	{
		RetryNTimes retryAnalyzer = (RetryNTimes)result.getMethod().getRetryAnalyzer();

		if(retryAnalyzer != null && retryAnalyzer.willRetry(result))
		{
			// As says below, if we set the result as skipped, html reports of TestNG mess up, but html reports of ReportNG are ok.
			// Fortunately, this can be fixed in onFinish().
			result.setStatus(ITestResult.SKIP);

			// Don't execute any of the next 2 because reports might break
			// Reporter.setCurrentTestResult(result); // This will add a new result!
			// Reporter.setCurrentTestResult(null); // This seems to do nothing for us
		}
	}


	public synchronized void onFinish(ITestContext testContext)
	{
		/* Now we are going to fix the status of tests that appear in the skipped list but are marked as failed!!
		 * See the class comments.
		 * 
		 * Will also check the passed and failed lists for security.
		 * */

		testContext.getPassedTests().getAllResults().stream().filter(result -> result.getStatus() != ITestResult.SUCCESS).forEach(result -> result.setStatus(ITestResult.SUCCESS));

		testContext.getFailedTests().getAllResults().stream().filter(result -> result.getStatus() != ITestResult.FAILURE).forEach(result -> result.setStatus(ITestResult.FAILURE));

		testContext.getSkippedTests().getAllResults().stream().filter(result -> result.getStatus() != ITestResult.SKIP).forEach(result -> result.setStatus(ITestResult.SKIP));
		
		
		/* If you discover that a test that have passed in a retry also appears as failed, you can uncomment the next lines to fix it.
		 * This was happening when onTestFailure() was doing something like this: 
		 * 
		 * 		result.setStatus(ITestResult.SKIP);
		 * 		Reporter.setCurrentTestResult(result);
		 * 
		 * But after removing the second line, it didn't happen again so far.
		 * */

		//		List<String> passedTestsIds= new ArrayList<String>();
		//
		//		Set<ITestResult> passedTests= testContext.getPassedTests().getAllResults();
		//		for(ITestResult result: passedTests)
		//		{
		//			String testId= TestMethodUtils.getMethodIdentifier(result.getMethod().getMethodName(), (BaseTest)result.getInstance());
		//			passedTestsIds.add(testId);
		//		}
		//
		//		Set<ITestResult> failedTests= testContext.getFailedTests().getAllResults();
		//		for(ITestResult result: failedTests)
		//		{
		//			String testId= TestMethodUtils.getMethodIdentifier(result.getMethod().getMethodName(), (BaseTest)result.getInstance());
		//			if(passedTestsIds.contains(testId))
		//			{
		//				testContext.getFailedTests().removeResult(result);
		//			}
		//		}

	}

}
