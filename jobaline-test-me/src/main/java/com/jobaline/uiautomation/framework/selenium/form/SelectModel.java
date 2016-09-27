package com.jobaline.uiautomation.framework.selenium.form;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.lang.ListUtils;

import java.util.List;

/**
 * Created by damian on 3/17/15.
 */
public class SelectModel extends FormFieldModel
{
	private List<String> values;
	private List<String> texts;
	private String       value;
	private String       text;


	public SelectModel(String cssSelector, List<String> values, List<String> texts, String value, String text)
	{
		super(cssSelector);
		this.values = values;
		this.texts = texts;
		this.value = value;
		this.text = text;
	}


	private String getScriptFileName()
	{
		return "js/framework/selectModel.js";
	}


	/**
	 * Will generate the Javascript code to update the Select element.
	 * If value is set, will update the select by value. If value is not set and text is set, will update the select by text. If both of them
	 * are not set, will not update the Select element.
	 * */
	@Override public String generateUpdateStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			cssSelector,
			null,
			null,
			value,
			text
		));
	}


	@Override public String generatePreValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			cssSelector,
			values,
			texts,
			null,
			null
		));
	}


	@Override public String generatePostValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			cssSelector,
			null,
			null,
			value,
			text
		));
	}


	@Override public String generateAllStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"doAll",
			cssSelector,
			values,
			texts,
			value,
			text
		));
	}
}
