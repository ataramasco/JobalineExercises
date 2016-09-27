package com.jobaline.uiautomation.framework.lang;

/**
 * Created by damian on 2/11/15.
 */
public class Triple<T1, T2, T3>
{
	private T1 record1;
	private T2 record2;
	private T3 record3;


	public Triple(T1 record1, T2 record2, T3 record3)
	{
		this.record1 = record1;
		this.record2 = record2;
		this.record3 = record3;
	}


	public T1 getFirstRecord()
	{
		return record1;
	}


	public T2 getSecondRecord()
	{
		return record2;
	}


	public T3 getThirdRecord()
	{
		return record3;
	}

}
