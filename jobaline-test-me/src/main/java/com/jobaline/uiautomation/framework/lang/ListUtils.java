package com.jobaline.uiautomation.framework.lang;

import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Class StringUtils.
 * User: damian
 * Date: 29/09/13 8:00
 * To change this template use File | Settings | File Templates.
 */
public class ListUtils
{

	public static synchronized <T> List<T> createList(T... args)
	{
		List<T> list = new ArrayList<>();

		Collections.addAll(list, args);

		return list;
	}


	public static synchronized <T> T getElementRandomly(List<T> list)
	{
		return list.get((int)(Math.random() * list.size()));
	}


	/**
	 * Select n elements randomly from a list and return a new list with those elements.
	 * Note that the list size must be greater than n.
	 * */
	public static <T> List<T> getElementsRandomly(List<T> list, int n)
	{
		List<T> newList = new ArrayList<>();
		for(int i = 0; i < n; i++)
		{
			int index = (int)(Math.random() * list.size());
			newList.add(list.get(index));
			list.remove(index);
		}
		return newList;
	}


	public static <T> List<T> union(List<T>... lists)
	{
		List<T> result = new ArrayList<>();

		for(List<T> list : lists)
		{
			if(list != null)
			{
				result.addAll(list);
			}
		}

		return result;
	}


	public static <T> boolean hasRepeated(List<T> list)
	{
		boolean result = false;

		if(list.size() > 1)
		{
			Map<T, T> aux = new HashMap<>();
			for(T element : list)
			{
				if(aux.containsKey(element))
				{
					result = true;
					break;
				}

				aux.put(element, element);
			}
		}

		return result;
	}

}
