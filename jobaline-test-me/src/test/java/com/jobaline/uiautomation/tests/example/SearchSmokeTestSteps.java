package com.jobaline.uiautomation.tests.example;

import com.jobaline.uiautomation.application.ui.example.amazon.HomePage;
import com.jobaline.uiautomation.application.ui.example.amazon.SearchForm;
import com.jobaline.uiautomation.application.ui.example.amazon.SearchResult;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.List;

/**
 * ------- Tutorial comments --------
 *
 * This is the test implementation class, the counterpart of SearchSmokeTest.
 *
 * This class will operate with:
 *
 *   - Workflow objects: implement reusable workflows
 *   - Test service objects: implement reusable test steps.
 *   - The "UI " layer: page objects, page components, form objects etc.
 *
 * This class should never access the selenium layer directly.
 *
 * ------- End tutorial comments --------
 * */
public class SearchSmokeTestSteps
{
	private JSONObject searchFormData;


	public SearchSmokeTestSteps(JSONObject data)
	{
		searchFormData = data.getJSONObject("searchFormData");
	}


	public void openSearchPage()
	{
		HomePage.open();
		HomePage.verifyLocation();
	}


	public void doSearch()
	{
		SearchForm searchForm = new HomePage().getSearchForm();
		searchForm.fillForm(searchFormData);
		searchForm.submitFormSuccessfullyAndWaitForPageToLoad();

		HomePage.verifyLocation();
	}


	public void verifyPageContainsResults()
	{
		/**
		 * ------- Tutorial comments --------
		 *
		 * Note that the following code is asserting that the page has results by asking to the HomePage object.
		 *
		 * We are here in the test layer while the HomePage object is part of the UI layer. We always follow the next standard:
		 *
		 *  - Asserts are always in the test layer. The test layer is responsible to verify that the expectations of the test are met.
		 *  - Representation of pages are always in the UI layer. The UI layer has objects that operate with pages: retrieve information from pages, set information to
		 *    forms, perform clicks on objects, etc. They never know what test are using them, nor why, nor the test expectations.
		 *  - The test layer make asserts based on information retrieved by objects of the UI layer. They never access the selenium layer directly.
		 *  - Objects of the UI layer never make assertions. They provide information to tests so they make them.
		 *
		 * It does not mean that the objects of the UI layer don't make aaaaany kind of verification. They may make "low level" verifications. For example, if a page object
		 * is asked for some information, they assume that the element containing that information is present in the page and if not will throw an exception. But
		 * this is only because they are asked to do something that they can not do and conceptually, this is an error.
		 *
		 * And... what happens when a test must work with some information that is optional? this is, it may or may not be in the page. The test is responsible to make sure
		 * that the information is in the page before asking for it (to avoid the page object throw an Exception). Of course, the test will ask to the page object who know
		 * how to interact with the page. For example, suppose that for some reason the title is optional and we want to assert its length in case it is present. Then, we
		 * would do something like this:
		 *
		 * if(result.hasTitle)
		 * {
		 *     Assert.assertTrue(result.getTitle().length <= 150, "The title is longer than 150 characters.");
		 * }
		 *
		 * In practice, the previous case should be a very rare case: the test should know exactly what are the results expected and what data do they have.
		 *
		 * Finally, note that a test will never have code like the following:
		 *
		 * page.verifyContainsResults();
		 *
		 * ------- End tutorial comments --------
		 * */

		HomePage page = new HomePage();
		Assert.assertTrue(page.hasResults(), "After performing a search on the home page that is expected to return results, the page does not have results.");
	}


	public void verifyResultsContainsRequiredData()
	{
		HomePage page = new HomePage();

		List<SearchResult> results = page.getResults();

		for(SearchResult result : results)
		{
			Assert.assertTrue(result.hasTitle(), "After performing a search on the home page, there is an item without title.");
			Assert.assertTrue(result.hasPrice(), "After performing a search on the home page, there is an item without price. Item title: " + result.getTitle());
		}
	}

}
