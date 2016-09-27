package com.jobaline.uiautomation.framework.lang;

/**
 * Created by damian on 2/11/15.
 */
public class Pair<T1, T2>
{
	private T1 record1;
	private T2 record2;


	public Pair(T1 record1, T2 record2)
	{
		this.record1 = record1;
		this.record2 = record2;
	}


	public T1 getFirstRecord()
	{
		return record1;
	}


	public T2 getSecondRecord()
	{
		return record2;
	}

}
