package com.jobaline.uiautomation.framework.core.ui;

import com.jobaline.uiautomation.framework.selenium.form.FormModel;
import org.json.JSONObject;

/**
 * Created by damian.j on 9/30/15.
 */
public interface IFormSection
{
	/**
	 * Fill the form based on the data received by parameter.
	 *
	 * This data is format-free, the class that implements it is going to define the format.
	 * */
	public abstract void fillForm(JSONObject data);

	/**
	 * As this is part of a bigger form, the object using this fragment may want to fill different form fragments together so may call this method to get the form
	 * model instead of filling the form in the page.
	 * */
	public abstract FormModel getFormModel(JSONObject data);

}
