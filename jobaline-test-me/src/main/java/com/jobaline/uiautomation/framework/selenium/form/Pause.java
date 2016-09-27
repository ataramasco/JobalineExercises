package com.jobaline.uiautomation.framework.selenium.form;

/**
 * Created by damian on 4/1/15.
 */
public class Pause extends FormFieldModel
{

	private long time;


	public Pause(long time)
	{
		super("");
		this.time = time;
	}


	@Override public String generateUpdateStatements()
	{
		return ""
			+ "var waitInMillis = " + this.time + ";"
			+ "var start = new Date().getTime();"
			+ "while((new Date().getTime() - start) < waitInMillis);";
	}


	@Override public String generatePreValidationStatements()
	{
		return "";
	}


	@Override public String generatePostValidationStatements()
	{
		return "";
	}

}
