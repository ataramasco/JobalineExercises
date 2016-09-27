package com.jobaline.uiautomation.framework.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by damian.j on 10/15/15.
 */
public class SystemUtils
{
	public static String getFirstIpV4AddressOfInterface(String interfaceName)
	{
		String ipAddress = null;

		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements())
			{
				NetworkInterface networkInterface = interfaces.nextElement();

				if(interfaceName.equals(networkInterface.getName()))
				{
					Enumeration<InetAddress> ips = networkInterface.getInetAddresses();
					while(ips.hasMoreElements())
					{
						String ipAddress_aux = ips.nextElement().getHostAddress();

						if(!ipAddress_aux.contains(":")) // If has ":" then it is V6
						{
							ipAddress = ipAddress_aux;
							break;
						}
					}
				}
			}
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}

		return ipAddress;
	}


	public static String getPropertyOrFail(String propertyName)
	{
		String value = System.getProperty(propertyName, null);

		if(value == null)
		{
			throw new RuntimeException(String.format("Could not find the system property '%s'", propertyName));
		}

		return value;
	}


	public static String getEnvironmentVariableOrFail(String variableName)
	{
		String value = System.getenv(variableName);

		if(value == null)
		{
			throw new RuntimeException(String.format("Could not find the system property '%s'", variableName));
		}

		return value;
	}
}
