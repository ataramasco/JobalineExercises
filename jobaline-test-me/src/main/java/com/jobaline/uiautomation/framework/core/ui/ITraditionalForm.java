package com.jobaline.uiautomation.framework.core.ui;

import org.json.JSONObject;

/**
 * Created by damian.j on 9/30/15.
 */
public interface ITraditionalForm
{
	/**
	 * Fill the form based on the data received by parameter.
	 *
	 * This data depends on the form that is going to be filled and is format free (i.e. the implementor is going to know how to parse it)
	 * */
	public abstract ITraditionalForm fillForm(JSONObject data);

	/**
	 * Submits the form setting no expectation.
	 *
	 * It does not know whether the form is going to get posted to the server, or there are validation errors or a new page is loaded or not. It can be as simple as clicking a button.
	 * */
	public abstract void submitForm();

	/**
	 * Submits the form expecting that:
	 *
	 *   - there are no client side validation errors
	 *   - the form got posted to the server
	 *   - there are no server side validation errors
	 *   - a new page is loaded.
	 * */
	public abstract void submitFormSuccessfullyAndWaitForPageToLoad();

	/**
	 * Tries to submit the form expecting that:
	 *
	 *   - there are client side validation errors
	 *   - the form does not get posted to the server
	 *   - no new page is loaded.
	 *
	 * This method starts with "try" because the form is never actually submitted to the server
	 * */
	public abstract void trySubmitFormExpectingClientSideValidationErrors();

	/**
	 * Submits the form expecting that:
	 *
	 *   - there are no client side validation errors,
	 *   - the form got posted to the server,
	 *   - there are server side validation errors,
	 *   - a new page is loaded.
	 * */
	public abstract void submitFormExpectingServerSideValidationErrorsAndWaitForPageToLoad();

}
