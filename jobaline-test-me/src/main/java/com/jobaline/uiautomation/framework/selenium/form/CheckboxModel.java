package com.jobaline.uiautomation.framework.selenium.form;

import com.jobaline.uiautomation.framework.ResourceManager;
import com.jobaline.uiautomation.framework.lang.ListUtils;

/**
 * Created by damian on 3/17/15.
 */
public class CheckboxModel extends FormFieldModel
{
	private Boolean isChecked;


	public CheckboxModel(String cssSelector, Boolean isChecked)
	{
		super(cssSelector);
		this.isChecked = isChecked;
	}


	private String getScriptFileName()
	{
		return "js/framework/checkBoxModel.js";
	}


	@Override public String generateUpdateStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"update",
			cssSelector,
			isChecked
		));
	}


	@Override public String generatePreValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"preValidation",
			cssSelector,
			isChecked
		));
	}


	@Override public String generatePostValidationStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"postValidation",
			cssSelector,
			isChecked
		));
	}


	@Override public String generateAllStatements()
	{
		return ResourceManager.getJavascriptFileContent(getScriptFileName(), true, ListUtils.createList(
			"doAll",
			cssSelector,
			isChecked
		));
	}

}
