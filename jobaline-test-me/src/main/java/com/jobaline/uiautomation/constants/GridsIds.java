package com.jobaline.uiautomation.constants;

import com.jobaline.uiautomation.framework.lang.ListUtils;
import com.jobaline.uiautomation.framework.lang.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridsIds
{
	public static final int TESTERGRID   = 1;

	private static Map<Integer, Integer> gridUrlsIndex;

	private static Map<Integer, List<String>> defaultGridUrls = new MapUtils<Integer, List<String>>().createMap(
		TESTERGRID, ListUtils.createList("http://localhost:4444/wd/hub")
	);


	/**
	 * Return the url of the grid specified.
	 * If the grid has multiple urls, this method will follow a round robin algorithm
	 * */
	public synchronized static String getGridUrl(Integer gridId)
	{
		if(gridUrlsIndex == null)
		{
			gridUrlsIndex = new HashMap<>();

			for(Integer gridIdAux : defaultGridUrls.keySet())
			{
				gridUrlsIndex.put(gridIdAux, -1);
			}
		}

		if(!defaultGridUrls.containsKey(gridId))
		{
			throw new RuntimeException("The grid id: " + gridId + " has not any url set.");
		}

		List<String> urls = defaultGridUrls.get(gridId);

		if(urls.size() == 0)
		{
			throw new RuntimeException("The grid id: " + gridId + " has not any url set.");
		}

		int index = gridUrlsIndex.get(gridId);

		if(index + 1 < urls.size())
		{
			index++;
		}
		else
		{
			index = 0;
		}

		// Uncomment the next line to print the url selected
		System.out.println(index + ": " + urls.get(index));

		gridUrlsIndex.put(gridId, index);

		return urls.get(index);
	}
}
