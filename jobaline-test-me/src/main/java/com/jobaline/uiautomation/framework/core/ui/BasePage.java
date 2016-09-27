package com.jobaline.uiautomation.framework.core.ui;

import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.exception.VerifyBrowserLocationException;
import com.jobaline.uiautomation.framework.selenium.IHasAccessToSelenium;
import com.jobaline.uiautomation.framework.selenium.SeleniumWrapper;
import com.jobaline.uiautomation.framework.testng.SeleniumClientProxy;

/**
 * It is the base class of all the objects following the Page Pattern.
 *
 * A Page Object will never perform tests asserts, it only manipulates page's elements.
 *
 * It could only perform some specific verification and throw an exception on very specific cases. For example, a page object has a method to click a button but the
 * button is not in the page. When this method is called, an exception must happen because if the button is not there it can not be clicked.
 *
 * See "Selenium 2 Testing tools" book, page 75.
 */
public abstract class BasePage implements IHasAccessToSelenium
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BasePage.class.getName().replace("com.jobaline.uiautomation.", ""));

	/**
	 * After the page changes, it may happens that the Selenium driver takes a little bit to return the new url.
	 * */
	private static int VERIFY_LOCATION_USING_URL_TIMEOUT = 3000;

	/**
	 * After the page changes, it may happens that the Selenium driver takes a little bit to return the new url.
	 * */
	private static int VERIFY_LOCATION_USING_TITLE_TIMEOUT = 3000;

	/**
	 * Small security margin in case the element is not added to the dom yet.
	 * */
	private static int VERIFY_LOCATION_USING_ELEMENT_TIMEOUT = 3000;


	public BasePage()
	{
	}


	private static BasePage createSubclassInstance()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String subClassName = null;

		// 0 will be java.lang.Thread
		// Then, it will be several times BasePage until we reach teh subclass
		for(int i = 1; i < stackTrace.length; i++)
		{
			StackTraceElement stackTraceElement = stackTrace[i];
			if(!stackTraceElement.getClassName().equals(BasePage.class.getName()))
			{
				subClassName = stackTraceElement.getClassName();
				break;
			}
		}

		Class<? extends BasePage> pageClass;
		try
		{
			pageClass = (Class<? extends BasePage>)Class.forName(subClassName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Could not create an instance of the page object. This is a programming error. Please fix it.");
		}

		// If the class obtained from the stacktrace is not a subclass of BasePage is because the static method invoked was not defined in the
		// subclass.
		if(!BasePage.class.isAssignableFrom(pageClass))
		{
			throw new RuntimeException("You need to define the static method in the subclass.");
		}

		BasePage page;
		try
		{
			page = pageClass.newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Could not create an instance of the page object. This is a programming error. Please fix it.");
		}

		return page;
	}


	protected abstract String getPath();

	// region
	// The following static method must be defined in the subclasses if needed and just call the implementation of this class. Java does not support
	// static methods overriding, only static methods hiding.
	// If we have a SearchPage and we want to have the method open(), we have to define the method open() as follow in the SearchPage class:
	//
	// public static void open()
	// {
	// 		BasePage.open();
	// };
	//
	// Of course, we can change the implementation, but usually the implementation in this class is enough so we just need to make a call to the corresponding
	// method in the super class.


	public static void verifyLocation()
	{
		createSubclassInstance().verifyLocationImpl();
	}


	public static void open()
	{
		createSubclassInstance().openImpl();
	}


	public static void open(String subdomain)
	{
		createSubclassInstance().openImpl(subdomain);
	}


	public static void open(String subdomain, Object... parameters)
	{
		createSubclassInstance().openImpl(subdomain, parameters);
	}


	public static String getUrl()
	{
		return createSubclassInstance().getUrlImpl();
	}


	public static String getUrl(String subdomain)
	{
		return createSubclassInstance().getUrlImpl(subdomain);
	}


	public static String getUrl(String subdomain, Object... parameters)
	{
		return createSubclassInstance().getUrlImpl(subdomain, parameters);
	}

	// endregion


	protected static void verifyLocationByCheckingUrlContainsText(String urlFragment)
	{
		verifyLocationByCheckingUrlContainsText(urlFragment, VERIFY_LOCATION_USING_URL_TIMEOUT);
	}


	protected static void verifyLocationByCheckingUrlContainsText(String urlFragment, long timeout)
	{
		SeleniumWrapper seleniumClient = new DummyPage().getSeleniumClient();

		String currentUrl = null;

		boolean result = false;
		long startTime = System.currentTimeMillis();
		while(!result && System.currentTimeMillis() - startTime <= timeout)
		{
			currentUrl = seleniumClient.getCurrentUrl();
			result = currentUrl.toLowerCase().contains(urlFragment.toLowerCase());
		}

		if(!result)
		{
			throw new VerifyBrowserLocationException("Page URL does not contain " + urlFragment + " / Current URL: " + currentUrl);
		}
	}


	protected static void verifyLocationByCheckingTitleContainsText(String titleFragment)
	{
		verifyLocationByCheckingTitleContainsText(titleFragment, VERIFY_LOCATION_USING_TITLE_TIMEOUT);
	}


	protected static void verifyLocationByCheckingTitleContainsText(String titleFragment, long timeout)
	{
		SeleniumWrapper seleniumClient = new DummyPage().getSeleniumClient();

		String currentTitle = null;

		boolean result = false;
		long startTime = System.currentTimeMillis();
		while(!result && System.currentTimeMillis() - startTime <= timeout)
		{
			currentTitle = seleniumClient.getCurrentTitle();
			result = currentTitle.toLowerCase().contains(titleFragment.toLowerCase());
		}

		if(!result)
		{
			throw new VerifyBrowserLocationException("Page title does not contain " + titleFragment + " / Current title: " + currentTitle);
		}
	}


	protected static void verifyLocationByCheckingPresenceOfElement(String elementLocator)
	{
		verifyLocationByCheckingPresenceOfElement(elementLocator, VERIFY_LOCATION_USING_ELEMENT_TIMEOUT);
	}


	protected static void verifyLocationByCheckingPresenceOfElement(String elementLocator, long timeout)
	{
		LOGGER.debug("Verifying the browser location by presence of element: " + elementLocator);

		SeleniumWrapper seleniumClient = new DummyPage().getSeleniumClient();

		boolean result = false;
		long startTime = System.currentTimeMillis();
		while(!result && System.currentTimeMillis() - startTime <= timeout)
		{
			try
			{
				result = seleniumClient.isElementDisplayed(elementLocator);
			}
			catch(WebDriverException e)
			{
				// The following exception must not be caught. It can not be fixed and will inevitably fail the test, there's no reason to retry: it will only make the test longer. 
				if(e.getMessage().toLowerCase().contains("session not started or terminated"))
				{
					throw e;
				}

				LOGGER.debug("An exception was caught while trying to verify the presence of element.");
				e.printStackTrace();
			}
		}

		if(!result)
		{
			throw new VerifyBrowserLocationException("Page does not contain the element: " + elementLocator);
		}
	}


	protected void verifyLocationImpl()
	{
		verifyLocationByCheckingUrlContainsText(EnvironmentUtils.getApplicationUnderTestDomain() + getPath());
	}


	protected final void openImpl()
	{
		openImpl("jobs");
	}


	protected final void openImpl(String subdomain)
	{
		openImpl(subdomain, new Object[]{});
	}


	protected final void openImpl(String subdomain, Object... parameters)
	{
		getSeleniumClient().openUrl(getUrl(subdomain, parameters));
	}


	protected final String getUrlImpl()
	{
		return getUrlImpl("jobs");
	}


	protected final String getUrlImpl(String subdomain)
	{
		return getUrlImpl(subdomain, new Object[]{});
	}


	protected final String getUrlImpl(String subdomain, Object... parameters)
	{
		if(parameters.length % 2 != 0)
		{
			throw new RuntimeException("The list of arguments to create the url query string must be pair since it will be mapped to a parameter name/value map.");
		}

		String queryString = "";

		if(parameters.length != 0)
		{
			queryString = "?";
			for(int i = 0; i < parameters.length; i += 2)
			{
				queryString += String.format("%s=%s&", parameters[i], parameters[i + 1]);
			}
		}

		return "http://" + subdomain + EnvironmentUtils.getApplicationUnderTestDomain() + getPath() + queryString;
	}


	public SeleniumWrapper getSeleniumClient()
	{
		return SeleniumClientProxy.getSeleniumClient();
	}

}
