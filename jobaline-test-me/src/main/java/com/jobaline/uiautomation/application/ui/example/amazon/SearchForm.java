package com.jobaline.uiautomation.application.ui.example.amazon;

import com.jobaline.uiautomation.framework.core.ui.BasePage;
import com.jobaline.uiautomation.framework.core.ui.BasePageFragment;
import com.jobaline.uiautomation.framework.core.ui.ITraditionalForm;
import com.jobaline.uiautomation.framework.selenium.form.FormModel;
import com.jobaline.uiautomation.framework.selenium.form.TextInputModel;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by damian.j on 3/14/16.
 */
public class SearchForm extends BasePageFragment implements ITraditionalForm
{
	public static final String CATEGORY_ALL   = "all";
	public static final String CATEGORY_VIDEO = "video";
	public static final String CATEGORY_BOOKS = "books";

	private static Map<String, String> categoryMapping;


	public SearchForm(BasePage page)
	{
		super(page);
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * Map the category values used by the tests to the actual values used by the system. As explained in the comments for fillForm() this is form maintainability and simplicity.
	 *
	 * ------- End tutorial comments --------
	 * */
	private static String mapCategory(String category)
	{
		if(categoryMapping == null)
		{
			categoryMapping = new HashMap<>();
			categoryMapping.put(CATEGORY_ALL, "search-alias=aps");
			categoryMapping.put(CATEGORY_VIDEO, "search-alias=instant-video");
			categoryMapping.put(CATEGORY_BOOKS, "search-alias=stripbooks");
		}

		if(!categoryMapping.containsKey(category))
		{
			throw new RuntimeException(String.format("The category value is not valid: '%s'", category));
		}

		return categoryMapping.get(category);
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This method is responsible to fill the form and the only way to fill the form must be by using this method. This means that this class should never have methods like: setCategory() or fillSearchBox() etc.
	 * We may have them as private methods invoked by this other, but never as part of the class public interface.
	 *
	 * The parameter is a json object with the data to fill the form. This method will only interact with the inputs that it has to fill based on the parameter. For example, if the "category" is received
	 * as parameter, then this method will fill the category drop down list. If the category is not received by parameter this method will ignore the category drop down list. It is not the responsibility of
	 * this method to know whether category is required or no.
	 *
	 * When this method has to fill some specific input (for example the category) will verify that the input is present and will throw an exception if not. This kind of verification is not consider a "test assert"
	 * it is an obvious consequence: can not fill an input that does not exist. It is similar to the fact that tests are not checking if the browser is open, it must be open, and if it is not some exception will be
	 * thrown at some point, but there is no test assert for the browser to be open.
	 *
	 * This method will not verify that the inputs that are not going to be filled are present.
	 *
	 * Note that for the category, the values that this method defines are not the actual values of the the drop down list of the page. The idea is to abstract tests from the system values so if they change
	 * we only make changes in the internal mapping and not in all the tests filling this form. Also, we can define friendlier values.
	 *
	 * ------- End tutorial comments --------
	 *
	 * data format example:
	 *
	 *    {
	 *     "category": "stripbooks",
	 *     "text": "the c programming language"
	 *    }
	 *
	 * 	"category" values:
	 *
	 * 	  - "all"  : All
	 * 	  - "video": Amazon Video
	 * 	  - "books": Books
	 *
	 * */
	@Override public ITraditionalForm fillForm(JSONObject data)
	{
		/**
		 * ------- Tutorial comments --------
		 *
		 * Following you can find how the form is filled.
		 *
		 * Ideally, the pattern would be:
		 *
		 *   1- Create an instance of FormModel
		 *   2- Add all the fields information to it.
		 *   3- Call selenium wrapper to fill the form using the FormModel instance.
		 *
		 * But in some complicated forms, we need to open sections, wait for dialogs or fill complex inputs not supported yet
		 * by FormModel (like jQuery UI widgets) or do some other complicated stuff so we use both: FormModel and direct invocation
		 * to the selenium api (that we call through the methods of SeleniumWrapper)
		 *
		 * ------- End tutorial comments --------
		 * */

		FormModel form = new FormModel("Search form");

		if(data.has("text"))
		{
			form.addField(new TextInputModel("#twotabsearchtextbox", data.getString("text")));
		}

		if(data.has("category"))
		{
			String actualValue = mapCategory(data.getString("category"));

			/**
			 * ------- Tutorial comments --------
			 *
			 * Note that in the following line, we set the select option using the selenium wrapper directly. Ideally we would use FormModel as follow:
			 *
			 *     form.addField(new SelectModel("#searchDropdownBox", null, null, actualValue, null));
			 *
			 * But SelectModel implementation uses JQuery and it is not available in the page.
			 *
			 * In any case, it is a good example to show that we can fill inputs by using both together: FormModel and SeleniumWrapper.
			 *
			 * ------- End tutorial comments --------
			 * */

			getSeleniumClient().selectDropDownOptionByValue("#searchDropdownBox", actualValue);
		}

		getSeleniumClient().fillForm(form);

		return this;
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This method clicks the submit button with no expectations. Usually, this method is not needed but is added for flexibility.
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override public void submitForm()
	{
		getSeleniumClient().click("input[value='Go']");
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This method is going to be called by tests that are filling the form properly. Only tests know when a form is filled properly (all the required inputs are filled with valid values) and
	 * when to call this method.
	 *
	 * For example, a test filling the form with invalid values to test validation rules, can never call this method.
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override public void submitFormSuccessfullyAndWaitForPageToLoad()
	{
		getSeleniumClient().clickAndWaitForPageToLoadCompletely(("input[value='Go']"));
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This method is not supported since this form does not have any validation.
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override public void trySubmitFormExpectingClientSideValidationErrors()
	{
		throw new UnsupportedOperationException("This form does not have client side validation");
	}


	/**
	 * ------- Tutorial comments --------
	 *
	 * This method is not supported since this form does not have any validation.
	 *
	 * ------- End tutorial comments --------
	 * */
	@Override public void submitFormExpectingServerSideValidationErrorsAndWaitForPageToLoad()
	{
		throw new UnsupportedOperationException("This form does not have server side validation");
	}
}
