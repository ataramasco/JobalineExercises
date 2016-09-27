package com.jobaline.uiautomation.framework.selenium.form;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.lang.ListUtils;

/**
 * Created by damian on 3/17/15.
 */
public class TextInputModel extends FormFieldModel
{
	private String value;


	private String getScriptFileName()
	{
		return "js/framework/textInput.js";
	}


	public TextInputModel(String cssSelector, String value)
	{
		super(cssSelector);
		this.value = value;
	}


	@Override public String generateUpdateStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"update",
			cssSelector,
			value
		));
	}


	@Override public String generatePreValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"preValidation",
			cssSelector,
			value
		));
	}


	@Override public String generatePostValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"postValidation",
			cssSelector,
			value
		));
	}


	@Override public String generateAllStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"doAll",
			cssSelector,
			value
		));
	}
}
