package com.jobaline.uiautomation.framework.selenium;

public class SeleniumUtils
{
	public static boolean isFatalSeleniumError(Throwable t)
	{
		String message = t.getMessage().toLowerCase();
		return message.contains("session not started or terminated") ||
				message.contains("error communicating with the remote browser. it may have died") ||
				message.contains("session id is null") ||
				message.contains("no such session") ||
				message.contains("was terminated due to so_timeout");
	}
}
