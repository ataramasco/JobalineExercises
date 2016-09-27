package com.jobaline.uiautomation.framework.util;

/**
 * Class StringUtils.
 * User: damian
 * Date: 29/09/13 8:00
 * To change this template use File | Settings | File Templates.
 */
public class PhoneNumberUtils
{

	/**
	 *
	 * */
	public static String toE164Format(String phoneNumberInFrindlyFormat)
	{
		// TODO check that the phone number is in friendly format.
		// It is already E164, return the parameter
		// If it is not in E164 and not in friendly format throw RuntimeException. 
		return "+1" + (phoneNumberInFrindlyFormat.replaceAll("\\D", ""));
	}


	/**
	 *
	 * */
	public static String toFriendlyFormat(String phoneNumberInE164)
	{
		// TODO check that the phone number is in E164.
		// It is already friendly format, return the parameter
		// If it is not in friendly format and not in E164 throw RuntimeException. 

		String phoneNumberInFrindlyFormat = phoneNumberInE164.substring(2); // Remove the +1

		return "(" + phoneNumberInFrindlyFormat.substring(0, 3) + ") " + phoneNumberInFrindlyFormat.substring(3, 6) + "-" + phoneNumberInFrindlyFormat.substring(6, 10);
	}


	/**
	 * This method will flip the last digit of the phone number in order to get a different phone number.
	 * */
	public static String flipLastDigitOfPhoneNumber(String phoneNumber)
	{
		return phoneNumber.substring(0, phoneNumber.length() - 1) + (Integer.parseInt("" + phoneNumber.charAt(phoneNumber.length() - 1)) + 1) % 10;
	}
}
