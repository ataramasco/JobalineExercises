package com.jobaline.uiautomation.framework.core.orchestrator.actor.dataDependencyResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jobaline.uiautomation.framework.EnvironmentUtils;
import com.jobaline.uiautomation.framework.core.orchestrator.DataDependency;
import com.jobaline.uiautomation.framework.core.orchestrator.DynamoJSONEncoder;
import com.jobaline.uiautomation.framework.core.orchestrator.database.TestDatabaseFactory;
import com.jobaline.uiautomation.framework.generators.IdGenerator;

/**
 * Created by damian on 4/23/2015.
 */
public class DataDependencyResolverActor implements IDataDependencyResolverActor
{
	private String generateUniqueFakePhoneNumber()
	{
		int lastDigits = (int)(Math.random() * 10000);
		if(lastDigits < 10)
		{
			return "(425) 654-000" + lastDigits;
		}
		else if(lastDigits < 100)
		{
			return "(425) 654-00" + lastDigits;
		}
		else if(lastDigits < 1000)
		{
			return "(425) 654-0" + lastDigits;
		}
		else
		{
			return "(425) 654-" + lastDigits;
		}
	}


	private abstract class PlaceHolderValueGenerator
	{
		public abstract String generateValue();
	}


	private String replacePlaceholder(String jsonSource, String placeHolderName, PlaceHolderValueGenerator generator)
	{
		String placeHolder1RegEx = String.format("\\[%s\\]", placeHolderName);
		String placeHolder1MatcherRegEx = String.format(".*\\[%s\\].*", placeHolderName);

		String placeHolder2RegEx = String.format(".*\\[%s_\\d+\\].*", placeHolderName);
		String placeHolder2MatcherRegEx = String.format("\\[%s_\\d+\\]", placeHolderName);

		while(jsonSource.matches(placeHolder1MatcherRegEx))
		{
			String value = generator.generateValue();
			jsonSource = jsonSource.replaceFirst(placeHolder1RegEx, value);
		}

		if(jsonSource.matches(placeHolder2RegEx))
		{
			List<String> placeholders = new ArrayList<>();
			Matcher m = Pattern.compile(placeHolder2MatcherRegEx).matcher(jsonSource);
			while(m.find())
			{
				placeholders.add(m.group());
			}

			String current = null;
			String randomId = null;
			for(String placeholder : placeholders)
			{
				if(!placeholder.equals(current))
				{
					current = placeholder;
					randomId = generator.generateValue();
				}

				jsonSource = jsonSource.replace(current, randomId);
			}
		}

		return jsonSource;
	}


	private String replacePlaceHolders(String jsonSource)
	{
		jsonSource = replacePlaceholder(jsonSource, "random_id", new PlaceHolderValueGenerator()
		{
			@Override public String generateValue()
			{
				return IdGenerator.generate();
			}
		});

		jsonSource = replacePlaceholder(jsonSource, "random_id_tiny", new PlaceHolderValueGenerator()
		{
			@Override public String generateValue()
			{
				return IdGenerator.generateTiny();
			}
		});

		jsonSource = replacePlaceholder(jsonSource, "random_id_short", new PlaceHolderValueGenerator()
		{
			@Override public String generateValue()
			{
				return IdGenerator.generateShort();
			}
		});

		jsonSource = replacePlaceholder(jsonSource, "random_id_only_letters_short", new PlaceHolderValueGenerator()
		{
			@Override public String generateValue()
			{
				return IdGenerator.generateShort();
			}
		});

		jsonSource = replacePlaceholder(jsonSource, "unique_fake_number", new PlaceHolderValueGenerator()
		{
			@Override public String generateValue()
			{
				return generateUniqueFakePhoneNumber();
			}
		});

		jsonSource = replacePlaceholder(jsonSource, "aut_domain", new PlaceHolderValueGenerator()
		{
			String stagingHost = null;


			@Override public String generateValue()
			{
				if(stagingHost == null)
				{
					stagingHost = EnvironmentUtils.getApplicationUnderTestDomain();
					if(stagingHost.startsWith("."))
					{
						stagingHost = stagingHost.substring(1);
					}
				}

				return stagingHost;
			}
		});

		return jsonSource;
	}


	public JSONObject getDataDictionaryFromDictionariesDatabase(String dependencyId)
	{
		return TestDatabaseFactory.instance().getDictionary(dependencyId);
	}


	@Override public JSONObject resolveDependencies(List<DataDependency> dataDependencies)
	{
		JSONObject dataDictionaries = new JSONObject();
		for(DataDependency dataDependency : dataDependencies)
		{
			String testParameterName = dataDependency.getTestParameterName();

			String json_source;

			if(!dataDependency.isReferenceToDictionariesDatabase())
			{
				json_source = dataDependency.getDataDictionary();
			}
			else
			{
				String dependencyId = dataDependency.getDependencyId();

				if(testParameterName == null || testParameterName.isEmpty() || dependencyId == null || dependencyId.isEmpty())
				{
					throw new RuntimeException("The data dependency is invalid, the test parameter name and/or the dependency id are empties");
				}

				json_source = getDataDictionaryFromDictionariesDatabase(dependencyId).toString();
			}

			json_source = DynamoJSONEncoder.decode(json_source);

			Object object;

			if(json_source.trim().startsWith("{"))
			{
				object = new JSONObject(json_source);
			}
			else
			{
				object = new JSONArray(json_source);
			}

			try
			{
				dataDictionaries.put(testParameterName, object);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				throw new RuntimeException("Could add the data dictionary to the list."); // Shouldn't be any reason here to have this exception, testParameterName is not empty, can't contain characters not allowed by json (because it comes from DynamoDB) and jsonObject contains a valid json object.
			}
		}

		dataDictionaries = new JSONObject(replacePlaceHolders(dataDictionaries.toString()));

		return dataDictionaries;
	}
}
