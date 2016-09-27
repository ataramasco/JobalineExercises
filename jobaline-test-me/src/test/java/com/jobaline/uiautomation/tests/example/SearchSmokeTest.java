package com.jobaline.uiautomation.tests.example;

import com.jobaline.uiautomation.framework.testng.BaseSeleniumTestV2;
import org.json.JSONObject;
import org.testng.annotations.Test;

/**
 * ------- Tutorial comments --------
 *
 * For each end to end automated test there are 2 main classes:
 *
 *  - The "test class" or "test definition class" that defines the test
 *  - The "test steps class" or "test implementation class" that implements the steps of the test.
 *
 * This is the test class. SearchSmokeTestSteps is the test implementation class.
 *
 * It must always extend from BaseSeleniumTestV2 and implement its abstract method.
 *
 * There will be a TestNG test method (see the test() method) that will only make calls to the "test steps" class.
 *
 * We never use the same class for 2 different tests. Each test has its own class. We never have a "test steps" for 2 different test
 * classes (unless some very rare case).
 *
 * We never use more than 1 test method in a test class unless we are forced to do this. If so, there will be something like:
 *
 *    @Test(groups = { "example.search" })
 *    public void test(){ }
 *
 *    @Test(groups = { "example.search" }, dependsOnMethods = { "testPart1" })
 *    public void testPart2(){ }
 *
 * If we have more than 1 test method, they need to be executed sequentially (this is forced by the dependsOnMethods) since they both will have access to the same
 * selenium connection and they will interfere each other. Furthermore, by nature, almost always tests steps are sequentially. For example, we first post a job and then
 * apply to it.
 *
 * ------- End tutorial comments --------
 * */
public class SearchSmokeTest extends BaseSeleniumTestV2
{
	private SearchSmokeTestSteps steps;


	/**
	 * ------- Tutorial comments --------
	 *
	 * The id of the test is used to find the input dictionaries of the test in the "matrix". The matrix is a database that declares what are the inputs of the tests.
	 *
	 * There are 2 different matrix implementations, this is, 2 different data sources of tests inputs dictionaries:
	 *
	 *   - A distributed one implemented using DynamoDB. To have the tests using this, must specify the parameter tests.testdatabase=2
	 *   - A local one implemented using local files. To have the tests using this, must specify the parameter tests.testdatabase=1
	 *
	 * When the local data source is used, the test input definition can be place in any json file under the folder resources/testsDatabase/matrix.
	 *
	 * The test definition for this one can be found in resources/testsDatabase/matrix/example.json
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override protected String getTestId()
	{
		return "example.SearchSmokeTest";
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * The parameter received by this method is one of the inputs dictionaries.
	 *
	 * This test runs with 2 different input dictionaries (see the json file with the test inputs definitions). Then, our test framework will create 2 instances of this class
	 * and set the inputs respectively using this method.
	 *
	 * Conceptually, the test framework will do something like this:
	 *
	 * List<JSONObject> testInputs = matrix.getTestInputs(testId);
	 * for(JSONObject testInputs : testInputs)
	 * {
	 *    SearchSmokeTest testInstance = new SearchSmokeTest();
	 *    testInstance.setData(testInput);
	 *    testInstance.run();
	 * }
	 *
	 * Note that the previous code is just for illustrating process.
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override protected void setData(JSONObject data)
	{
		steps = new SearchSmokeTestSteps(data);
	}


	@Test(groups = { "example.search" })
	public void test()
	{
		steps.openSearchPage();
		steps.doSearch();
		steps.verifyPageContainsResults();
		steps.verifyResultsContainsRequiredData();
	}

}
