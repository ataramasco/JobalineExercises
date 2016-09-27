package com.jobaline.uiautomation.tests.example;

import com.jobaline.uiautomation.application.ui.example.amazon.ArticlePage;
import org.testng.Assert;

/**
 * ------- Exercise comments --------
 *
 * This is the test implementation class for the exercise two, the counterpart of ArticleSmokeTest.
 *
 * ------- End exercise comments --------
 * */
public class ArticleSmokeTestSteps
{


    /* Open the article page */
	public void openArticlePage()
	{
		ArticlePage.open();
		ArticlePage.verifyLocation();
	}

    /* Click the photo gallery */
	public void openGalleryImages() {
		ArticlePage page = new ArticlePage();
		page.clickImageGallery();
	}

    /* Verify if the photo gallery is open */
	public void verifyPageContainsResults()
	{
		ArticlePage page = new ArticlePage();
		Assert.assertTrue(page.hasResults(), "After opening the article gallery images that is expected to return results, the page does not have results.");
	}


}
