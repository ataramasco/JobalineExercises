package com.jobaline.uiautomation.framework.selenium.form;

/**
 * Created by damian on 3/17/15.
 */
public class RadioModel extends FormFieldModel
{
	private String value;


	public RadioModel(String cssSelector, String value)
	{
		super(cssSelector);
		this.value = value;
	}


	@Override public String generateUpdateStatements()
	{
		String script = ""
			+ "	(function()"
			+ "	{"
			+ "		var elem = $(\"%s[value='%s']\");"
			+ "		elem.click();"
			+ "		if(navigator.userAgent.indexOf('MSIE 8.0') !== -1)"
			+ "		{"
			+ "			elem.attr('checked', 'checked');" // IE 8 seems to update the dom after the scripts finishes. After clicking the radio, if we don't set the attribute 'checked', when we try to get the radio checked, no one will be returned!
			+ "		}"
			+ "	}).call();";

		return String.format(script, cssSelector, value);
	}


	@Override public String generatePreValidationStatements()
	{
		String script = ""
			+ "if($(\"%s\").length == 0)"
			+ "{"
			+ "		throw \"The element: '%s' was not found in the page\";"
			+ "}";

		return String.format(script, cssSelector, cssSelector);
	}


	@Override public String generatePostValidationStatements()
	{
		String script = ""
			+ "	(function()"
			+ "	{"
			+ "		var val = $(\"%s:checked\").val();"
			+ "		if(val !== \"%s\")"
			+ "		{"
			+ "			throw \"Could not check the radio '%s' with the value: '%s'. Current value: '\" + val + \"'\";"
			+ "		}"
			+ "	}).call();";

		return String.format(script, cssSelector, value, cssSelector, value);
	}

}
