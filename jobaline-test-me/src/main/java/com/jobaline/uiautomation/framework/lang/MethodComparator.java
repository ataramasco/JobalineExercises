package com.jobaline.uiautomation.framework.lang;

import java.lang.reflect.Method;
import java.util.Comparator;

public class MethodComparator implements Comparator<Method>
{

	public int compare(Method method1, Method method2)
	{
		return method1.getName().compareTo(method2.getName());
	}

}
