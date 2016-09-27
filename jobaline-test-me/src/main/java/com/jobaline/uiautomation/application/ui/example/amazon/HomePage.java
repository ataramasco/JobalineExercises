package com.jobaline.uiautomation.application.ui.example.amazon;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.core.ui.BasePage;
import com.jobaline.uiautomation.framework.EnvironmentUtils;
import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

/**
 * ------- Tutorial comments --------
 *
 * This class represents the Amazon Home page: www.amazon.com
 *
 * All of the operations performed on the Amazon home page must be done through this object or through objects that compound this
 * object (for example the SearchForm object). This is a mid layer between the "test layer" and the "selenium layer".
 *
 * Created by damian.j on 3/14/16.
 *
 * ------- End tutorial comments --------
 */
public class HomePage extends BasePage
{
	private SearchForm searchForm;

	private List<SearchResult> searchResults;


	/**
	 * ------- Tutorial comments --------
	 *
	 * Returns the path of the url. Examples:
	 *
	 * http://jobs.jobalineci.com  ----->  the path is: "/"
	 *
	 * http://jobs.jobalineci.com/config/CompetencyGroup?brandId=1346&groupId=2143&  ----->  the path is: "/config/CompetencyGroup"
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override protected String getPath()
	{
		return "/";
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This is one of the "standard page methods". Must be defined in every class descending from BasePage.
	 *
	 * Every test that needs to open this page must call: HomePage.open();
	 *
	 * This must be defined in every page object. Unfortunately, it is not allowed (and is conceptually wrong) to define abstract static methods. If not, this method
	 * would be defined as abstract in BasePage so every subclass must implement it. The reason it is not an instance method is to follow a common test automation
	 * pattern which is basically to use static methods for stateless operation.
	 *
	 * ------- End tutorial comments --------
	 * */
	public static void open()
	{
		/*
		* ------- Tutorial comments --------
		*
		* In general, the default implementation for this method would be:
		*
		* BasePage.open();
		*
		* But our system has "jobs" as the default subdomain so we need to specify the "www" domain.
		*
		* ------- End tutorial comments --------
		* */

		BasePage.open("www");
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This is one of the "standard page methods". Must be defined in every class descending from BasePage.
	 *
	 * Every test that needs to get the url of this page must call: HomePage.getUrl();
	 *
	 * ------- End tutorial comments --------
	 * */
	public static String getUrl()
	{
		/*
		* ------- Tutorial comments --------
		*
		* The following would be in most cases the implementation.
		*
		* ------- End tutorial comments --------
		* */

		return BasePage.getUrl();
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This is one of the "standard page methods". Must be defined in every class descending from BasePage.
	 *
	 * Every test that needs to verify that the current page one the browser is this one, must call: HomePage.verifyLocation();
	 *
	 * ------- End tutorial comments --------
	 * */
	public static void verifyLocation()
	{
		BasePage.verifyLocation(); // This must be the default implementation.
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This is one of the "standard page methods".
	 *
	 * This method is called automatically by BasePage.verifyLocation() and this will be called by HomePage.verifyLocation().
	 *
	 * We override this method only if they mechanism to verify the location in this page must
	 * be different from the default one implemented in BasePage.verifyLocationImpl()
	 *
	 * Here is the same, but we override it for illustration purporses.
	 *
	 * ------- End tutorial comments --------
	 * */
	protected void verifyLocationImpl()
	{
		verifyLocationByCheckingUrlContainsText(EnvironmentUtils.getApplicationUnderTestDomain() + getPath());
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * The "search form" is a component of the home page, so the HomePage instance is the responsible of creating SearchForm instances.
	 *
	 * It will be responsible of interacting with the search form: fill form, submit it, read values from inputs, etc, etc.
	 *
	 * ------- End tutorial comments --------
	 * */
	public SearchForm getSearchForm()
	{
		if(searchForm == null)
		{
			searchForm = new SearchForm(this);
		}

		return searchForm;
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * Returns objects representing the search results displayed in the page.
	 *
	 * We are using javascript to read the items information for performance purposes.
	 *
	 * Selenium API calls are expensive. If we read the items information using the Selenium API, we will be executing a lot of WebDriver commands that need to
	 * travel through a network if we are using RemoteWebDriver.
	 *
	 * When we use an script, there is only one WebDriver command to execute the script and return all the data.
	 *
	 * ------- End tutorial comments --------
	 * */
	public List<SearchResult> getResults()
	{
		if(searchResults == null)
		{
			searchResults = new ArrayList<>();

			String script = ResourceManager.getJavascriptFileContent("js/ui/example/searchResultsReader.js", true);

			JSONObject data = getSeleniumClient().executeScriptToReadData(script);

			JSONArray resultsData = data.getJSONArray("results");

			for(int i = 0; i < resultsData.length(); i++)
			{
				JSONObject resultData = resultsData.getJSONObject(i);
				searchResults.add(new SearchResult(this, resultData));
			}
		}

		return searchResults;
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * Tells whether the page has results or not.
	 *
	 * Note that objects of the UI layer, will never make verifications (that is, have asserts). They will only set data to pages and retrieve data from pages but never will verify that
	 * something is in the page or not. The only reason that this object can throw an exception is if it is asked something that it can not do. For example, set an input that is not
	 * in the page.
	 *
	 * ------- End tutorial comments --------
	 * */
	public boolean hasResults()
	{
		return getSeleniumClient().exists(".a-fixed-left-grid");
	}

}
