package com.jobaline.uiautomation.framework.testng;

public class TestMethodUtils
{
	
	/**
	 * Creates an identifier that will be helpful to be used for different purposes.
	 * 
	 * Remember that what we call "test", is actually a combination of a test method and a browser in the case of Selenium tests. This is, in test methos of subclasses of 
	 * BaseSeleniumTest.
	 * 
	 * For each browser, our infrastructure will create a new instance of the BaseSeleniumTest class (See BaseSeleniumTest.create()).
	 * 
	 * But we could also specify through a maven property twice or more times the same browser. Our infra will create different BaseSeleniumTest instances
	 * no matter that the browser is the same.
	 * 
	 * For example, if we want to run testA and testB of the class testClass in: firefox, firefox, chrome. Then, our infra will create 3 instances of testClass
	 * and TestNG will run 6 tests:
	 * 
	 * 		testA  firefox  testClass instance 1
	 * 		testB  firefox  testClass instance 1
	 * 		testA  firefox  testClass instance 2
	 * 		testB  firefox  testClass instance 2
	 * 		testA  chrome   testClass instance 3
	 * 		testB  chrome   testClass instance 4
	 *  
	 * For this reason, we will use the test method name and the test instance hash to create the identifier.
	 *
	 * TODO include method parameters to create an identifier. Must be careful because I don't know if tests methods are called using the same instances as parameter. For example, let suppose that the test receives an Employer instance as parameter, I don't know if the test will be called with the same Employer instance when it is going to be retried.
	 * */
	public static synchronized String getMethodIdentifier(String testMethodName, BaseTest testInstance)
	{
		return testMethodName + testInstance.hashCode();
	}
	
}
