package com.jobaline.uiautomation.framework.lang;

import java.util.Random;

/**
 * Class StringUtils.
 * User: damian
 * Date: 29/09/13 8:00
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils
{
	public static final int DEFAULT_LENGTH = 10;


	public static String getRandomText()
	{
		return getRandomText(DEFAULT_LENGTH, false);
	}


	public static String getRandomText(int length)
	{
		return getRandomText(length, false);
	}


	public static String getRandomText(boolean alphaNumeric)
	{
		return getRandomText(DEFAULT_LENGTH, alphaNumeric);
	}


	public static String getRandomText(int length, boolean alphaNumeric)
	{
		return getRandomText(length, length, alphaNumeric);
	}


	public static String getRandomText(int minLength, int maxLength, boolean alphaNumeric)
	{
		return getRandomText(minLength, maxLength, alphaNumeric, false);
	}


	public static String getRandomText(int minLength, int maxLength, boolean alphaNumeric, boolean includeSpace)
	{
		return getRandomText(minLength, maxLength, alphaNumeric, includeSpace, false);
	}


	public static String getRandomText(int minLength, int maxLength, boolean alphaNumeric, boolean includeSpace, boolean allowLeadingAndTrailingSpace)
	{
		int length = (int)(Math.random() * (maxLength - minLength)) + minLength;

		char[] chars;
		if(alphaNumeric)
		{
			if(includeSpace)
				chars = "abcde fghij klmno pqrst uvwxy z0123 456789".toCharArray();
			else
				chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		}
		else
		{
			if(includeSpace)
				chars = "abcde fghij klmno pqrst uvwxyz".toCharArray();
			else
				chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		}

		StringBuilder sb = new StringBuilder();
		Random random = new Random();

		for(int i = 0; i < length; i++)
		{
			char c = chars[random.nextInt(chars.length)];
			if(c == ' ')
			{
				if(sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ') // avoid sequences of spaces
				{
					i--;
					continue;
				}
				else if(!allowLeadingAndTrailingSpace && (sb.length() == 0 || sb.length() + 1 == length))
				{
					i--;
					continue;
				}
			}

			sb.append(c);
		}

		return sb.toString();
	}


	public static boolean containsMultipleSubstrings(String string, String[] substrings, boolean normalizeSpaces)
	{
		boolean result = true;

		if(normalizeSpaces)
		{
			string = string.replaceAll("\\s+", " ");
		}

		for(String substring : substrings)
		{
			if(normalizeSpaces)
			{
				substring = substring.replaceAll("\\s+", " ");
			}

			if(!string.contains(substring))
			{
				result = false;
				break;
			}
		}

		return result;
	}


	public static boolean isLowerCase(String value)
	{
		return value.toLowerCase().equals(value);
	}

}
