package com.jobaline.uiautomation.framework.util;

import org.apache.commons.lang.StringUtils;

public class URLUtils
{
	/**
	 * The URL must be in the standard format, not in SEO friendly format.
	 * */
	public static boolean hasParameter(String url, String parameter)
	{
		return url.contains(String.format("&%s=", parameter)) || url.contains(String.format("?%s=", parameter));
	}


	/**
	 * Add a parameter to an URL. The URL must be in the standard format, not in SEO friendly format.
	 * */
	public static String addParameter(String url, String parameter, String value)
	{
		if(url.contains("?"))
		{
			if(url.substring(url.length() - 1, url.length()).equals("?")) // Example: www.example.com/script?
			{
				return url + parameter + "=" + value;
			}
			else
			{
				if(url.substring(url.length() - 1, url.length()).equals("&")) // Example: www.example.com/script?someparam=somevalue&
				{
					return url + parameter + "=" + value;
				}
				else // Example: www.example.com/script?someparam=somevalue
				{
					return url + "&" + parameter + "=" + value;
				}
			}
		}
		else // Example: www.example.com/script
		{
			return url + "?" + parameter + "=" + value;
		}
	}


	/**
	 * Update a parameter already present in the URL. The URL must be in the standard format, not in SEO friendly format.
	 * */
	public static String updateParameter(String url, String parameter, String value)
	{
		StringBuilder url_builder = new StringBuilder();
		String[] parts;
		if(url.contains(String.format("&%s=", parameter)))
		{
			parts = url.split(String.format("&%s=", parameter));
			url_builder.append(parts[0]).append(String.format("&%s=%s", parameter, value));
		}
		else if(url.contains(String.format("?%s=", parameter)))
		{
			parts = url.split(String.format("\\?%s=", parameter));
			url_builder.append(parts[0]).append(String.format("&%s=%s", parameter, value));
		}
		else
		{
			throw new RuntimeException(String.format("Could not find the parameter '%s' in the following url: %s", parameter, url));
		}

		if(parts.length > 1 && parts[1].contains("&"))
		{
			url_builder.append(parts[1].substring(parts[1].indexOf("&")));
		}

		return url_builder.toString();
	}


	/**
	 * Returns the value of a parameter present in the URL. The URL must be in the standard format, not in SEO friendly format.
	 * */
	public static String getParameterValue(String url, String parameter)
	{
		String value;

		String[] parts;
		if(url.contains(String.format("&%s=", parameter)))
		{
			parts = url.split(String.format("&%s=", parameter));
			if(parts.length == 1)
			{
				value = "";
			}
			else
			{
				value = parts[1];
			}
		}
		else if(url.contains(String.format("?%s=", parameter)))
		{
			parts = url.split(String.format("\\?%s=", parameter));
			if(parts.length == 1)
			{
				value = "";
			}
			else
			{
				value = parts[1];
			}
		}
		else
		{
			throw new RuntimeException(String.format("Could not find the parameter '%s' in the following url: %s", parameter, url));
		}

		if(value.contains("&"))
		{
			value = value.substring(0, value.indexOf("&"));
		}

		return value;
	}


	/**
	 * Return the bottom level domain. For example, "jobs" is the bottom level domain of "jobs.jobalineci.com".
	 * This method requires 3 level domain. If not, a RuntimeException will be thrown.
	 * This means that "jobalineci.com" is not allowed.
	 * */
	public static String getBottomLevelDomain(String url)
	{
		if(StringUtils.countMatches(url, ".") < 2)
		{
			throw new RuntimeException("The url specified by parameter does not seem to be a valid url or its domain is not at least a 3 level domain. Url: " + url);
		}

		if(url.contains("http://"))
		{
			url = url.replace("http://", "");
		}
		else if(url.contains("https://"))
		{
			url = url.replace("https://", "");
		}

		return url.split("\\.")[0];
	}


	/**
	 * Url syntax:
	 *
	 *   scheme://[user:password@]domain:port/path?query_string#fragment_id
	 *
	 * This method returns:
	 *
	 *   scheme://[user:password@]domain:port
	 * */
	public static String getPortionBeforePath(String url)
	{
		int from = url.indexOf("://");

		if(from == -1)
		{
			throw new RuntimeException("Could not find the '://' portion of the url: " + url);
		}

		from += 3;

		int to = url.indexOf("/", from);

		if(to == -1) // the url has not path
		{
			return url;
		}
		else
		{
			return url.substring(0, to);
		}
	}
}
