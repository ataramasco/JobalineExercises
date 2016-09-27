package com.jobaline.uiautomation.framework.testng;

import com.jobaline.uiautomation.framework.lang.MethodComparator;
import com.jobaline.uiautomation.framework.testng.exception.FailException;
import org.testng.*;
import org.testng.annotations.AfterMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Listener process test methods annotations. 
 *
 * It will be called for test methods and configuration methods (for example, those annotate with @BeforeMethod).
 * Must only operate with test methods.
 *
 * If some test annotation throws a SkipException, the test will not run but so the configuration methods.
 * Maybe you want to avoid executing the configuration methods (for example, if the some initialization method that opens
 * the browser) for the sake of efficiency. It is not easy to do automatically here because TestNG
 * first invoke this listener for configuration methods and at that stage is not easy to predict what will happen
 * when this listener process the annotations. But you can add an "if" in your configuration
 * methods to do nothing when these are invoked: this class provides a static method (TestMethodsListener.willSkipMethod())
 * that tells you if a test method, with certain parameters will be skipped. See BaseSeleniumTest.initTest() for an example
 * on the use of this method.
 * */
public class TestMethodsListener implements IInvokedMethodListener2
{
	/**
	 * This method can be called to execute the methods annotated with AfterMethod in the test class instance returned by 
	 * iTestResult.getInstance(). The annotation must contain alwaysRun==true.
	 * I (damian) had to implement it because if afterInvocation() throw a SkipException, AfterMethods won't be invoked. There
	 * are some posts about it but no official answers, just custom solutions like mine. See for example:
	 * https://groups.google.com/forum/#!topic/testng-users/XSb0yiAMCpU
	 * */
	private static void executeAfterMethodsProgrammatically(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)
	{
		// Get all the methods of Test class (a subclass of BaseSeleniumTest or BaseGeneralTest) and select AfterMethods with alwaysRun==true
		List<Method> methods_aux = Arrays.asList(iTestResult.getInstance().getClass().getMethods());
		List<Method> methods = new ArrayList<>();
		methods_aux.stream().filter(aMethods_aux -> aMethods_aux.isAnnotationPresent(AfterMethod.class)).forEach(aMethods_aux -> {
			AfterMethod annotation = aMethods_aux.getAnnotation(AfterMethod.class);
			if(annotation.alwaysRun())
			{
				methods.add(aMethods_aux);
			}
		});

		// Sorting the methods by name
		Collections.sort(methods, new MethodComparator());

		// Now execute all AfterMethods
		for(Method m : methods)
		{
			try
			{
				m.invoke(iTestResult.getInstance(), iTestResult, iInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod(), iTestResult.getParameters());
			}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
	}


	/**
	 * Execute all methods marked as BeforeInvocationMethod. This is a concept created by me (damian).
	 * A BeforeInvocationMethod is a method of BaseSeleniumTest or BaseGeneralTest  subclasses that must execute after all methods
	 * marked as BeforeMethod but before the test.
	 * */
	private static void executeBeforeInvocationMethods(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)
	{
		// Get all the methods of Test class (a subclass of BaseSeleniumTest or BaseGeneralTest) and select BeforeInvocationMethods
		List<Method> methods_aux = Arrays.asList(iTestResult.getInstance().getClass().getMethods());
		List<Method> methods = methods_aux.stream().filter(aMethods_aux -> aMethods_aux.getName().startsWith("beforeInvocation")).collect(Collectors.toList());

		// Sorting the methods by name
		Collections.sort(methods, new MethodComparator());

		// Now execute all BeforeInvocationMethods
		for(Method m : methods)
		{
			try
			{
				m.invoke(iTestResult.getInstance(), iInvokedMethod, iTestResult, iTestContext);
			}
			catch(Exception e)
			{
				if(e.getCause() instanceof SkipException)
				{
					if(e.getCause() instanceof FailException)
						throw (FailException)e.getCause();
					else
						throw (SkipException)e.getCause();
				}
				else
				{
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * Execute all methods marked as AfterInvocationMethod. This is a concept created by me (damian).
	 * An AfterInvocationMethod method is a method of BaseSeleniumTest or BaseGeneralTest subclasses that must execute after the test
	 * but before all methods marked as AfterMethod.
	 * */
	private static void executeAfterInvocationMethods(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)
	{
		// Get all the methods of Test class (a subclass of BaseSeleniumTest or BaseGeneralTest) and select AfterInvocationMethods
		List<Method> methods_aux = Arrays.asList(iTestResult.getInstance().getClass().getMethods());
		List<Method> methods = methods_aux.stream().filter(aMethods_aux -> aMethods_aux.getName().startsWith("afterInvocation")).collect(Collectors.toList());

		// Sorting the methods by name
		Collections.sort(methods, new MethodComparator());

		// Now execute all AfterInvocationMethods
		for(Method m : methods)
		{
			try
			{
				m.invoke(iTestResult.getInstance(), iInvokedMethod, iTestResult, iTestContext);
			}
			catch(Exception e)
			{
				if(e.getCause() instanceof SkipException)
				{
					// TestNG won't run AfterMethods, I don't know why, so I must run it by myself. See the comments in 
					// executeAfterMethodsProgrammatically() to know more. Before I must set the proper state because
					// TestNG will change the state after, when the exception is thrown.

					if(e.getCause() instanceof FailException)
						iTestResult.setStatus(ITestResult.FAILURE);
					else
						iTestResult.setStatus(ITestResult.SKIP);

					Reporter.setCurrentTestResult(iTestResult);

					executeAfterMethodsProgrammatically(iInvokedMethod, iTestResult, iTestContext);

					if(e.getCause() instanceof FailException)
						throw (FailException)e.getCause();
					else
						throw (SkipException)e.getCause();
				}
				else
				{
					e.printStackTrace();
				}
			}
		}
	}


	public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)
	{
		// Following I'm checking the method invoked is a test. I can't use iInvokedMethod.isConfigurationMethod() because it is 
		// returning false for BeforeMethods and AfterMethods and it shouldn't!!
		if(!iInvokedMethod.isTestMethod())
		{
			return;
		}

		executeBeforeInvocationMethods(iInvokedMethod, iTestResult, iTestContext);
	}


	public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)
	{
		// Following I'm checking the method invoked is a test. I can't use iInvokedMethod.isConfigurationMethod() because it is 
		// returning false for BeforeMethods and AfterMethods and it shouldn't!!
		if(!iInvokedMethod.isTestMethod())
		{
			return;
		}

		/**
		 * Next will execute AfterInvocationMethods.
		 * Those are methods of the BaseSeleniumTest or BaseGeneralTest classes that must execute after the test and before than
		 * AfterMethods.
		 * */
		executeAfterInvocationMethods(iInvokedMethod, iTestResult, iTestContext);
	}


	public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
	{
		throw new RuntimeException("Should not call this method, should call beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)");
	}


	public void afterInvocation(IInvokedMethod method, ITestResult testResult)
	{
		throw new RuntimeException("Should not call this method, should call afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult, ITestContext iTestContext)");
	}

}
