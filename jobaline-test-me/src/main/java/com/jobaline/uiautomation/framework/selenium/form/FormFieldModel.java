package com.jobaline.uiautomation.framework.selenium.form;

/**
 * Created by damian on 3/17/15.
 */
public abstract class FormFieldModel extends ElementModel implements IFormFieldModel
{
	public FormFieldModel(String cssSelector)
	{
		super(cssSelector);
	}


	@Override public String generateAllStatements()
	{
		return generatePreValidationStatements() + generateUpdateStatements() + generatePostValidationStatements();
	}
}
