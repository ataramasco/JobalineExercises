package com.jobaline.uiautomation.application.ui.example.amazon;

import com.jobaline.uiautomation.framework.core.ui.BasePage;
import org.openqa.selenium.By;

public class ArticlePage extends BasePage
{

	@Override protected String getPath()
	{
		return "/";
	}


	public static void open()
	{
		BasePage.open("www");
	}


	public static String getUrl()
	{
		return BasePage.getUrl();
	}


	public static void verifyLocation()
	{
		BasePage.verifyLocation(); // This must be the default implementation.
	}


	public void clickImageGallery(){
		getSeleniumClient().findElement(By.id("landingImage")).click();
	}


	public boolean hasResults()
	{
		if(getSeleniumClient().findElement(By.className("a-popover-inner")) != null)
			return true;
		else
			return false;
	}


}
