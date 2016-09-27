package com.jobaline.uiautomation.framework.selenium;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jobaline.uiautomation.constants.GridsIds;
import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.lang.ListUtils;
import com.jobaline.uiautomation.framework.lang.Pause;
import com.jobaline.uiautomation.framework.selenium.browsers.AndroidWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.BrowserWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.ChromeWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.FirefoxWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.IPadWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.IPhoneWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.InternetExplorerWrapper;
import com.jobaline.uiautomation.framework.selenium.browsers.SafariWrapper;
import com.jobaline.uiautomation.framework.selenium.form.FormModel;
import com.jobaline.uiautomation.framework.selenium.form.IFormFieldModel;
import com.jobaline.uiautomation.framework.selenium.phantomJsThreeHourTimeoutFix.PhantomJSDriver;
import com.jobaline.uiautomation.framework.util.JSONObjectUtils;
import com.jobaline.uiautomation.framework.util.SystemUtils;

/**
 * Wrapper Class For Selenium
 * This class has critical sections, must be implemented carefully in order to be thread-safe
 */
public class SeleniumWrapper
{
	private static final Logger LOGGER = LoggerFactory.getLogger("Selenium");

	/**
	 * Following are the timeouts for different events.
	 * The timeouts need to be wisely decided:
	 * 	  - A big timeout will make tests to take a lot of time to fail when they are going to fail.
	 *    - A short timeout will make test taking a bit longer to respond to some event to fail when they wouldn't fail if the timeout was a bit longer
	 * */

	/* About implicit and explicit waits.
	 *
	 * The explicit waits are used with WebDriverWait (or FluentWait<WebDriver>) and ExpectedConditions. But most of the conditions are implemented with WebDriver.findElement() which is affected for the implicit timeout.
	 *
	 * Consider the following code:
	 *
	 * 	  WebDriverWait webDriverWait = new WebDriverWait(getWebDriver(), 2);
	 * 	  WebElement element = webDriverWait.until(ExpectedConditions.presenceOfElementLocated(someByForAnElementNotPresent));
	 *
	 * The timeout for the wait is 2 seconds (what wee call the explicit wait). But the expected condition is implemented using WebDriver.findElement(). If the
	 * implicit timeout is 15 seconds, I think that the result of the previous code is not completely predictable. Either:
	 *
	 *   1- It takes 2 seconds and the second line throws a TimeoutException or
	 *   2- it takes 15 seconds because the driver would eventually respond when it reaches the implicit timeout.
	 *
	 * I tested in BS_WIN8_FIREFOX_30 and the second case is happening!! although it may be different for each web driver implementation.
	 *2
	 * This is why the implicit wait must be short (1 or 2 seconds) and any operation affected by the implicit wait that we'd want to have a longer wait must be wrapper by an explicit wait.
	 *
	 * Also, by default, most of the operations done by this class on elements will assume that the elements are present in the page and:
	 * 	  will not wait for the element to be present
	 * 	  will throw an exception if the element is not present
	 *
	 * For example, findElement() will not wait for the element to be present in the page. It may wait at most the implicit wait time if it uses WebDriver.findElement() or may not wait if it is implemented via Javascript. The point is that
	 * whoever uses this method, must assume that the element is present. If the element is added to the DOM after an ajax request, the client of this method should use waitForElementToBePresent() before trying to use the element.
	 * The same applies for methods like click(), getText(), isElementDisabled(), etc.
	 */

	private static final int IMPLICIT_WAIT = 1500;

	// Time to wait for a page to start loading
	public static final long PAGE_TO_START_LOADING_TIMEOUT = 15 * 1000;

	// Time to wait for a page to load
	public static final long PAGE_LOAD_TIMEOUT = 70 * 1000;

	public static final long DEFAULT_AJAX_REQUEST_TIMEOUT = 45 * 1000;

	// Default timeout for a script executing purely in the client side to run. "purely in the client side" means that it does not make any ajax request nor downloads
	// any resource. It just make changes to the dom and Javascript variables.
	public static final long DEFAULT_CLIENT_SCRIPT_TIMEOUT = 3 * 1000;

	/**
	 * After the page starts loading, time to wait for the document to be in the "complete" state, see the document ready possible states here: https://developer.mozilla.org/en-US/docs/Web/API/document.readyState
	 *
	 * When Selenium has to load a url, it blocks until the request has a response and the page starts to load. Sadly, some browser don't block. But also, it may take time to download all the content of the page and
	 * have the dom complete. This wait will avoid to have issue in such cases.
	 */
	public static final long DOCUMENT_COMPLETE_TIMEOUT = 60 * 1000;
	public static final long DOCUMENT_COMPLETE_TIMEOUT_ANDROID_4 = 60 * 1000;

	private static final long ELEMENT_CLICKABLE_TIMEOUT = 3 * 1000;

	/**
	 * See isTesterMachine()
	 * */
	private Boolean isTesterMachine = false;

	/**
	 * See isCIMachine()
	 * */
	private Boolean isCIMachine = false;

	/**
	 * See isGrid()
	 * */
	private Boolean isGrid = false;

	/**
	 * */
	private Integer gridId;

	/**
	 * */
	private String gridUrl;

	private String taskName;

	/**
	 * If the driver of the browser in use inherit from RemoteWebDriver will be held by this variable.
	 * No matter if Selenium is running locally or in a grid.
	 *
	 * Currently, all the drivers we use in the project inherit from RemoteWebDriver. At some point we had some support for htmlunit and its driver
	 * (HtmlUnitWebDriver) does not inherit from RemoteWebDriver so we had to give a different treatment.
	 * */
	private RemoteWebDriver remoteWebDriver = null;

	private TakesScreenshot screenshotDriver;

	private BrowserWrapper browserWrapper = null;

	private BrowserConfiguration browserConfiguration = null;

	protected static Actions actions = null;

	private int screenshotIndex = 0;

	private List<String> screenshotsCreated = null;

	/**
	 * TestNG tests and Selenium possible configurations:
	 *
	 * 		Tester machine -> is grid -> is our grid
	 * 									 is Sauce Labs grid
	 * 									 is Browser stack grid
	 * 									 is Tester grid
	 *
	 * 		Tester machine -> is not grid
	 *
	 * 		CI machine -> is grid -> is our grid
	 * 								 is Sauce Labs grid
	 * 								 is Browser stack grid
	 *
	 * 		CI machine -> is not grid
	 *
	 * */

	/**
	 * Tell if TestNG is running in the machine of the tester.
	 * */
	public boolean isTesterMachine()
	{
		return isTesterMachine;
	}


	/**
	 * Tell if TestNG is running in some CI like Jenkins.
	 * */
	public boolean isCIMachine()
	{
		return isCIMachine;
	}


	/**
	 * Tell if Selenium is running tests are in a grid or in local browser.
	 * */
	public boolean isGrid()
	{
		return isGrid;
	}


	/**
	 * Tell if tests are running in a grid built by the tester.
	 * */
	public boolean isTesterGrid()
	{
		if(gridId != null && (gridId.intValue() == GridsIds.TESTERGRID) && (!isGrid() || !isTesterMachine()))
		{
			throw new RuntimeException("Your selenium configuration is not consistent. If isTesterGrid is true, isGrid and isTesterMachine must be true too.");
		}

		return isGrid() && gridId.intValue() == GridsIds.TESTERGRID;
	}


	/**
	 * Suppressing constructor to enforce Singleton
	 */
	private SeleniumWrapper()
	{
	}


	public SeleniumWrapper(BrowserConfiguration browserConfiguration, boolean isTesterMachine, boolean isCIMachine, boolean isGrid, Integer gridId, String gridUrl, String taskName)
	{
		this.browserConfiguration = browserConfiguration;
		this.isTesterMachine = isTesterMachine;
		this.isCIMachine = isCIMachine;
		this.isGrid = isGrid;
		this.gridId = gridId;
		this.gridUrl = gridUrl;
		this.taskName = taskName;
	}


	public synchronized boolean isBrowserInitialized()
	{
		return browserWrapper != null && getWebDriver() != null;
	}


	/**
	 * Return the WebDriver associated to the current being used by this instance.
	 * */
	public WebDriver getWebDriver()
	{
		return remoteWebDriver;
	}


	/**
	 * */
	public BrowserConfiguration getBrowserConfiguration()
	{
		return browserConfiguration;
	}


	public String getSessionId()
	{
		return ((RemoteWebDriver)getWebDriver()).getSessionId().toString();
	}


	public boolean isInternetExplorer()
	{
		return browserWrapper instanceof InternetExplorerWrapper;
	}


	public boolean isSafari()
	{
		return browserWrapper instanceof SafariWrapper;
	}


	public boolean isChrome()
	{
		return browserWrapper instanceof ChromeWrapper;
	}


	public boolean isFirefox()
	{
		return browserWrapper instanceof FirefoxWrapper;
	}


	public boolean isAndroid()
	{
		return browserWrapper instanceof AndroidWrapper;
	}


	public boolean isIphone()
	{
		return browserWrapper instanceof IPhoneWrapper;
	}


	public boolean isIpad()
	{
		return browserWrapper instanceof IPadWrapper;
	}


	/**
	 * Tells if the website is displaying with the phone layout.
	 * */
	public boolean isPhoneLayout()
	{
		return browserConfiguration.isPhone();
	}


	/**
	 * Tells if the website is displaying with the phone layout.
	 * */
	public boolean isTabletLayout()
	{
		return browserConfiguration.isTablet();
	}


	/**
	 * Tells if the website is displaying with the phone layout.
	 * */
	public boolean isDesktopLayout()
	{
		return browserConfiguration.isDesktop();
	}


	/**
	 * Initialize a local browser and the driver to manipulate it.
	 * */
	public void initBrowser(Map<String, Object> capabilities)
	{
		if(isGrid())
		{
			if(taskName != null)
			{
				// The "name" capability is available both in Sauce Labs, Browserstack and our grid.
				capabilities = new HashMap<>(capabilities);
				capabilities.put("name", taskName);
			}

			initGridBrowser(capabilities);
		}
		else
		{
			initLocalBrowser(capabilities);
		}

		setImplicitWait(IMPLICIT_WAIT, TimeUnit.MILLISECONDS);
	}


	/**
	 * Initialize a local browser and the driver to manipulate it.
	 * */
	private void initLocalBrowser(Map<String, Object> capabilities)
	{
		browserWrapper = BrowserWrapper.getBrowserWrapper(capabilities);

		if(capabilities.get("browserName").equals("phantomjs"))
		{
			remoteWebDriver = new PhantomJSDriver(browserWrapper.getCapabilities());
			setWindowSize(new Dimension(1024, 768));
		}
		else
		{
			throw new RuntimeException("The browser is not supported running Selenium locally: " + capabilities.get("browserName"));
		}

		setPageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
	}


	/**
	 * Initialize a browser belonging to a grid and the remote driver to manipulate it. The possible values of the parameters are those allowed by DesiredCapabilities.
	 * */
	private void initGridBrowser(Map<String, Object> capabilities)
	{
		capabilities = new HashMap<>(capabilities); // Create a new Map object from the object passed by parameter to not modify the original object

		browserWrapper = BrowserWrapper.getBrowserWrapper(capabilities);

		try
		{
			remoteWebDriver = new RemoteWebDriver(new URL(gridUrl), browserWrapper.getCapabilities());
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
		}
	}


	public void setPageLoadTimeout(long time, TimeUnit unit)
	{
		try
		{
			getWebDriver().manage().timeouts().pageLoadTimeout(time, unit);
		}
		catch(Exception e)
		{
			LOGGER.debug("Page load timeout could not be set in the current browser: " + getBrowserConfiguration().getId());
		}
	}


	/**
	 * Clean the session.
	 * */
	public void cleanSession()
	{
		getWebDriver().manage().deleteAllCookies();
	}


	/**
	 * Close the browser.
	 * */
	public synchronized void closeBrowser()
	{
		if(isGrid && isTesterGrid()) // There is no need to clean the session in Browserstack or Sauce Labs, they provide new clean virtual machines for every test
		{
			try
			{
				cleanSession();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		/* Will not execute WebDriver.close() because WebDriver.quit() should be enough
		try
		{
			// Sauce Labs does not support closing browser for iPhone and IPad. It leads in losing the connection.
			// Browserstack does no support closing browser for iPhone and IPad.
			// JSG uses Selendroid for Android and it does support closing browser.
			if(
				(isSauceLabsGrid() && !isIpad() && !isIphone()) ||
				(isBrowserStackGrid() && !isIpad() && !isIphone()) ||
				(isOurGrid() && !isAndroid())
			)
			{
				getWebDriver().close();
			}
		}
		catch (Exception e)
		{
			LOGGER.debug("An exception occurred when trying to execute WebDriver.close().");
			LOGGER.debug(e.getClass().toString() + ": " + e.getMessage() + " / " + " / " + getWebDriver().getClass().toString() + ".close()");
		}
		*/

		try
		{
			LOGGER.debug("Quitting browser, calling WebDriver.quit().");

			getWebDriver().quit();

			LOGGER.debug("Browser closed");
		}
		catch(Exception e)
		{
			LOGGER.trace("An exception occurred when trying to execute WebDriver.quit().");
			LOGGER.trace(e.getClass().toString() + ": " + e.getMessage() + " / " + " / " + getWebDriver().getClass().toString() + ".close()");
		}

		remoteWebDriver = null;
		browserWrapper = null;
	}


	public boolean getTakeInHouseScreenshots()
	{
		return System.getProperty("tests.takeInHouseScreenshots", "0").equals("1") && browserConfiguration.supportsInHouseScreenshots();
	}


	private void checkUrlIsNotProd(String url)
	{
		if(url.contains("jobaline.com"))
		{
			if(!EnvironmentUtils.isProdTesting())
			{
				System.out.println("The url seems to be PROD: " + url + ". Have you tried to open a PROD page??!! Please, review this. Thread name: " + Thread.currentThread().getName());
				System.exit(1);
			}
		}
	}


	public void setImplicitWait(long time, TimeUnit unit)
	{
		try
		{
			getWebDriver().manage().timeouts().implicitlyWait(time, unit);
		}
		catch(Exception e)
		{
			LOGGER.debug("Could not set the implicit wait to " + time + " " + unit);
		}
	}


	public void openUrl(String path)
	{
		LOGGER.debug("Opening url: " + path);

		takeScreenshot("before opening url: " + path);

		checkUrlIsNotProd(path);

		getWebDriver().navigate().to(path);

		waitForDocumentReadyStatusToBeComplete();
	}


	public void refreshPage()
	{
		LOGGER.debug("Refreshing the current page.");

		takeScreenshot("before refreshing page");

		getWebDriver().navigate().refresh();

		waitForDocumentReadyStatusToBeComplete();
	}


	public String getCurrentUrl()
	{
		String url = getWebDriver().getCurrentUrl();

		checkUrlIsNotProd(url);

		return url;
	}


	public String getCurrentTitle()
	{
		return getWebDriver().getTitle();
	}


	/**
	 * Please be careful, this method will return null if the browser does not support this feature (set/get the windows size).
	 * */
	public Dimension getWindowSize()
	{
		if(browserConfiguration.supportsWindowSize())
		{
			return getWebDriver().manage().window().getSize();
		}
		else
		{
			return null;
		}
	}


	public Dimension getDocumentSize()
	{
		Dimension d;

		// TODO move the script to a js file
		String script = "";
		script += "try { ";
		script += "  return '{width:' + (typeof jQuery === 'undefined'? 0 : jQuery(document).width()) + ', height:' + (typeof jQuery === 'undefined'? 0 : jQuery(document).height()) + '}';";
		script += "}";
		script += "catch(error) {";
		script += "  return '{width:0, height:0}';";
		script += "}";

		String json = (String)executeScript(script);
		try
		{
			JSONObject jsonObject = new JSONObject(json);
			d = new Dimension(jsonObject.getInt("width"), jsonObject.getInt("height"));
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			throw new RuntimeException("There was an error when trying to parse the json object returned after executing a script: " + json);
		}

		return d;
	}


	/**
	 * Please be careful, this method will do nothing if the the browser does not support this feature (set/get the windows size).
	 * */
	public void setWindowSize(Dimension dimension)
	{
		if(browserConfiguration.supportsWindowSize())
		{
			getWebDriver().manage().window().setSize(dimension);
		}
		else
		{
			LOGGER.debug("Setting/getting the windows size is not supported in the current browser.");
		}
	}


	public void maximize()
	{
		try
		{
			getWebDriver().manage().window().maximize();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOGGER.debug(e.getClass().getName() + " : " + getWebDriver().getClass().getName() + ".manage().window().maximize()" + " / Exception message: " + e.getMessage());
		}
	}


	public void scrollBottom()
	{
		executeScript("window.scrollTo(0, document.body.scrollHeight);");
	}


	public void scrollToElement(String cssSelector)
	{
		executeScript("jQuery(document).scrollTop(jQuery('" + cssSelector + "').offset().top);");
	}


	public void setCookie(String key, String value)
	{
		String domain = EnvironmentUtils.getApplicationUnderTestDomain();
		Cookie cookie = new Cookie(key, value, domain, "/", null);
		getWebDriver().manage().addCookie(cookie);
	}


	public String getCookieValue(String key)
	{
		Cookie cookie = getWebDriver().manage().getCookieNamed(key);
		return cookie == null? null : cookie.getValue();
	}


	/**
	 * The default type is css selector. So if the locator does not have a prefix, it will be a css selector. For example "a.submit_button" and "css=a.submit_button" will
	 * be equivalent.
	 *
	 * The other types must have a prefix, for example:
	 *
	 * 	"id=some_id"
	 * 	"link=click here"
	 * 	"xpath=//div"
	 * 	"name=input_name"
	 * 	"tag=a"
	 * 	"class=some_class"
	 *
	 * */
	public By getElementBy(String locator)
	{
		LOGGER.trace("By: " + locator);

		String cssSelectorWithAttributeFilter_RegEx = ".*\\[.+=.+\\].*"; // This will match for example: "input[name=email]" or "#container input[name=email]"
		if(!locator.contains("=") || (!locator.startsWith("css=") && locator.matches(cssSelectorWithAttributeFilter_RegEx)))
		{
			return By.cssSelector(locator);
		}

		int i = locator.indexOf("=");
		String locatorType = locator.substring(0, i).toLowerCase();
		String locatorValue = locator.substring(i + 1);

		if(locatorType.equalsIgnoreCase("css"))
		{
			return By.cssSelector(locatorValue);
		}
		else if(locatorType.equalsIgnoreCase("id"))
		{
			return By.id(locatorValue);
		}
		else if(locatorType.equalsIgnoreCase("link"))
		{
			return By.linkText(locatorValue);
		}
		else if(locatorType.equalsIgnoreCase("name"))
		{
			return By.name(locatorValue);
		}
		else if(locatorType.equalsIgnoreCase("xpath"))
		{
			return By.xpath(locatorValue);
		}
		else if(locatorType.equalsIgnoreCase("tag"))
		{
			return By.tagName(locatorValue);
		}
		else if(locatorType.equalsIgnoreCase("class"))
		{
			return By.className(locatorValue);
		}
		else
		{
			throw new RuntimeException("The locator's type is not any type supported: " + locator);
		}
	}


	public <T> T waitForExpectedCondition(ExpectedCondition<T> condition, long timeout)
	{
		return waitForExpectedCondition(condition, timeout, null);
	}


	public <T> T waitForExpectedCondition(ExpectedCondition<T> condition, long timeout, String message)
	{
		FluentWait<WebDriver> wait = new FluentWait<>(getWebDriver()).withTimeout(timeout, TimeUnit.MILLISECONDS);

		if(message != null)
		{
			wait.withMessage(message);
		}

		return wait.until(condition);
	}


	/**
	 * @param referenceToOldPage WebElement of the old page. If null, this method will not do anything
	 * */
	private void waitForPageToStartLoading(WebElement referenceToOldPage, long timeout)
	{
		/* Some browsers like Safari or Android emulators don't block the execution after an event that will fire a page load event
		 * (clicking a link, submittign a form). This must be handled because if not will cause a lot of errors. But it is
		 * a bug because the specified behavior is to block the execution. And, there is not any provided way to know if
		 * the page is loading (because it wouldn't be necessary if the execution blocked!).
		 * Here is a technique using StaleElementException to workaround this. When we find an element using
		 * WebDriver.findElement() or WebElement.findElement() the Web Driver implementation in the browser will maintain a reference in
		 * a cache, at some point, after the page starts to load, the driver will clear that cache and further references to the element
		 * will throw a StaleElementException. This way we know when a page is loading.
		 * */

		if(referenceToOldPage != null)
		{
			boolean isNewPageLoading = false;
			long start = System.currentTimeMillis();
			while(!isNewPageLoading && System.currentTimeMillis() - start < timeout)
			{
				try
				{
					String tagName = referenceToOldPage.getTagName();

					// Sauce labs android 4 phone returns the following message instead of throwing an Exception
					if(tagName.contains("element does not exist in cache"))
					{
						isNewPageLoading = true;
					}
				}
				catch(StaleElementReferenceException e)
				{
					isNewPageLoading = true;
				}
			}

			if(!isNewPageLoading)
			{
				throw new TimeoutWaitingForPageToStartLoadingException("Timeout waiting for the page to start loading.");
			}
		}
	}


	/**
	 * Wait for the document ready state to be "complete".
	 *
	 * Possible document states:
	 *
	 *	- uninitialized - Has not started loading yet
	 *	- loading - Is loading
	 *	- loaded - Has been loaded
	 *	- interactive - Has loaded enough and the user can interact with it
	 *	- complete - Fully loaded
	 *
	 * See the document ready possible states here: https://developer.mozilla.org/en-US/docs/Web/API/document.readyState
	 * */
	public void waitForDocumentReadyStatusToBeComplete()
	{
		/*
		 * This method will be called after some event (click on submit button, open url) firing a page load event.
		 * Browsers may take a while to set the status as loading. Need to add some fixed wait. Because if I
		 * check the status and it is not loading, there's no way to know if it is because the browser did not set the status
		 * or because the load finished.
		 * */

		if(getBrowserConfiguration().isIE8())
		{
			Pause.seconds(7);
		}
		else
		{
			Pause.oneSecond();
		}

		long timeout;

		timeout = DOCUMENT_COMPLETE_TIMEOUT;

		ExpectedCondition<Boolean> condition = driver ->
		{
			try
			{
				return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("JavaScript error"))
				{
					LOGGER.warn("A Javascript error happened when tried to get the document ready state: " + e.getMessage());
					return false;
				}
				else
				{
					throw e;
				}
			}
		};

		try
		{
			waitForExpectedCondition(condition, timeout, "Timeout waiting for the page to finish loading (By waiting for Javascript: document.readyState == \"complete\")");
		}
		catch(TimeoutException e)
		{
			takeScreenshot("after waiting for document to be ready (failed)");
			throw e;
		}

		takeScreenshot("after waiting for document to be ready");
	}


	public void waitForJQueryToBeLoaded()
	{
		boolean isLoaded = false;
		long startTime = System.currentTimeMillis();
		while(!isLoaded && System.currentTimeMillis() - startTime < SeleniumWrapper.DEFAULT_CLIENT_SCRIPT_TIMEOUT)
		{
			isLoaded = (Boolean)executeScript("return typeof $ !== 'undefined'");
		}

		if(!isLoaded)
		{
			throw new RuntimeException("JQuery is not available in the page. Did the page load successfully?");
		}
	}


	public void waitForElementToBeHidden(String cssSelector, long timeout, String errorMessage)
	{
		if(timeout < 0)
		{
			throw new RuntimeException("The parameter timeout must be >= 0");
		}

		boolean hidden = false;

		long startTime = System.currentTimeMillis();
		while(!hidden && System.currentTimeMillis() - startTime <= timeout) // Use <= to check at least once when the timeoutInSeconds = 0
		{
			hidden = isElementHidden(cssSelector);
		}

		if(!hidden)
		{
			if(errorMessage == null)
			{
				errorMessage = "Timeout waiting for the element with css selector '" + cssSelector + "' to be hidden";
			}

			takeScreenshot(errorMessage);

			throw new TimeoutException(errorMessage);
		}
	}


	public void waitForElementToBeEnabled(String cssSelector, long timeout, String errorMessage)
	{
		if(timeout < 0)
		{
			throw new RuntimeException("The parameter timeout must be >= 0");
		}

		boolean enabled = false;

		long startTime = System.currentTimeMillis();
		while(!enabled && System.currentTimeMillis() - startTime <= timeout) // Use <= to check at least once when the timeoutInSeconds = 0
		{
			enabled = !isElementDisabled(cssSelector);
		}

		if(!enabled)
		{
			if(errorMessage == null)
			{
				errorMessage = "Timeout waiting for the element with css selector '" + cssSelector + "' to be enabled";
			}

			takeScreenshot(errorMessage);

			throw new TimeoutException(errorMessage);
		}
	}


	public void focusElement(String cssSelector)
	{
		((JavascriptExecutor)getWebDriver()).executeScript("jQuery(\"" + cssSelector + "\")[0].focus();");
	}


	public void blurElement(String cssSelector)
	{
		((JavascriptExecutor)getWebDriver()).executeScript("jQuery(\"" + cssSelector + "\")[0].blur();");
	}


	public void triggerChangeEventOnElement(String cssSelector)
	{
		((JavascriptExecutor)getWebDriver()).executeScript("jQuery(\"" + cssSelector + "\").change()");
	}


	public String getBodyInnerHtml()
	{
		return getInnerHtml("body");
	}


	public String getInnerHtml(String cssSelector)
	{
		return (String)((JavascriptExecutor)getWebDriver()).executeScript("jQuery('" + cssSelector + "')[0].innerHTML;");
	}


	/**
	 * A click to a general element.
	 * This method does not have any expectation after the click. Is there is some expectation after the click, should use any other method provided
	 * by this class like clickAndWaitForPageToLoadCompletely() and clickLinkToAnotherPage()
	 * */
	public void click(Object locatorOrElement)
	{
		click(locatorOrElement, null);
	}


	/**
	 * A click to a general element.
	 * This method does not have any expectation after the click. Is there is some expectation after the click, should use any other method provided
	 * by this class like clickAndWaitForPageToLoadCompletely() and clickLinkToAnotherPage()
	 * */
	public void click(Object locatorOrElement, String elementDescription)
	{
		String locator = null;
		WebElement element;

		if(String.class.isInstance(locatorOrElement))
		{
			locator = (String)locatorOrElement;
			element = verifyElementClickable(getElementBy((String)locatorOrElement));
		}
		else if(WebElement.class.isInstance(locatorOrElement))
		{
			element = (WebElement)locatorOrElement;
		}
		else
		{
			throw new RuntimeException("The parameter locatorOrElement must be either a String or WebElement instance.");
		}

		String text = logClick(elementDescription != null? elementDescription : locatorOrElement);

		takeScreenshot("Before " + text);

		clickInternal(element, locator);

		takeScreenshot("After " + text);
	}


	/**
	 * Click any element that will fire a page load event except a traditional link of the form <a href="www.example.com">Example</a>. More
	 * strictly, a link that:
	 *
	 *   - fires a page load event
	 *   - the page loaded will be the page at the location specified in href.
	 *
	 * There are two reasons of why these links are not handled by this method:
	 *   - It uses JQuery.click() for some browsers because WebElemen.click(). JQuery.click() will not make the browser to follow the link.
	 *   - In some emulators, clicking these link fails so the workaround is to read the value of href and open it.
	 *
	 * To click these links use clickLinkToAnotherPage()
	 * */
	public void clickAndWaitForPageToLoadCompletely(Object locatorOrElement)
	{
		clickAndWaitForPageToLoadCompletely(locatorOrElement, null, PAGE_LOAD_TIMEOUT);
	}


	/**
	 * Click any element that will fire a page load event except a traditional link of the form <a href="www.example.com">Example</a>. More
	 * strictly, a link that:
	 *
	 *   - fires a page load event
	 *   - the page loaded will be the page at the location specified in href.
	 *
	 * There are two reasons of why these links are not handled by this method:
	 *   - It uses JQuery.click() for some browsers because WebElement.click() does not work. But JQuery.click() will not make the browser to follow the link.
	 *   - In some emulators, clicking these link fails so the workaround is to read the value of href and open it.
	 *
	 * To click these links use clickLinkToAnotherPage()
	 * */
	public void clickAndWaitForPageToLoadCompletely(Object locatorOrElement, String elementDescription, long timeout)
	{
		WebElement referenceToOldPage = getWebDriver().findElement(By.tagName("body"));

		click(locatorOrElement, elementDescription);

		waitForPageToStartLoading(referenceToOldPage, timeout);

		waitForDocumentReadyStatusToBeComplete();
	}


	/**
	 * Click elements like buttons that will not fire a page load event but instead might need to populate on the same page. 
	 * For eg. Run Report button on Usage Report. It fires an event to populate a table which takes some time to load. So to ensure that the table is loaded,it clicks the run report and wait for table to load.
	 * @param cssSelector : the cssSelector of the locator of element to be clicked
	 * @param timeout : timeout decided as per the page model.
	 * @param errorMessage : error Message to log in case of exception or error
	 **/
	public void clickAndWaitForContainText(String cssSelector, long timeout, String errorMessage)
	{
		String text = getText(cssSelector);

		click(cssSelector);

		waitForElementToContainText(cssSelector, text, true, true, timeout, errorMessage);

	}


	/**
	 * Clicks a traditional link of the form <a href="www.example.com">Example</a>. More
	 * strictly, a link that:
	 *
	 *   - fires a page load event
	 *   - the page loaded will be the page at the location specified in href.
	 *
	 * There are two reasons of why these links are not handled by the SeleniumWrapper.click():
	 *
	 *   - It uses JQuery.click() for some browsers because WebElement.click() does not always work but JQuery.click() does not work with plain links (it will trigger the click handlers attaches but won't make the browser follow the url).
	 *   - In some emulators, clicking these link fails so the workaround is to read the value of href and open it.
	 *
	 * */
	public void clickLinkToAnotherPage(Object locatorOrElement)
	{
		clickLinkToAnotherPage(locatorOrElement, null);
	}


	/**
	 * Clicks a traditional link of the form <a href="www.example.com">Example</a>. More
	 * strictly, a link that:
	 *
	 *   - fires a page load event
	 *   - the page loaded will be the page at the location specified in href.
	 *
	 * There are two reasons of why these links are not handled by this method:
	 *   - It uses JQuery.click() for some browsers because WebElemen.click(). JQuery.click() will not make the browser to follow the link.
	 *   - In some emulators, clicking these link fails so the workaround is to read the value of href and open it.
	 *
	 * To click these links use clickLinkToAnotherPage()
	 * */
	public void clickLinkToAnotherPage(Object locatorOrElement, String elementDescription)
	{
		WebElement element = getElement(locatorOrElement);

		verifyElementClickable(element);

		String text = logClickToLinkToAnotherPage(elementDescription != null? elementDescription : locatorOrElement);
		takeScreenshot("Before " + text);

		WebElement referenceToOldPage;
		if(isFirefox() || isChrome() || isInternetExplorer())
		{
			referenceToOldPage = null;
		}
		else
		{
			referenceToOldPage = getWebDriver().findElement(By.tagName("div"));
		}

		clickLinkToAnotherPageInternal(element);

		waitForPageToStartLoading(referenceToOldPage, PAGE_TO_START_LOADING_TIMEOUT);

		waitForDocumentReadyStatusToBeComplete();
	}


	/**
	 * Checks if an existent element is read only.
	 * If the element is not present in the page, a TimeoutException will be thrown.
	 * */
	public boolean isElementReadOnly(String cssSelector)
	{
		verifyElementPresent(cssSelector, 0);

		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/isElementReadOnly.js", true, parameters);

		JSONObject result = executeScriptToReadData(script);

		return result.getBoolean("isReadOnly");
	}


	/**
	 * Checks if an existent element is disabled.
	 * If the element is not present in the page, a TimeoutException will be thrown.
	 * */
	public boolean isElementDisabled(String cssSelector)
	{
		verifyElementPresent(cssSelector, 0);

		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/isElementDisabled.js", true, parameters);

		JSONObject result = executeScriptToReadData(script);

		return result.getBoolean("isDisabled");
	}


	/**
	 * Checks if an existent element is read only or disabled.
	 * If the element is not present in the page, a RuntimeException will be thrown.
	 * */
	public boolean isElementReadOnlyOrDisabled(Object locatorOrElement)
	{
		WebElement element = getElement(locatorOrElement);

		// For some some attributes, the next method returns either "true" or null.
		// See the documentation of WebElement.getAttribute(): https://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebElement.html#getAttribute(java.lang.String)

		String isReadOnly = element.getAttribute("readonly");
		if(isReadOnly != null && isReadOnly.equals("true"))
		{
			return true;
		}
		else
		{
			String isDisabled = element.getAttribute("disabled");
			return isDisabled != null && isDisabled.equals("true");
		}
	}


	/**
	 * Checks if an element exists (is present in the dom) and is displayed.
	 * */
	public boolean isElementDisplayed(String locator)
	{
		return isElementDisplayed(locator, 0);
	}


	/**
	 * Checks if an element exists (is present in the dom) and is displayed.
	 *
	 * @param timeout will wait this time if it is not displayed but will not throw an exception if the timeout is reached.
	 * */
	public boolean isElementDisplayed(String cssSelector, long timeout)
	{
		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/isElementDisplayed.js", true, parameters);

		boolean isDisplayed = false;
		long startTime = System.currentTimeMillis();
		while(!isDisplayed && System.currentTimeMillis() - startTime <= timeout)
		{
			JSONObject result = executeScriptToReadData(script);
			isDisplayed = result.getBoolean("isDisplayed");
		}

		return isDisplayed;
	}


	/**
	 * Checks if an element exists (is present in the dom) and is hidden.
	 * */
	public boolean isElementHidden(String cssSelector)
	{
		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/isElementHidden.js", true, parameters);

		JSONObject result = executeScriptToReadData(script);

		return result.getBoolean("isHidden");
	}


	/**
	 * @param element is mandatory
	 * @param locator is optional, if it is available, it will be used for some browsers when using a WebElement fails intermittently
	 * */
	private void clickInternal(WebElement element, String locator)
	{
		if(isInternetExplorer())
		{
			executeScript("arguments[0].click()", element);
		}
		else
		{
			try
			{
				element.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
				{
					// If we have this exception, let's wait a bit and try one more time, the page may be refreshing
					Pause.milliseconds(DEFAULT_CLIENT_SCRIPT_TIMEOUT);
					element.click();
				}
				else if(getBrowserConfiguration().isPhantomJs() && e.getMessage().contains("Click failed: timeout"))
				{
					LOGGER.warn("PhantomJS reported a failed click but it shouldn't affect tests, clicks are performed, the error is thrown because there is no confirmation from the browser.");
				}
				else
				{
					throw e;
				}
			}
		}
	}


	private void clickLinkToAnotherPageInternal(WebElement element)
	{
		if(isInternetExplorer())
		{
			executeScript("arguments[0].click()", element);
		}
		else if(isAndroid() || isIphone() || isIpad() || isSafari())
		{
			String location = element.getAttribute("href");
			openUrl(location);
		}
		else
		{
			element.click();
		}
	}


	public void inputText(String cssSelector, String textToType, boolean clearInputBefore)
	{
		WebElement element = findElement(cssSelector);

		if(clearInputBefore)
		{
			element.clear();
		}

		if(textToType == null || textToType.equals("")) // It will save some request to selenium server
		{
			logInputText(cssSelector, "");
		}
		else
		{
			inputTextWithoutLogging(element, textToType);

			String text = logInputText(element, textToType);

			takeScreenshot("After " + text);
		}
	}


	private void inputTextWithoutLogging(WebElement element, String textToType)
	{
		element.sendKeys(textToType);
	}


	private void inputTextWithoutLoggingUsingJavascript(WebElement element, String textToType)
	{
		((JavascriptExecutor)getWebDriver()).executeScript("jQuery(arguments[0]).val('" + textToType + "');", element);
	}


	/**
	 * Gets the text of a displayed element by its locator.
	 * @throws RuntimeException if the element is not present in the page or is hidden.
	 * */
	public String getText(String cssSelector)
	{
		return getText(cssSelector, true, null);
	}


	/**
	 * Gets the text of a displayed element by its locator.
	 * @throws RuntimeException if the element is not present in the page or is hidden.
	 * @throws RuntimeException if requireElementDisplayed is true and the element is present in the page but is hidden.
	 * */
	public String getText(String cssSelector, boolean requireElementDisplayed)
	{
		return getText(cssSelector, requireElementDisplayed, null);
	}


	public String getText(String cssSelector, boolean requireElementDisplayed, String notDisplayedErrorMessage)
	{
		if(requireElementDisplayed && !isElementDisplayed(cssSelector))
		{
			String errorMessage = notDisplayedErrorMessage != null? notDisplayedErrorMessage : "The element is present in the page but it is hidden.";
			throw new RuntimeException(errorMessage);
		}

		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/getElementText.js", true, parameters);

		JSONObject result = executeScriptToReadData(script);
		return result.getString("text");
	}


	/**
	 * This method is expected to be used for css selector matching multiple elements. Will return the texts of those elements.
	 * */
	public List<String> getElementsTexts(String cssSelector)
	{
		String script = ResourceManager.getJavascriptFileContent("js/framework/getElementsTexts.js", true, ListUtils.createList(
				cssSelector
		));

		JSONObject result = executeScriptToReadData(script);

		return JSONObjectUtils.toList(result.getJSONArray("texts"), String.class);
	}


	/**
	 * Returns true if the radio or checkbox is checked, false otherwise.
	 *
	 * If the element is not present will throw a TimeoutException
	 * */
	public boolean isChecked(String cssSelector)
	{
		verifyElementPresent(cssSelector, 0);

		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/isElementChecked.js", true, parameters);

		JSONObject result = executeScriptToReadData(script);

		return result.getBoolean("isChecked");
	}


	public void selectDropDownOption(Object locatorOrElement, String optionText)
	{
		Select selectElement = getSelectElement(locatorOrElement);

		selectElement.selectByVisibleText(optionText);

		LOGGER.trace(String.format("Dropdown option selected by text '%s'.", optionText));
	}


	public void selectDropDownOptionByValue(Object locatorOrElement, String value)
	{
		Select selectElement = getSelectElement(locatorOrElement);

		selectElement.selectByValue(value);

		LOGGER.trace(String.format("Dropdown option selected by value '%s'.", value));
	}


	public Map<String, String> getSelectOptions(String cssSelector)
	{
		String script = "";
		script += "data.options = {};";
		script += "jQuery('" + cssSelector + "').find('option').each(function()";
		script += "{";
		script += "		data.options[jQuery(this).val()] = jQuery(this).text();";
		script += "});";

		JSONObject data = executeScriptToReadData(script);

		Map<String, String> options = new HashMap<>();

		JSONObject options_json = data.getJSONObject("options");
		Iterator it = options_json.keys();
		while(it.hasNext())
		{
			String value = (String)it.next();
			String text = options_json.getString(value);
			options.put(value, text);
		}

		return options;
	}


	public String getSelectedDropDownOptionValue(String cssSelector)
	{
		Select dropDownList = findSelectElement(cssSelector);
		return dropDownList.getFirstSelectedOption().getAttribute("value");
	}


	public String getSelectedDropDownOptionText(String cssSelector)
	{
		Select dropDownList = findSelectElement(cssSelector);
		return dropDownList.getFirstSelectedOption().getText();
	}


	/**
	 * Checks if the element is present.
	 * Returns the element if present or throw a TimeoutException if not.
	 * */
	private WebElement verifyElementPresent(Object locator, long timeout)
	{
		By by;
		if(String.class.isInstance(locator))
		{
			by = getElementBy((String)locator);
		}
		else
		{
			by = (By)locator;
		}

		/* Selenium implements the wait just calling repeatedly WebDriver.findElement().
		 *
		 * Sometimes, this method is throwing a WebDriverException caused by a weird ClassCastException when tests are running in Browserstack.
		 *
		 * Looking at the method code (See RemoteWebDriver.findElement(String by, String using), in Selenium 2.42.2 it starts
		 * in the line 302), it is doing:
		 *
		 * 		Response response = execute(DriverCommand.FIND_ELEMENT,
		 * 			ImmutableMap.of("using", by, "value", using));
		 * 		Object value = response.getValue();
		 *
		 * The variable "value" should contain a WebElement, but sometimes it contains an empty String: "". As it is not a WebElement, a ClassCastException will
		 * happen. This is caught but a WebDriverException will be thrown.
		 *
		 * I think that this shouldn't happen, so we will catch the WebDriverException and retry a few more times. Must be careful because WebDriverException is
		 * superclass of TimeoutException and this one means that the element was not found. So we must not catch TimeoutException.
		 *
		 * Note that the purpose of this retry logic is to avoid weird behaviors. If Selenium behaves correctly, will only try 1 time and
		 * will throw a TimeoutException if the element is not present or won't throw an exception if it is present.
		 * */

		int triesForWeirdExceptions = 3;
		while(triesForWeirdExceptions > 0)
		{
			try
			{
				return waitForExpectedCondition(ExpectedConditions.presenceOfElementLocated(by), timeout);
			}
			catch(TimeoutException | NotFoundException e)
			{
				// This is an expected exception when the element is not present
				throw e;
			}
			catch(WebDriverException e)
			{
				// The following exceptions must not be caught. Can not be fixed and will inevitably fail the test, there's no reason to retry: it will only make the test longer.
				if(SeleniumUtils.isFatalSeleniumError(e))
				{
					throw e;
				}

				triesForWeirdExceptions--;
				LOGGER.debug("There was an exception, will try again " + triesForWeirdExceptions + " more times. Message: " + e.getMessage());
				e.printStackTrace();
				Pause.oneSecond();
			}
		}

		// The method will only reach here if there were weird exceptions. We will throw a TimeoutException as
		// WebDriverWait.until() should have thrown.
		throw new TimeoutException("The element is not present.");
	}


	/**
	 * Checks if the element is present, searching in the context specified.
	 * Returns the element if present or throw a RuntimeException if not.
	 * */
	private WebElement verifyElementPresent(Object locator, WebElement context, long timeout)
	{
		By by;
		if(String.class.isInstance(locator))
		{
			by = getElementBy((String)locator);
		}
		else
		{
			by = (By)locator;
		}

		/* Selenium implements the wait just calling repeatedly WebDriver.findElement().
		 * Sometimes, this method is throwing a weird ClassCastException when tests are running in Browserstack.
		 * Looking at the method code (See RemoteWebDriver.findElement(String by, String using), in Selenium 2.39 it starts
		 * in the line 302), it is doing:
		 *
		 * 		Response response = execute(DriverCommand.FIND_ELEMENT, ImmutableMap.of("using", by, "value", using));
		 * 		Object value = response.getValue();
		 *
		 * value should contain a WebElement, but sometimes it contains an empty String: "". I think that this shouldn't happen,
		 * so we will retry a few more times.
		 *
		 * Note that the purpose of this retry logic is to avoid weird behaviors. If Selenium behaves correctly, will only try 1 time and
		 * will throw a RuntimeException or not depending whether the element is not present or is present.
		 * */

		WebElement element = null;

		int triesForWeirdExceptions = 3;
		while(triesForWeirdExceptions >= 0)
		{
			// Important, don't catch RuntimeException!
			try
			{
				element = waitForExpectedCondition(new ExpectedCondition<WebElement>()
				{
					public WebElement apply(WebDriver driver)
					{
						WebElement element;
						try
						{
							element = context.findElement(by);
						}
						catch(Exception e)
						{
							element = null;
						}

						return element;
					}


					public String toString()
					{
						return "presence of element located by " + locator + " using context";
					}
				}, timeout);
			}
			catch(WebDriverException e)
			{
				// The following exception must not be caught. It can not be fixed and will inevitably fail the test, there's no reason to retry: it will only make the test longer.
				if(e.getMessage().toLowerCase().contains("session not started or terminated"))
				{
					throw e;
				}

				Pause.oneSecond();
			}

			triesForWeirdExceptions--;
		}

		if(element == null)
		{
			throw new RuntimeException("The elements are not present.");
		}

		return element;
	}


	private WebElement verifyElementClickable(String locator)
	{
		return verifyElementClickable(getElementBy(locator));
	}


	private WebElement verifyElementClickable(By by)
	{
		return waitForExpectedCondition(ExpectedConditions.elementToBeClickable(by), ELEMENT_CLICKABLE_TIMEOUT);
	}


	private WebElement verifyElementClickable(WebElement element)
	{
		return waitForExpectedCondition(ExpectedConditions.elementToBeClickable(element), ELEMENT_CLICKABLE_TIMEOUT);
	}


	public Object executeScript(String script, Object... args)
	{
		return ((JavascriptExecutor)getWebDriver()).executeScript(script, args);
	}


	/**
	 *
	 * The script to be executed need to save the data in the "data" variable that will hold a Javascript object
	 * */
	public JSONObject executeScriptToReadData(String script, Object... args)
	{
		// TODO move the script to a js file
		String script_aux = "";
		script_aux += "return (function()";
		script_aux += "{";
		script_aux += "		var result = {};";
		script_aux += "		result.code = 0;"; // Means success. Will overwrite it with 0 if there is an error.
		script_aux += "		try";
		script_aux += "		{";
		script_aux += "			var data = {};";
		script_aux += script;
		script_aux += "			result.data = data;";
		script_aux += "		}";
		script_aux += "		catch(error)";
		script_aux += "		{";
		script_aux += "			result.code = 1;";
		script_aux += "			result.errorMessage = error;";
		script_aux += "		}";
		script_aux += "		return JSON.stringify(result);";
		script_aux += "}).call();";

		if(script_aux.contains("$("))
		{
			waitForJQueryToBeLoaded();
		}

		String json = (String)((JavascriptExecutor)getWebDriver()).executeScript(script_aux, args);

		JSONObject data;

		if(json == null)
		{
			throw new RuntimeException("Could not read data from the page using Javascript. The script executed to read the data returned an empty result.");
		}
		else
		{
			try
			{
				JSONObject result = new JSONObject(json);

				if(result.getInt("code") == 1)
				{
					throw new RuntimeException("There was a error when trying to execute a Javascript to read data from the page: " + (result.has("errorMessage")? result.get("errorMessage").toString() : "error message not available."));
				}

				data = result.getJSONObject("data");
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("There was a error when trying to execute a Javascript to read data from the page. The JS script executed to read the data did not return a valid JSON string as a result: " + json);
			}
		}

		return data;
	}


	/**
	 * Check if an element is present given its css selector.
	 *
	 * An element present can be displayed or not.
	 */
	public boolean exists(String cssSelector)
	{
		List<String> parameters = ListUtils.createList(cssSelector);
		String script = ResourceManager.getJavascriptFileContent("js/framework/isElementPresent.js", true, parameters);

		JSONObject result = executeScriptToReadData(script);
		return result.getBoolean("isPresent");
	}


	/**
	 * Find the first element with the given locator.
	 *
	 * @param locator Object either a String locator (see the class Locator) or a By.
	 * @return WebElement the first element that matches the locator
	 */
	public WebElement findElement(Object locator)
	{
		return findElement(locator, true);
	}


	/**
	 * Find the first element with the given locator.
	 *
	 * If the element is not present, this method will either throw an Exception if verifyElementPresent=true or will return null if verifyElementPresent=false.
	 *
	 * TODO we need to reconsider to make verifications here because when an element is not present, an Exception is thrown and we do not context information about
	 * TODO why the element is missing, what page is it, etc, etc. The context information is in the client of the selenium layer. Maybe we can add a parameter
	 * TODO with the error message so the clients can add the information needed.
	 *
	 * @param locator Object either a String locator (see the class Locator) or a By.
	 * @param verifyElementPresent boolean whether the method must verify that the element is present and throw an exception if it is not.
	 * @return WebElement the first element that matches the locator
	 */
	public WebElement findElement(Object locator, boolean verifyElementPresent)
	{
		By by;
		if(String.class.isInstance(locator))
		{
			by = getElementBy((String)locator);
		}
		else
		{
			by = (By)locator;
		}

		WebElement webElement;

		if(verifyElementPresent)
		{
			webElement = verifyElementPresent(by, IMPLICIT_WAIT);
		}
		else
		{
			try
			{
				webElement = getWebDriver().findElement(by);
			}
			catch(NoSuchElementException e)
			{
				webElement = null;
			}
			catch(WebDriverException e)
			{
				// The following exception must not be caught. It can not be fixed and will inevitably fail the test, there's no reason to retry: it will only make the test longer.
				if(e.getMessage().toLowerCase().contains("session not started or terminated") || e.getMessage().toLowerCase().contains("unable to communicate to node"))
				{
					throw e;
				}
				else
				{
					e.printStackTrace();
					throw new RuntimeException("An unexpected exception occurred while trying to find the element. Please see the stacktrace at the console.");
				}
			}
			catch(Exception e)
			{
				// If the element is not present, an exception will occur. It should be a NoSuchElementException but depending on the browser, the class of the exception
				// could be other. If this other exception has the same meaning of NoSuchElementException, it is fine and this method will return null. But if it is another
				// exception that shouldn't occur, for example, an exception telling that the Selenium session has been terminated, must abort and print the stacktrace of the exception.
				if(e.getMessage().contains("Unable to locate element"))
				{
					webElement = null;
				}
				else
				{
					e.printStackTrace();
					throw new RuntimeException("An unexpected exception occurred while trying to find the element. Please see the stacktrace at the console.");
				}
			}
		}

		return webElement;
	}


	/**
	 * Find the first element with the given locator.
	 *
	 * @param locator Object either a String locator (see the class Locator) or a By.
	 * @return WebElement the first element that matches the locator
	 */
	public WebElement findElement(Object locator, WebElement context)
	{
		return findElement(locator, context, true);
	}


	/**
	 * Find the first element with the given locator.
	 *
	 * TODO we need to reconsider to make verifications here because when an element is not present, an Exception is thrown and we do not context information about
	 * TODO why the element is missing, what page is it, etc, etc. The context information is in the client of the selenium layer. Maybe we can add a parameter
	 * TODO with the error message so the clients can add the information needed.
	 *
	 * @param locator Object either a String locator (see the class Locator) or a By.
	 * @param verifyElementPresent boolean whether the method must verify that the element is present and throw an exception if it is not.
	 * @return WebElement the first element that matches the locator
	 */
	public WebElement findElement(Object locator, WebElement context, boolean verifyElementPresent)
	{
		By by;
		if(String.class.isInstance(locator))
		{
			by = getElementBy((String)locator);
		}
		else
		{
			by = (By)locator;
		}

		WebElement webElement;

		if(verifyElementPresent)
		{
			webElement = verifyElementPresent(by, context, IMPLICIT_WAIT);
		}
		else
		{
			try
			{
				webElement = context.findElement(by);
			}
			catch(Exception e)
			{
				webElement = null;
			}
		}

		return webElement;
	}


	public WebElement findElementUsingJavascript(String script, String exceptionMessage)
	{
		WebElement webElement;
		try
		{
			webElement = (WebElement)executeScript(script);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ElementNotFoundException(exceptionMessage == null? "An element could not be found." : exceptionMessage);
		}

		if(webElement == null)
		{
			throw new ElementNotFoundException(exceptionMessage == null? "An element could not be found." : exceptionMessage);
		}

		return webElement;
	}


	/**
	 * Find all the elements with the given locator.
	 *
	 * @param locator Object either a String locator (see the class Locator) or a By.
	 * @return List<WebElement> list of all the elements matching the locator. Empty list if no element is found.
	 */
	public List<WebElement> findElements(Object locator)
	{
		By by;
		if(String.class.isInstance(locator))
		{
			by = getElementBy((String)locator);
		}
		else
		{
			by = (By)locator;
		}

		List<WebElement> webElements = getWebDriver().findElements(by);

		if(webElements.size() > 0 && RemoteWebDriver.class.isInstance(getWebDriver())) // JSON is not available in IE7
		{
			try
			{
				// TODO move the script to a js file
				String script = "";
				script += "	var elements = [];";
				script += "";
				script += "	jQuery(\"" + locator + "\").each(function()";
				script += "	{";
				script += "		var elementData = {};";
				script += "		elementData.attributes = {};";
				script += "		var attributes = this.attributes;";
				script += "		for(var i = 0; i < attributes.length; i++)";
				script += "		{";
				script += " 		attr = attributes[i];";
				script += "    		elementData.attributes[attr.nodeName.toLowerCase()] = attr.value;";
				script += "		}";
				script += "		elementData.text = jQuery(this).text();";
				script += "		elements.push(elementData);";
				script += "	});";
				script += "";
				script += "	data.elements = elements;";

				JSONObject data = executeScriptToReadData(script);

				JSONArray jsonArray = data.getJSONArray("elements");

				if(jsonArray.length() == webElements.size()) // We are querying the dom 2 times, the number of elements might have changed. If so, can not do anything.
				{
					List<WebElement> webElements_aux = new ArrayList<>();

					for(int i = 0; i < jsonArray.length(); i++)
					{
						String webDriverElementId = ((RemoteWebElement)webElements.get(i)).getId();

						JSONObject jsonObject = jsonArray.getJSONObject(i);
						webElements_aux.add(new OWebElement(jsonObject, webDriverElementId, (RemoteWebDriver)getWebDriver()));
					}

					webElements = webElements_aux;
				}
			}
			catch(Exception e)
			{
				LOGGER.debug("findElements(): the elements were found but could not load their data in a local cache.");
				e.printStackTrace();
			}
		}

		return webElements;
	}


	/**
	 * Iterates through a list of web elements and returns the first element containing the specified text string.
	 *
	 * @param locator locator string
	 * @param text    the text string element that need to match
	 * @return the first element that having this text
	 */
	public WebElement findElementContainingText(String locator, String text)
	{
		List<WebElement> webElements = findElements(locator);
		//we should use the sub functions
		return findElementContainingText(webElements, text);
	}


	/**
	 * Find the first Select element with the given locator.
	 *
	 * @param locator Object either a String locator (see the class Locator) or a By.
	 * @return Select the first select element that matches the locator
	 */
	public Select findSelectElement(String locator)
	{
		return new Select(findElement(locator, true));
	}


	private Select getSelectElement(Object locatorOrElement)
	{
		Select selectElement;

		if(String.class.isInstance(locatorOrElement))
		{
			selectElement = findSelectElement((String)locatorOrElement);
		}
		else if(WebElement.class.isInstance(locatorOrElement))
		{
			selectElement = new Select((WebElement)locatorOrElement);
		}
		else if(Select.class.isInstance(locatorOrElement))
		{
			selectElement = (Select)locatorOrElement;
		}
		else
		{
			throw new RuntimeException("The parameter locatorOrElement must be either a String, a Select or a WebElement instance.");
		}

		return selectElement;
	}


	private WebElement getElement(Object locatorOrElement)
	{
		WebElement element;

		if(String.class.isInstance(locatorOrElement))
		{
			element = findElement(locatorOrElement);
		}
		else if(WebElement.class.isInstance(locatorOrElement))
		{
			element = (WebElement)locatorOrElement;
		}
		else
		{
			throw new RuntimeException("The parameter locatorOrElement must be either a String or WebElement instance.");
		}

		return element;
	}


	public void waitForElementToContainText(String cssSelector, String text, boolean ignoreCase, boolean matchSubstring, long timeout, String errorMessage)
	{
		WebElement element = findElement(cssSelector);

		boolean result = false;

		long startTime = System.currentTimeMillis();

		while(!result && System.currentTimeMillis() - startTime < timeout)
		{
			if(matchSubstring)
			{
				result = ignoreCase? element.getText().toLowerCase().contains(text.toLowerCase()) : element.getText().contains(text);
			}
			else
			{
				result = ignoreCase? element.getText().equalsIgnoreCase(text) : element.getText().equals(text);
			}
		}

		if(!result)
		{
			if(errorMessage == null)
			{
				errorMessage = "Timeout waiting for element to contain the following text: " + text;
			}

			throw new TimeoutException(errorMessage);
		}
	}


	public void waitForElementToBePresent(String cssSelector, long timeout, String errorMessage)
	{
		// Don't change the implementation of this method to use: ExpectedConditions.presenceOfElementLocated();
		// or any other method that use its implementation like findElement()
		// We can not rely on that condition. Find the comments in this class about it.

		String script = "data.isPresent = jQuery('" + cssSelector + "').length !== 0;";

		boolean isPresent = false;
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime <= timeout)
		{
			JSONObject data = executeScriptToReadData(script);
			isPresent = data.getBoolean("isPresent");

			if(isPresent)
			{
				break;
			}
		}

		if(!isPresent)
		{
			if(errorMessage == null)
			{
				errorMessage = "Timeout while waiting for element to be present: '" + cssSelector + "'";
			}

			takeScreenshot(errorMessage);
			throw new TimeoutException(errorMessage);
		}
		else
		{
			takeScreenshot("after waiting for element to be present: '" + cssSelector + "'");
		}
	}


	public void waitForElementToBeDisplayed(String cssSelector, long timeout, String errorMessage)
	{
		if(errorMessage == null)
		{
			errorMessage = "Timeout while waiting for element to be displayed: '" + cssSelector + "'";
		}

		ExpectedCondition condition = ExpectedConditions.visibilityOfElementLocated(getElementBy(cssSelector));

		try
		{
			waitForExpectedCondition(condition, timeout, errorMessage);
			takeScreenshot("after waiting for element to be displayed: '" + cssSelector + "'");
		}
		catch(TimeoutException e)
		{
			takeScreenshot(errorMessage);
			throw e;
		}
	}


	/**
	 * Iterates through a list of web elements and returns the first element containing the specified text string.
	 *
	 * @param webElements list of webElements to match
	 * @param text        the text string element that need to match
	 * @return the first element that having this text
	 */
	public WebElement findElementContainingText(List<WebElement> webElements, String text)
	{
		for(WebElement webElement : webElements)
		{
			if(webElement.getText().contains(text))
			{
				return webElement;
			}
		}

		return null;
	}


	public String getElementAttribute(String cssSelector, String attrName)
	{
		return getElementsAttribute(cssSelector, attrName).get(0);
	}


	public List<String> getElementsAttribute(String cssSelector, String attrName)
	{
		List<String> values = new ArrayList<>();

		// TODO move the script to a js file
		String script = "";
		script += "var values = [];";
		script += "	jQuery(\"" + cssSelector + "\").each(function()";
		script += "	{";
		script += "		if(this.hasAttribute('" + attrName + "')) ";
		script += "		{ ";
		script += "			values.push(this.getAttribute('" + attrName + "'));";
		script += "		} ";
		script += "	});";
		script += "	data.values = values; ";

		JSONObject data = executeScriptToReadData(script);

		try
		{
			JSONArray jsonArray = data.getJSONArray("values");
			for(int i = 0; i < jsonArray.length(); i++)
			{
				values.add(jsonArray.getString(i));
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			throw new RuntimeException("An script executed did not return a valid json string.");
		}

		return values;
	}


	public void fillForm(FormModel formModel)
	{
		/*
		 * TODO improve: should be using another method instead of executeScriptToReadData() in order to have a better failure message
		 * */

		takeScreenshot("Before filling form: " + formModel.getName());

		List<IFormFieldModel> fields = formModel.getFields();

		int maxScriptSize = isAndroid() || isIphone() || isIpad()? 10000 : 30000;

		StringBuilder script = new StringBuilder();
		for(int i = 0; i < fields.size(); i++)
		{
			script.append(fields.get(i).generateAllStatements());
			if(script.length() > maxScriptSize || i == fields.size() - 1)
			{
				executeScriptToReadData(script.toString());
				script = new StringBuilder();
			}
		}

		takeScreenshot("After filling form");
	}


	public void takeScreenshotIfEnabled()
	{
		takeScreenshot(null);
	}


	public Integer getCurrentScreenshotsCount()
	{
		return screenshotIndex;
	}


	public Integer getCurrentScreenshotIndex()
	{
		return screenshotIndex;
	}


	public Integer getNextScreenshotIndex()
	{
		screenshotIndex++;
		return screenshotIndex;
	}


	public String getDefaultScreenshotsPrefix()
	{
		return getSessionId();
	}


	public String getDefaultScreenshotsExtension()
	{
		return "jpg";
	}


	private String createScreenshotName(String prefix, Integer index, String extension)
	{
		return String.format(prefix + "-%03d." + extension, index);
	}


	public List<String> getScreenshotsCreated()
	{
		if(screenshotsCreated == null)
		{
			screenshotsCreated = new ArrayList<>();
		}

		return screenshotsCreated;
	}


	public void takeScreenshot(String logMessage)
	{
		if(getTakeInHouseScreenshots() && browserConfiguration.supportsInHouseScreenshots())
		{
			takeScreenshot(logMessage, getDefaultScreenshotsPrefix());
		}
	}


	public void takeScreenshot(String logMessage, String imagePrefix)
	{
		takeScreenshot(logMessage, imagePrefix, getDefaultScreenshotsExtension(), 700, 0.50f);
	}


	/**
	 *
	 * @param prefix for the image name
	 * @param extension can be "jpg", "png" or "gif"
	 * @param maxWidth implemented only for jpg
	 * @param quality from 0 to 100. Implemented only for jpg
	 * */
	private void takeScreenshot(String logMessage, String prefix, String extension, Integer maxWidth, Float quality)
	{
		if(screenshotDriver == null)
		{
			try
			{
				if(getWebDriver() instanceof PhantomJSDriver)
				{
					screenshotDriver = (TakesScreenshot)getWebDriver();
				}
				else
				{
					screenshotDriver = (TakesScreenshot)new Augmenter().augment(getWebDriver());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}

		Dimension documentDimensions = null;
		try
		{
			documentDimensions = getDocumentSize();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(documentDimensions == null)
		{
			LOGGER.warn("Can't take screenshot because could not read the document size.");
			return;
		}

		long documentSize = (long)documentDimensions.getWidth() * (long)documentDimensions.getHeight();
		if(documentSize > browserConfiguration.getMaximumScreenshotPageSize())
		{
			LOGGER.debug("Can't take an screenshot with more than " + browserConfiguration.getMaximumScreenshotPageSize() + " pixels. Document size: " + documentDimensions.getWidth() + " x " + documentDimensions.getHeight() + "=" + documentSize);
			return;
		}

		int messageAreaHeight = 55;

		String sep = System.getProperty("file.separator");

		String dir = EnvironmentUtils.getScreenshotsDirectoryPath();

		String filename = createScreenshotName(prefix, getNextScreenshotIndex(), extension);
		String path = dir + sep + filename;

		if(logMessage == null || logMessage.isEmpty())
		{
			LOGGER.trace("Taking screenshot");
		}
		else
		{
			LOGGER.trace("Taking screenshot: " + logMessage);
		}

		File pngImageFile = screenshotDriver.getScreenshotAs(OutputType.FILE);

		if(pngImageFile != null)
		{
			try
			{
				if(extension.equals("gif"))
				{
					BufferedImage gifImage = ImageIO.read(pngImageFile);
					ImageIO.write(gifImage, "gif", new File(path));
				}
				else if(extension.equals("jpg"))
				{
					BufferedImage pngImage = ImageIO.read(pngImageFile);

					File imageFile = new File(path);

					int newWidth;
					if(maxWidth == null)
					{
						newWidth = pngImage.getWidth();
					}
					else
					{
						newWidth = Math.min(pngImage.getWidth(), maxWidth);
					}

					// Convert to RGB without transparency and scale image

					int width = pngImage.getWidth();
					int height = pngImage.getHeight();
					int newHeight = (height * newWidth) / width; // Don't do ((newWidth / width) * height) because it will be 0 if newWidth < with
					newHeight += messageAreaHeight;

					BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR); // I we don't specify BufferedImage.TYPE_INT_BGR, we get a red image
					Graphics2D g = resizedImage.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(pngImage, 0, 0, newWidth, newHeight - messageAreaHeight, 0, 0, width, height, Color.WHITE, null);
					g.setColor(Color.white);
					g.fill(new Rectangle(0, newHeight - messageAreaHeight, newWidth, newHeight));
					g.setColor(Color.black);
					g.fill(new Rectangle(0, newHeight - messageAreaHeight, newWidth, 3));

					g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

					// Will print at most 3 lines.

					int maxLines = 3;
					int charactersPerLine = 95;
					int lineHeight = 15;
					int textYCoordinate = newHeight - messageAreaHeight + lineHeight;

					int line = 0;
					while(logMessage.length() > charactersPerLine && line < maxLines - 1)
					{
						g.drawString(logMessage.substring(0, charactersPerLine), 5, textYCoordinate);
						logMessage = logMessage.substring(charactersPerLine, logMessage.length());
						textYCoordinate += lineHeight;
						line++;
					}

					if(logMessage.length() > 0)
					{
						if(logMessage.length() > charactersPerLine)
						{
							g.drawString(logMessage.substring(0, charactersPerLine) + "...", 5, textYCoordinate);
						}
						else
						{
							g.drawString(logMessage, 5, textYCoordinate);
						}
					}

					// Set quality and save the image

					ImageOutputStream ios = ImageIO.createImageOutputStream(imageFile);
					Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
					ImageWriter writer = iter.next();
					ImageWriteParam iwp = writer.getDefaultWriteParam();
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality(quality != null? quality : 0.80f);
					writer.setOutput(ios);
					writer.write(null, new IIOImage(resizedImage, null, null), iwp);

					g.dispose();
					ios.close();
					writer.dispose();

					System.gc();
				}
				else if(extension.equals("png"))
				{
					FileUtils.copyFile(pngImageFile, new File(path));
				}
				else
				{
					throw new RuntimeException("File extension " + extension + " not supported.");
				}

				if(screenshotsCreated == null)
				{
					screenshotsCreated = new ArrayList<>();
				}
				screenshotsCreated.add(filename);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				screenshotIndex--;
			}
		}
		else
		{
			LOGGER.trace("Could not take an screenshot, the driver returned a null value.");
		}
	}


	/**
	 * Logs a click.
	 * If the parameter is an instance of WebElement, the method will try to log either its id or the text if has no id. Call always this method
	 * before the element is clicked because if the page changes, may happen a StaleElementReferenceException.
	 * */
	private String logClick(Object locatorOrElement)
	{
		String text;

		if(String.class.isInstance(locatorOrElement))
		{
			text = String.format("Clicking on: %s", locatorOrElement);
		}
		else if(WebElement.class.isInstance(locatorOrElement))
		{
			WebElement element = (WebElement)locatorOrElement;

			String description;
			if(element.getAttribute("id") != null && !element.getAttribute("id").equals(""))
			{
				description = "<" + element.getTagName() + "> with id=" + element.getAttribute("id");
			}
			else
			{
				description = "<" + element.getTagName() + "> with text=" + element.getText();
			}

			text = String.format("Clicking %s", description);
		}
		else
		{
			throw new RuntimeException("The parameter locatorOrElement must be either a String or WebElement instance.");
		}

		LOGGER.trace(text);

		return text;
	}


	/**
	 * Logs a click.
	 * Call always this method before the element is clicked because there may be a StaleElementReferenceException.
	 * */
	private String logClickToLinkToAnotherPage(Object locatorOrElement)
	{
		String text;

		if(String.class.isInstance(locatorOrElement))
		{
			text = String.format("Clicking on link: %s", locatorOrElement);
		}
		else if(WebElement.class.isInstance(locatorOrElement))
		{
			WebElement element = (WebElement)locatorOrElement;

			String description;
			if(element.getAttribute("id") != null && !element.getAttribute("id").equals(""))
			{
				description = "<" + element.getTagName() + "> with id=" + element.getAttribute("id");
			}
			else
			{
				description = "<" + element.getTagName() + "> with text=" + element.getText();
			}

			text = String.format("Clicking on link: %s", description);
		}
		else
		{
			throw new RuntimeException("The parameter locatorOrElement must be either a String or WebElement instance.");
		}

		LOGGER.trace(text);

		return text;
	}


	/**
	 * Logs an input of text on a WebElement.
	 *
	 * If locatorOrElement is a WebElement, this method will try to log its id or the text if has no id.
	 * */
	private String logInputText(Object locatorOrElement, String textToType)
	{
		String text;

		if(String.class.isInstance(locatorOrElement))
		{
			String printableTextToType = textToType.length() > 50? textToType.substring(0, 75) + "..." : textToType;

			text = String.format("Input into %s: %s", locatorOrElement, printableTextToType);
		}
		else if(WebElement.class.isInstance(locatorOrElement))
		{
			String desc;

			WebElement element = (WebElement)locatorOrElement;

			if(element.getAttribute("id") != null && !element.getAttribute("id").equals(""))
			{
				desc = "<" + element.getTagName() + " id='" + element.getAttribute("id") + "'>";
			}
			else
			{
				desc = "<" + element.getTagName() + " css='" + element.getAttribute("class") + "'>";
			}

			text = String.format("Input into %s: %s", desc, textToType);
		}
		else
		{
			throw new RuntimeException("The parameter locatorOrElement must be either a String or WebElement instance.");
		}

		LOGGER.trace(text);

		return text;
	}


	public void findLinkToAnotherPageByCssSelectorAndTextAndClickIt(String cssSelector, String text)
	{
		WebElement link = findElementContainingText(cssSelector, text);
		clickLinkToAnotherPage(link);
	}

}
