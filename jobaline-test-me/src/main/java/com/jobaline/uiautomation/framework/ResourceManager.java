package com.jobaline.uiautomation.framework;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by damian on 3/31/15.
 */
public class ResourceManager
{
	/**
	 * @param name must be relative to the default directory of the Java process
	 * */
	public static URI getResourceURI(String name)
	{
		URI uri;
		try
		{
			uri = ResourceManager.class.getClassLoader().getResource(name).toURI();
		}
		catch(NullPointerException e)
		{
			throw new RuntimeException("Could not get the URI of the file '" + name + "'. It was not found.");
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Could not get the URI of the file '" + name + "'.");
		}

		return uri;
	}


	/**
	 * @param name must be relative to the default directory of the Java process
	 * */
	public static String getResourceContent(String name)
	{
		String content;

		try
		{
			File file = new File(getResourceURI(name));
			content = FileUtils.readFileToString(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Could not read the content of the file '" + name + "'.");
		}

		return content;
	}


	/**
	 * @param name must be relative to the default directory of the Java process
	 * */
	public static String getResourceAbsolutePath(String name)
	{
		return getResourceURI(name).getPath().replace("/C:", "C:");
	}


	private static String convertListToJSArray(List<?> list)
	{
		String jsArray;
		if(list == null)
		{
			jsArray = "null";
		}
		else
		{
			jsArray = "[";
			for(int i = 0; i < list.size(); i++)
			{
				if(i != 0)
				{
					jsArray += ",";
				}

				Object arg = list.get(i);
				if(arg == null)
				{
					jsArray += "null";
				}
				else if(JSONObject.class.isAssignableFrom(arg.getClass()) || JSONArray.class.isAssignableFrom(arg.getClass()))
				{
					jsArray += JSONObject.quote(arg.toString());
				}
				else if(String.class.isAssignableFrom(arg.getClass()))
				{
					jsArray += "\"" + arg + "\"";
				}
				else if(Number.class.isAssignableFrom(arg.getClass()) || Boolean.class.isAssignableFrom(arg.getClass()))
				{
					jsArray += arg;
				}
				else if(List.class.isAssignableFrom(arg.getClass()))
				{
					jsArray += convertListToJSArray((List<?>)arg);
				}
				else
				{
					throw new RuntimeException("The argument to be passed as an argument to the script must be a null, String, Number or Boolean or a List containing the previous types. The following type is not allowed: " + arg.getClass());
				}
			}

			jsArray += "]";
		}

		return jsArray;
	}


	public static String getJavascriptFileContent(String name, boolean minimization)
	{
		return getJavascriptFileContent(name, minimization, null);
	}


	public static String getJavascriptFileContent(String name, boolean minimization, List<?> args)
	{
		String content = getResourceContent(name);

		if(minimization)
		{
			// TODO use some minimization tool like Closure Compiler
			// Until we start using a minimization tool, let's remove the first comment for now (it's a starting :)).
			if(content.startsWith("/*"))
			{
				content = content.substring(content.indexOf("*/") + 2);
			}

			content = content.replaceAll("\\r|\\n|\\t", "");
		}

		String args_str = "var args = " + convertListToJSArray(args) + ";";

		content = "(function(){" + args_str + content + "}).call();";

		return content;
	}

}
