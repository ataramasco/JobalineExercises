package com.jobaline.uiautomation.framework.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by damian on 8/16/2015.
 */
public class FrameworkLoggerFactory
{
	public static int LOGGER_SELENIUM_CLIENT = 0;
	public static int LOGGER_DYNAMODB = 1;
	public static int LOGGER_CASPERJS = 2;


	public static Logger getLogger(int loggerId)
	{
		if(loggerId == LOGGER_SELENIUM_CLIENT)
		{
			return LoggerFactory.getLogger("framework.SeleniumClient");
		}
		else if(loggerId == LOGGER_DYNAMODB)
		{
			return LoggerFactory.getLogger("framework.DynamoDB");
		}
		else if(loggerId == LOGGER_CASPERJS)
		{
			return LoggerFactory.getLogger("framework.CasperJS");
		}
		else
		{
			throw new RuntimeException(String.format("The logger '%d' is not defined", loggerId));
		}
	}

}
