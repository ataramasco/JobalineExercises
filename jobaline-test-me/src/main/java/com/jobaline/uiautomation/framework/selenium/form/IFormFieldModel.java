package com.jobaline.uiautomation.framework.selenium.form;

/**
 * Created by damian on 3/17/15.
 */
public interface IFormFieldModel
{
	public String generateUpdateStatements();

	public String generatePreValidationStatements();

	public String generatePostValidationStatements();

	public String generateAllStatements();
}
