package com.jobaline.uiautomation.framework.selenium.form;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.lang.ListUtils;

/**
 * Created by damian on 3/17/15.
 */
public class TinyMCEModel extends FormFieldModel
{
	/**
	 * TinyMCE id. Is not a css selector.
	 * */
	private String id;

	private String text;


	public TinyMCEModel(String id, String text)
	{
		super(null);
		this.id = id;
		this.text = text;
	}


	private String getScriptFileName()
	{
		return "js/framework/tinyMCEModel.js";
	}


	@Override public String generateUpdateStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"update",
			id,
			text
		));
	}


	@Override public String generatePreValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"preValidation",
			id,
			text
		));
	}


	@Override public String generatePostValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"postValidation",
			id,
			text
		));
	}


	@Override public String generateAllStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"doAll",
			id,
			text
		));
	}

}
