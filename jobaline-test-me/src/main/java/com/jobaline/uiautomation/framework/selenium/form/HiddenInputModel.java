package com.jobaline.uiautomation.framework.selenium.form;

/**
 * Created by damian on 3/17/15.
 */
public class HiddenInputModel extends FormFieldModel
{
	protected String value;


	public HiddenInputModel(String cssSelector, String value)
	{
		super(cssSelector);
		this.value = value;
	}


	@Override public String generateUpdateStatements()
	{
		String script = "$(\"%s\").val(\"%s\");";
		return String.format(script, cssSelector, value);
	}


	@Override public String generatePreValidationStatements()
	{
		String script = "if(!$(\"%s\").length)throw \"The element was not found in the page '%s'\";";
		return String.format(script, cssSelector, cssSelector);
	}


	@Override public String generatePostValidationStatements()
	{
		return "";
	}
}
