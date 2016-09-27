package com.jobaline.uiautomation.tests.example;

import com.jobaline.uiautomation.framework.testng.BaseSeleniumTestV2;
import org.json.JSONObject;
import org.testng.annotations.Test;


public class ArticleSmokeTest extends BaseSeleniumTestV2
{
	/* Test steps class definition */
	private ArticleSmokeTestSteps steps;

	/* Obtain the TestId */
	@Override protected String getTestId()
	{
		return "example.ArticleSmokeTest";
	}


	/* Input data for the test */
	@Override protected void setData(JSONObject data){

		steps = new ArticleSmokeTestSteps();
	}

	/* Test */
	@Test(groups = { "example.article" })
	public void test()
	{
		steps.openArticlePage();
		steps.openGalleryImages();
		steps.verifyPageContainsResults();
	}


}
