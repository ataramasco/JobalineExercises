package com.jobaline.uiautomation.framework.testng;

import com.jobaline.uiautomation.framework.selenium.IHasAccessToSelenium;
import com.jobaline.uiautomation.framework.selenium.SeleniumWrapper;

import java.lang.reflect.Method;

/**
 * Created by damian on 5/25/2015.
 */
public class SeleniumClientProxy
{

	public static void checkIfGetSeleniumInvokerHasAccessToSelenium()
	{
		/**
		 * This method restricts what classes and methods can get the Selenium client.
		 * It must be either:
		 *   - Any descendant of this class.
		 *   - Any anonymous class implemented inside any descendant of this class.
		 *   - Any class implementing the interface IHasAccessToSelenium that accesses the Selenium client from the implementation of IHasAccessToSelenium.getSeleniumClient().
		 *
		 * Note that if the anonymous class is implemented inside a class implementing IHasAccessToSelenium but that is not
		 * a descendant of this class, it will not have access to the selenium client.
		 * */

		boolean hasAccessToSelenium = false;

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		// At this point, the stackTrace array contains the next entries:
		//   0: java.lang.Thread.getStackTrace()
		//   1: This method
		//   2: The method calling this method
		//   3: The method that called SeleniumClientProxy.getSeleniumClient()

		String nameOfClassCalling = stackTrace[3].getClassName();

		if(nameOfClassCalling.contains("$")) // This would be the case of an anonymous class
		{
			nameOfClassCalling = nameOfClassCalling.substring(0, nameOfClassCalling.indexOf('$'));
		}

		try
		{
			Class classCalling = Class.forName(nameOfClassCalling);

			if(BaseSeleniumTestV2.class.isAssignableFrom(classCalling))
			{
				hasAccessToSelenium = true;
			}
			else
			{
				String nameOfMethodCalling = stackTrace[3].getMethodName();

				mainLoop:
				for(Class c : classCalling.getInterfaces())
				{
					if(c.getName().equals(IHasAccessToSelenium.class.getName()))
					{
						for(Method m : IHasAccessToSelenium.class.getMethods())
						{
							if(m.getName().equals(nameOfMethodCalling))
							{
								hasAccessToSelenium = true;
								break mainLoop;
							}
						}
					}
				}
			}
		}
		catch(ClassNotFoundException | SecurityException e)
		{
			System.out.println("An exception occurred while trying to check what method is trying to access the Selenium client. Please fix it.");
			e.printStackTrace();
		}

		if(!hasAccessToSelenium)
		{
			throw new RuntimeException(String.format("The class %s does not have access to the Selenium client. Only a class descending from %s or implementing the interface %s has access to the Selenium client and must access to it using the implemented method of the interface.", nameOfClassCalling, BaseSeleniumTestV2.class.getName(), IHasAccessToSelenium.class.getName()));
		}
	}


	public static SeleniumWrapper getSeleniumClient()
	{
		checkIfGetSeleniumInvokerHasAccessToSelenium();

		if(BaseSeleniumTestV2.getInstance() != null)
		{
			return BaseSeleniumTestV2.getInstance().getSeleniumClient();
		}
		else
		{
			throw new RuntimeException("Can not get the selenium client because could not get the test instance.");
		}
	}
}
