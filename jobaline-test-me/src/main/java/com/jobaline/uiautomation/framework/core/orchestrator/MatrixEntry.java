package com.jobaline.uiautomation.framework.core.orchestrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damian on 4/23/15.
 */
public class MatrixEntry
{
	private String            testId;
	private List<TestIoEntry> testIoEntries;
	private List<TestIoEntry> testIoEntriesForCrossBrowser;

	private String jsonSource;

	private boolean isParsed = false;


	public MatrixEntry(String jsonSource)
	{
		this.jsonSource = jsonSource;
	}


	private List<TestIoEntry> parseTestIoEntries(JSONObject matrixEntry_json, boolean crossBrowserTestIOEntries)
	{
		if(testId == null)
		{
			throw new RuntimeException("testId must be parsed first");
		}

		String jsonAttrName = !crossBrowserTestIOEntries? "io" : "io-cbt";
		if(matrixEntry_json.has(jsonAttrName))
		{
			List<TestIoEntry> testIoEntries_aux = new ArrayList<>();

			JSONArray testIoEntries_json = matrixEntry_json.getJSONArray(jsonAttrName);

			for(int i = 0; i < testIoEntries_json.length(); i++)
			{
				JSONObject testIOEntry_json = testIoEntries_json.getJSONObject(i);

				TestIoEntry testIoEntry = new TestIoEntry(testId, testIOEntry_json);

				testIoEntries_aux.add(testIoEntry);
			}

			return testIoEntries_aux;
		}
		else
		{
			return null;
		}
	}


	private synchronized void parse()
	{
		if(!isParsed)
		{
			JSONObject matrixEntry_json;

			try
			{
				matrixEntry_json = new JSONObject(jsonSource);
				testId = matrixEntry_json.getString("id");
				testIoEntries = parseTestIoEntries(matrixEntry_json, false);
				testIoEntriesForCrossBrowser = parseTestIoEntries(matrixEntry_json, true);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("Could not parse the test/actor entry from the matrix.");
			}

			isParsed = true;
		}
	}


	public String getTestId()
	{
		parse();
		return testId;
	}


	public List<TestIoEntry> getTestIoEntries()
	{
		parse();
		return testIoEntries;
	}


	public List<TestIoEntry> getTestIoEntriesForCrossBrowser()
	{
		parse();
		return testIoEntriesForCrossBrowser;
	}


	/**
	 * Returns a TestIoEntry that creates the entity specified by parameter or null if the test/actor of this entry does not create the entity specified.
	 * */
	public TestIoEntry getTestIoEntryThatCreatesEntity(String outputEntityTypeName)
	{
		TestIoEntry testIoEntry = null;
		if(jsonSource.contains(outputEntityTypeName)) // fast check, it still does not assure that this actor/test creates the entity
		{
			for(TestIoEntry testIoEntry_aux : getTestIoEntries())
			{
				if(testIoEntry_aux.hasOutputEntities() && testIoEntry_aux.getOutputEntities().contains(outputEntityTypeName))
				{
					testIoEntry = testIoEntry_aux;
					break;
				}
			}
		}

		return testIoEntry;
	}


	public String getJsonSource()
	{
		return jsonSource;
	}
}
