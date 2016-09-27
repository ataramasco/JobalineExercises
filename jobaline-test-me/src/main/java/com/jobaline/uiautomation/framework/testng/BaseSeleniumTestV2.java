package com.jobaline.uiautomation.framework.testng;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONObject;
import org.openqa.selenium.UnsupportedCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.jobaline.uiautomation.constants.GridsIds;
import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.core.orchestrator.MatrixEntry;
import com.jobaline.uiautomation.framework.core.orchestrator.TestIoEntry;
import com.jobaline.uiautomation.framework.core.orchestrator.TestSetup;
import com.jobaline.uiautomation.framework.core.orchestrator.actor.OrchestratorActors;
import com.jobaline.uiautomation.framework.lang.Pause;
import com.jobaline.uiautomation.framework.selenium.BrowserConfiguration;
import com.jobaline.uiautomation.framework.selenium.CouldNotInitBrowserException;
import com.jobaline.uiautomation.framework.selenium.SeleniumWrapper;
import com.jobaline.uiautomation.framework.testng.annotations.BrowserDirectives;

/**
 * Base class of core tests classes: BaseSeleniumTests and BaseGeneralTest.
 * You must not inherit from this class a test class with test methods. You must inherit from BaseSeleniumTests or BaseGeneralTest.
 * */
public abstract class BaseSeleniumTestV2 extends BaseTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseSeleniumTestV2.class.getName().replace("com.jobaline.uiautomation.", ""));

	private static final Object classLock = new Object();

	private static Map<Long, BaseSeleniumTestV2> testInstances = new HashMap<>();

	/**
	 * Lock for @variable testInstances
	 * */
	private static final Object testInstancesLock = new Object();

	private BrowserConfiguration browserConfiguration;

	private TestIoEntry testIoSpec;

	private JSONObject testData;

	private String seleniumSessionLogsUrl;
	private String seleniumSessionName;

	private SeleniumWrapper seleniumClient;

	private int testStepsCount = 0;


	private boolean isFirstTestStep()
	{
		return testStepsCount == 1;
	}


	private void increaseTestStepsCount()
	{
		testStepsCount++;
	}


	@Override public void onRetry()
	{
		testData = null;
		testStepsCount = 0;
	}


	public String getTestName(Method method)
	{
		return getClass().getSimpleName() + "." + method.getName();
	}


	public static BaseSeleniumTestV2 getInstance()
	{
		synchronized(testInstancesLock)
		{
			return testInstances.get(Thread.currentThread().getId());
		}
	}


	private static void associateInstanceToThread(Long threadId, BaseSeleniumTestV2 testInstance)
	{
		synchronized(testInstancesLock)
		{
			// After association the thread with the instance, the following code was disassociating any other thread with the instance. But by doing this,
			// the code is wrongfully assuming that there are not concurrent threads running methods of the same instance, which is the case most of the time.
			// The disassociation should be done in some After Method, but can't do that because the listeners will execute after them.
			// Leaving the association after the test method ends shouldn't break anything:
			// 	 - if the thread is not used, that entry will be ignored
			//	 - if the thread is used for other test, the entry will be overwritten.
			/*
			Long[] keys = testInstances.keySet().toArray(new Long[0]);
			for(Long threadId_aux : keys)
			{
				BaseSeleniumTest testInstance_aux = testInstances.get(threadId_aux);
				if(testInstance_aux.equals(testInstance))
				{
					testInstances.remove(threadId_aux);
				}
			}
			*/

			testInstances.put(threadId, testInstance);
		}
	}


	public BrowserConfiguration getBrowserConfiguration()
	{
		return browserConfiguration;
	}


	protected void setBrowserConfiguration(BrowserConfiguration browserConfiguration)
	{
		this.browserConfiguration = browserConfiguration;
	}


	protected TestIoEntry getTestIoSpec()
	{
		return testIoSpec;
	}


	protected void setTestIoSpec(TestIoEntry testIoSpec)
	{
		this.testIoSpec = testIoSpec;
	}


	protected SeleniumWrapper getSeleniumClient()
	{
		return seleniumClient;
	}


	protected void setSeleniumClient(SeleniumWrapper client)
	{
		seleniumClient = client;
	}


	protected String getSeleniumSessionName()
	{
		return seleniumSessionName;
	}


	protected void setSeleniumSessionName(String name)
	{
		seleniumSessionName = name;
	}


	protected void setSeleniumSessionLogsUrl(String url)
	{
		seleniumSessionLogsUrl = url;
	}


	protected String getSeleniumSessionLogsUrl()
	{
		return seleniumSessionLogsUrl;
	}


	protected abstract String getTestId();

	protected abstract void setData(JSONObject data);


	protected SeleniumWrapper initBrowser()
	{
		SeleniumWrapper seleniumClient;

		boolean isTesterMachine = EnvironmentUtils.isTesterMachine;
		boolean isCIMachine = EnvironmentUtils.isCIMachine;
		boolean isGrid = EnvironmentUtils.isGrid;

		Integer gridId = null;
		String gridUrl = null;
		if(isGrid)
		{
			if(EnvironmentUtils.getGridId() != null)
			{
				gridId = EnvironmentUtils.getGridId();
				if(!gridId.equals(getBrowserConfiguration().getGridId()))
				{
					throw new RuntimeException(String.format("The browser configuration %s does not belong to the grid %s", getBrowserConfiguration().getId(), gridId));
				}
			}
			else
			{
				gridId = getBrowserConfiguration().getGridId();
			}

			gridUrl = gridId != null? GridsIds.getGridUrl(gridId) : null;
		}

		seleniumClient = new SeleniumWrapper(getBrowserConfiguration(), isTesterMachine, isCIMachine, isGrid, gridId, gridUrl, getSeleniumSessionName());

		// We try 5 times to init the browser. We will try to recover from recoverable exceptions. See the catch sections for recoverable exceptions
		// we had faced with.
		int tries = 5;

		// If the exception is that we have all the parallels VMs of Browserstack are busy, we will try more than "tries".
		// Sauce Labs don't throw exception for this, it queues the tests.
		int vmsBusyTries = 30;

		// If the exception is that all real devices of Browserstack are busy, we will try more than "tries".
		// With 30 tries and a wait time of 30 seconds per try, I was able to run 20 parallel tests in "iPad Air" without any fail (in 06/17/2014). And the test ran was the longest one:
		// ApplyViaWebTest.testHappyPathNewVerifiableUser()
		int bsRealDevicesBusyTries = 30;

		// If there is a exception, with this variable we can set if we wanna print all the stacktrace or just the message.
		boolean printJustExceptionMessage = false;

		Exception exception = null;

		for(int currentTry = 0; currentTry < tries; currentTry++)
		{
			int waitTime = 0;
			boolean abort = false;

			try
			{
				seleniumClient.initBrowser(getBrowserConfiguration().getCapabilities());

				// If there weren't exceptions, we suppose that the browser is initialized so break the for.
				break;
			}
			catch(org.openqa.selenium.remote.UnreachableBrowserException e)
			{
				/* This is an exception that is being thrown when there is a failure in the connection.
				 * We need to wait some time to try again.
				 */
				waitTime = 20;
				exception = e;
				printJustExceptionMessage = false;
			}
			catch(UnsupportedCommandException e)
			{
				if(e.getMessage().contains("is not in progress. It may have recently finished, or experienced an error"))
				{
					/* This is an exception that Sauce Labs is throwing sometimes when it can not start the browser.
					 * The class of the Exception and the message don't seem a startup issue, these exceptions shouldn't occur in the first command: init browser. But maybe
					 * the Selenium executes several commands internally.
					 * It just failed to init the browser, id does not depends on time, there's no need to wait some time to try again.
					 */
				}
				exception = e;
				printJustExceptionMessage = false;
			}
			catch(org.openqa.selenium.WebDriverException e)
			{
				if(e.getMessage().contains("Failed to connect to SafariDriver after "))
				{
					/* This exception is happening sometimes in the tester grid but may also happen in JSG if we add Safari to it
					 */
					waitTime = 0;
					printJustExceptionMessage = true;
				}
				else if(e.getMessage().contains("Unable to bind to locking port 7054"))
				{
					/* This exceptions seems to happen when a previous Firefox browser crashed and a port kept blocked. The port should be released
					 * after waiting some time.
					 * Other case could be when the test crashed and it didn't close the browser. A timeout will close the browser an release the port.
					 */
					waitTime = 30;
					printJustExceptionMessage = false;
				}
				else if(e.getMessage().contains("502 Bad Gateway"))
				{
					/* This is an exception that Browserstack is throwing sometimes when it start the VM but failed to open the browser. If you see the screenshots when it happens,
					 * you will see an empty Desktop.
					 * It just failed to init the browser, id does not depends on time, there's no need to wait some time to try again.
					 */
					printJustExceptionMessage = false;
				}
				else if(e.getMessage().contains("Could not start Browser / Emulator"))
				{
					/* This is an exception that Browserstack is throwing sometimes that fails to start the the browser.
					 * It just failed to init the browser, id does not depends on time, there's no need to wait some time to try again.
					 */
					printJustExceptionMessage = true;
				}
				else if(e.getMessage().contains("Please upgrade to add more parallel sessions"))
				{
					/* This is an exception that Browserstack is throwing when we try to open more sessions that the available.
					 * It have happened having 5 parallel sessions and running just 5 jobs running in Jenkins, so it is sometimes a Browserstack issue. It seems that
					 * after a session has finished, Browserstack took a while to set it as finished.
					 * We need to wait some time to try again because it depends on time.
					 * We will also try more for these exceptions.
					 */

					// I won't increase until vmsBusyTries reaches 0
					vmsBusyTries--;
					if(vmsBusyTries > 0)
					{
						currentTry--;
					}

					waitTime = 30;
					printJustExceptionMessage = true;
				}
				else if(e.getMessage().contains("There was an error. Please try again."))
				{
					/* This is an exception that Browserstack is throwing when it fails to open a session using a real device. I think
					 * that there aren't real devices available.
					 * We need to wait some time to try again because it depends on time.
					 * We will also try more for these exceptions.
					 */

					// I won't increase until vmsBusyTries reaches 0
					bsRealDevicesBusyTries--;
					if(bsRealDevicesBusyTries > 0)
					{
						currentTry--;
					}

					waitTime = 40;
					printJustExceptionMessage = true;
				}
				else if(e.getMessage().contains("Sauce could not start your job"))
				{
					/* This is an exception that Sauce Labs is throwing when it crashes while it is starting the browser. It's happening randomly
					 * and with a low probability (I would say a 1%) but with a thousand of tests in Suite 2 it is very likely to happen at least once.
					 * We'll wait a little time and try again and hopefully next time won't crash.
					 */
					waitTime = 10;
					printJustExceptionMessage = true;
				}
				else
				{
					/* If it is not a well known recoverable exception, throw an exception to make the test fail. If after look at logs it is a new recoverable exception, add it here
					 * in a new ""else if.
					 */
					abort = true;
					printJustExceptionMessage = false;
				}

				exception = e;
			}
			catch(Exception e)
			{
				/* If it is not a well known recoverable exception, throw an exception to make the test fail. If after look at logs it is a new recoverable exception, add it here
				 * in a new ""else if.
				 */
				abort = true;
				exception = e;
				printJustExceptionMessage = false;
			}

			if(exception != null)
			{
				synchronized(classLock)
				{
					if(!abort)
					{
						LOGGER.debug(String.format("A recoverable exception was caught while trying to open the browser, will wait %s seconds and try again.", waitTime));
					}
					else
					{
						LOGGER.debug(String.format("An Exception occurred when trying to start the browser, will abort, this is not an identifying Exception."));
					}

					if(printJustExceptionMessage)
					{
						System.out.println(exception.getMessage());
					}
					else
					{
						exception.printStackTrace();
					}
				}
			}

			if(abort)
			{
				break;
			}
			else
			{
				Pause.seconds(waitTime);
			}
		}

		if(seleniumClient != null && seleniumClient.isBrowserInitialized())
		{
			synchronized(classLock)
			{
				LOGGER.info("");

				if(getSeleniumSessionLogsUrl() != null)
				{
					LOGGER.info("Browserstack/Sauce labs selenium session started: " + getSeleniumSessionLogsUrl());
				}

				LOGGER.info("Capabilities: {");
				for(Map.Entry<String, Object> entry : getBrowserConfiguration().getCapabilities().entrySet())
				{
					LOGGER.info("     " + entry.getKey() + ": " + entry.getValue() + ",");
				}

				LOGGER.info("   }");

				LOGGER.info("");
			}
		}
		else
		{
			throw new CouldNotInitBrowserException(exception);
		}

		return seleniumClient;
	}


	protected void closeBrowser()
	{
		try
		{
			if(seleniumClient != null && seleniumClient.isBrowserInitialized())
			{
				getSeleniumClient().closeBrowser();
				seleniumClient = null;
			}
			else
			{
				LOGGER.trace("SeleniumWrapper or the browser is not initialized. Have the test explicitly closed the browser?");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	protected boolean isDesktopLayout()
	{
		return getSeleniumClient().isDesktopLayout();
	}


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod_InitTest(Method method, Object[] parameters)
	{
		increaseTestStepsCount();

		String threadName = getClass().getSimpleName()
				+ "-"
				// + getBrowserConfiguration().getId()
				// + "-"
				+ Integer.toHexString(this.hashCode());

		Thread.currentThread().setName(threadName);

		associateInstanceToThread(Thread.currentThread().getId(), this);

		super.beforeMethod_InitTest(method, parameters);
	}


	@BeforeMethod(dependsOnMethods = "beforeMethod_InitTest", alwaysRun = true)
	public void beforeMethod_GetTestData(Method method, Object[] parameters)
	{
		// Remember that the infrastructure V2 works with 1 test per class. We could have several test methods in that class if we want to break down the test into steps
		// so we need to make sure that the data is setup only before running the first step. This method will be called before every test step
		if(isFirstTestStep())
		{
			LOGGER.info("Setting up test data");

			testData = new TestSetup(getTestIoSpec()).getData();
			setData(testData);
		}
	}


	@BeforeMethod(dependsOnMethods = "beforeMethod_GetTestData", alwaysRun = true)
	public void beforeMethod_InitSeleniumTest(Method method, Object[] parameters)
	{
		LOGGER.info("Browser configuration: " + getBrowserConfiguration().getId());
	}


	/**
	 * This method creates the browser automatically before running the test.
	 * Note that if dependsOnMethods or dependsOnGroups is not defined, TestNG runs the BeforeMethod/AfterMethod in alphabetical order, so a prefix may be set to get the desired order.
	 * */
	@BeforeMethod(dependsOnMethods = "beforeMethod_InitSeleniumTest", alwaysRun = true)
	public void beforeMethod_InitBrowser(Method method, Object[] parameters)
	{
		if(getSeleniumClient() == null && (!method.isAnnotationPresent(BrowserDirectives.class) || method.getAnnotation(BrowserDirectives.class).start()))
		{
			String className = getClass().getName().replace("com.jobaline.uiautomation.tests.", "");
			setSeleniumSessionName(EnvironmentUtils.ciJobNameAndBuildNumber + " - " + className + "." + method.getName() + "()");

			SeleniumWrapper seleniumClient = initBrowser();

			setSeleniumClient(seleniumClient);
		}
	}


	/**
	 * This method closes the browser automatically after running the test.
	 * Note that if dependsOnMethods or dependsOnGroups is not defined, TestNG runs the BeforeMethod/AfterMethod in alphabetical order, so a prefix may be set to get the desired order.
	 *
	 * IMPORTANT: if you wanna change the name, check there is no subclass overriding this method. For example, in SMS tests.
	 * */
	@AfterMethod(alwaysRun = true)
	public void afterMethod_CloseBrowser(ITestResult result, Method method, Object[] parameters)
	{
		if(method.isAnnotationPresent(BrowserDirectives.class) && !method.getAnnotation(BrowserDirectives.class).close() && result.isSuccess()) // If the test specifies that the browser must remain open is because a dependent test will close it, but if the test fail, the dependent will not run so we need to close the browser
		{
			return;
		}

		try
		{
			if(getSeleniumClient() != null && getSeleniumClient().getTakeInHouseScreenshots())
			{
				getSeleniumClient().takeScreenshot("close browser");

				String screenshotsDirectory = EnvironmentUtils.getScreenshotsDirectoryPath();

				String jobName = getSeleniumSessionName();
				String resultText = getTestResultText(result);
				String browser = getBrowserConfiguration().getId();
				String executionTime = getTestExecutionTime(result).toString();
				List<String> screenshotsNames = getSeleniumClient().getScreenshotsCreated();

				VelocityContext context = new VelocityContext();
				context.put("testName", jobName);
				context.put("resultText", resultText);
				context.put("browser", browser);
				context.put("executionTime", executionTime);
				context.put("screenshotsFileNames", screenshotsNames);

				Properties props = new Properties();
				props.setProperty("resource.loader", "class");
				props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
				VelocityEngine engine = new VelocityEngine(props);
				engine.init();

				Writer writer = new StringWriter();
				Template template = engine.getTemplate("testLog.vm");
				template.merge(context, writer);

				String htmlBody = writer.toString();

				String fileName = getSeleniumClient().getDefaultScreenshotsPrefix() + ".html";

				File file = new File(screenshotsDirectory + System.getProperty("file.separator") + fileName);

				FileUtils.write(file, htmlBody);

				String logFileUrl = null;

				if(getSeleniumClient().isTesterMachine())
				{
					logFileUrl = file.getAbsolutePath();
				}
				else if(getSeleniumClient().isCIMachine())
				{
					boolean useS3 = true; // This should be a configuration. Screenshots are uploaded
					if(useS3)
					{
						logFileUrl = "http://jobalineci-test-run-logs.s3-website-us-east-1.amazonaws.com/" + fileName;
					}
					else
					{
						String logFileAbsolutePath = file.getAbsolutePath();
						logFileUrl = logFileAbsolutePath;

						String jenkinsJobUrl = System.getenv("JENKINS_URL");
						String jenkinsJobName = System.getenv("JOB_NAME");
						String jenkinsJobWorkspace = System.getenv("WORKSPACE");

						if(jenkinsJobUrl != null && jenkinsJobName != null && jenkinsJobWorkspace != null)
						{
							if(logFileAbsolutePath.startsWith(jenkinsJobWorkspace))
							{
								String logRelativePathToWorkspace = logFileAbsolutePath.substring(jenkinsJobWorkspace.length() + 1); // Plus 1 to exclude the: "/"

								logFileUrl = jenkinsJobUrl + "job/" + jenkinsJobName + "/ws/" + logRelativePathToWorkspace;
							}
						}
					}
				}

				setSeleniumSessionLogsUrl(logFileUrl);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			closeBrowser();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Prints a footer with valuable information about the result.
	 * Note that if dependsOnMethods or dependsOnGroups is not defined, TestNG runs the BeforeMethod/AfterMethod in alphabetical order, so a prefix may be set to get the desired order.
	 * */
	@AfterMethod(dependsOnMethods = "afterMethod_CloseBrowser", alwaysRun = true)
	public void afterMethod_PrintTestFooter(ITestResult result, Method method, Object[] parameters)
	{
		synchronized(classLock)
		{
			LOGGER.info("");

			String text = "Result: " + getTestResultText(result) + ", ";
			text += String.format("test: %s, browser: %s, time: %s, visual logs: %s", getTestName(method), getBrowserConfiguration().getId(), getTestExecutionTime(result).toString(), getSeleniumSessionLogsUrl());
			LOGGER.info(text);
		}
	}


	@Factory
	public Object[] create()
	{
		/* For the sake of being able to run a single test isolated without modifying the testng.xml configuration file, the DEBUG test name is the only
		 * one allowed to specify the packages instead of the classes in the testng.xml configuration file.
		 * This will make that all the tests classes will be instantiated but only those with the DEBUG group will run (the DEBUG test name includes
		 * the DEBUG group)
		 * */

		MatrixEntry testEntryInMatrix = OrchestratorActors.getMatrixActor().getTestEntryInMatrix(getTestId());

		List<TestIoEntry> testIoSpecs = testEntryInMatrix.getTestIoEntries();

		List<BrowserConfiguration> browsersConfigurations = EnvironmentUtils.getBrowserConfigurations();

		List<Object> instances = new ArrayList<>();

		for(TestIoEntry testIoSpec : testIoSpecs)
		{
			if(!testIoSpec.isEnabled())
			{
				continue;
			}

			for(BrowserConfiguration browsersConfiguration : browsersConfigurations)
			{
				try
				{
					BaseSeleniumTestV2 testInstance = getClass().newInstance();

					testInstance.setTestIoSpec(testIoSpec);
					testInstance.setBrowserConfiguration(browsersConfiguration);

					// If the test name is DEBUG will add the test instance only if it has the Test annotation with the DEBUG group, either in the class or in some method

					boolean addTestInstance = false;
					if(!EnvironmentUtils.testNames.equals("DEBUG"))
					{
						addTestInstance = true;
					}
					else
					{
						Test testAnnotationInClass = testInstance.getClass().getAnnotation(Test.class);
						if(testAnnotationInClass != null)
						{
							if(Arrays.asList(testAnnotationInClass.groups()).contains("DEBUG"))
							{
								addTestInstance = true;
							}
						}

						if(!addTestInstance)
						{
							for(Method m : testInstance.getClass().getMethods())
							{
								Test testAnnotationInMethod = m.getAnnotation(Test.class);
								if(testAnnotationInMethod != null)
								{
									if(Arrays.asList(testAnnotationInMethod.groups()).contains("DEBUG"))
									{
										addTestInstance = true;
										break;
									}
								}
							}
						}
					}

					if(addTestInstance)
					{
						instances.add(testInstance);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return instances.toArray();
	}
}
