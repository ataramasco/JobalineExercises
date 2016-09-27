package com.jobaline.uiautomation.framework.lang;

import java.util.*;

/**
 * Created by damian on 10/17/14.
 */
public class MapUtils<T1,T2>
{
	public static synchronized Map<Object, Object> createObjectObjectMap(Object... args)
	{
		if(args.length % 2 != 0)
		{
			throw new RuntimeException("The number of parameters must be even.");
		}

		Map<Object, Object> map= new HashMap<>();

		for(int i= 0; i < args.length; i+=2)
		{
			map.put(args[i], args[i+1]);
		}

		return map;
	}


	public static synchronized Map<String, Object> createStringObjectMap(Object... args)
	{
		if(args.length % 2 != 0)
		{
			throw new RuntimeException("The number of parameters must be even.");
		}

		Map<String, Object> map= new HashMap<>();

		for(int i= 0; i < args.length; i+=2)
		{
			map.put((String)args[i], args[i+1]);
		}

		return map;
	}


	public static synchronized Map<String, String> createStringStringMap(String... args)
	{
		if(args.length % 2 != 0)
		{
			throw new RuntimeException("The number of parameters must be even.");
		}

		Map<String, String> map= new HashMap<>();

		for(int i= 0; i < args.length; i+=2)
		{
			map.put(args[i], args[i+1]);
		}

		return map;
	}


	public static synchronized Map<Integer, String> createIntegerStringMap(Object... args)
	{
		if(args.length % 2 != 0)
		{
			throw new RuntimeException("The number of parameters must be even.");
		}

		Map<Integer, String> map= new HashMap<>();

		for(int i= 0; i < args.length; i+=2)
		{
			map.put((Integer)args[i], (String)args[i+1]);
		}

		return map;
	}


	public static synchronized <T3, T4> Map<T3, T4> reverse(Map<T3, T4> map)
	{
		Map<T3, T4> reverseMap= new LinkedHashMap<>();

		ListIterator<Map.Entry<T3, T4>> iterator = new ArrayList<>(map.entrySet()).listIterator(map.size());

		while(iterator.hasPrevious())
		{
			Map.Entry<T3, T4> entry = iterator.previous();
			reverseMap.put(entry.getKey(), entry.getValue());
		}

		return reverseMap;
	}


	public static synchronized <T3, T4> T3 getRandomKey(Map<T3, T4> map)
	{
		return new ArrayList<>(map.keySet()).get((int)(Math.random() * map.size()));
	}


	public static synchronized <T3, T4> T4 getRandomValue(Map<T3, T4> map)
	{
		return new ArrayList<>(map.values()).get((int)(Math.random() * map.size()));
	}


	public synchronized Map<T1, T2> createMap(Object... args)
	{
		if(args.length % 2 != 0)
		{
			throw new RuntimeException("The number of parameters must be even.");
		}

		Map<T1, T2> map= new HashMap<>();

		for(int i= 0; i < args.length; i+=2)
		{
			map.put((T1)args[i], (T2)args[i+1]);
		}

		return map;
	}


}
