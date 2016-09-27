/**
 * Created by damian on 3/25/15.
 *
 *   - operation  String, is required. Operation to perform: "preValidation", "update", "postValidation", "doAll"
 *   - selector   String, is required
 *   - values      Array of String, is optional. If set, will be used to validate the values of the options.
 *   - texts      Array of Text, is optional. If set, will be used to validate the texts of the options.
 *   - value      String, is optional. If set, will be used to select the option by value.
 *   - text      String, is optional. If set, will be used to select the option by text.
 *
 * "value" has priority over "text"
 *
 * args must be an array with 6 entries. If you don't want to set some of the entries, set it as null:
 *
 *    ["doAll", "select", ["1", "2", "3"], ["a", "b", "c"], "2", "b"];   // Will validate the texts, the values and will select by value
 *    ["doAll", "select", ["1", "2", "3"], ["a", "b", "c"], "2", null];  // Will validate the texts, the values and will select by value
 *    ["doAll", "select", ["1", "2", "3"], ["a", "b", "c"], null, "b"];  // Will validate the texts, the values and will select by text
 *    ["doAll", "select", ["1", "2", "3"], ["a", "b", "c"], null, null]; // Will validate the texts, the values and won't select
 *    ["doAll", "select", null, ["a", "b", "c"], null, null];  // Will validate the texts and won't select
 */

var operation = args[0];
var selector = args[1];
var values = args[2];
var texts = args[3];
var value = args[4];
var text = args[5];

var SelectModel = function(selector, values, texts, value, text)
{
	this.preValidation = function()
	{
		if($(selector).length == 0)
		{
			throw "The drop down list with '" + selector + "' was not found in the page";
		}

		var options;
		var actualToString;
		var expectedToString;

		if(typeof values !== "undefined" && values !== null)
		{
			options = $(selector).find("option");

			if(options.length !== values.length)
			{
				actualToString = options.map(function() { return $(this).val(); }).get().join();
				expectedToString = values.join();

				throw "The drop down list '" + selector + "' does not have the expected options values. Actual: " + actualToString + " / expected: " + expectedToString;
			}
			else
			{
				options.each(function()
				{
					if(values.indexOf($(this).val()) === -1)
					{
						var actualToString = options.map(function() { return $(this).val(); }).get().join();
						var expectedToString = values.join();

						throw "The drop down list '" + selector + "' does not have the following expected option: '" + $(this).val() + "'. Actual: " + actualToString + " / expected: " + expectedToString;
					}
				});
			}
		}


		if(typeof texts !== "undefined" && texts !== null)
		{
			options = $(selector).find("option");

			if(options.length !== texts.length)
			{
				actualToString = options.map(function() { return $(this).text(); }).get().join();
				expectedToString = texts.join();

				throw "The drop down list '" + selector + "' does not have the expected options texts. Actual: " + actualToString + " / expected: " + expectedToString;
			}
			else
			{
				options.each(function()
				{
					if(texts.indexOf($(this).text()) === -1)
					{
						var actualStr = options.map(function() { return $(this).text(); }).get().join();
						var expectedStr = texts.join();

						throw "The drop down list '" + selector + "' does not have the expected options texts. Actual: " + actualStr + " / expected: " + expectedStr;
					}
				});
			}
		}

		if(typeof value !== "undefined" && value !== null)
		{
			if($(selector).find("option[value='" + value + "']").length == 0)
			{
				throw "The drop down list '" + selector + "' does not have an option with value '" + value + "'";
			}
		}

		if(typeof text !== "undefined" && text !== null)
		{
			if($(selector).find("option:contains('" + text + "')").length == 0)
			{
				throw "The drop down list '" + selector + "' does not have an option with the text '" + text + "'";
			}
		}
	};


	this.update = function()
	{
		if(typeof value !== "undefined" && value !== null)
		{
			$(selector).get(0).value = value;
			/* I have not idea why the following code does not work for some selects (like the state in contact info page) in some browser (like phantomjs).
			 Will use HTMLSelectElement.value but it will not work if we make this object to support multi-option selects
			 $(selector).find("option[value='" + value + "']").attr("selected", true);
			 */
		}
		else if(typeof text !== "undefined" && text !== null)
		{
			$(selector).find("option:contains('" + text + "')").attr("selected", true);
		}
	};


	this.postValidation = function()
	{
		if(typeof value !== "undefined" && value !== null)
		{
			if($(selector + " option:selected").val() !== value)
			{
				throw "Could not select the option with value '" + value + "' in the drop down list '" + selector + "'";
			}
		}
		else if(typeof text !== "undefined" && text !== null)
		{
			if($(selector + " option:selected").text() !== text)
			{
				throw "Could not select the option with text '" + text + "' in the drop down list ''" + selector + "'";
			}
		}
	};


	this.doAll = function()
	{
		this.preValidation();
		this.update();
		this.postValidation();
	}
};

var o = new SelectModel(selector, values, texts, value, text);
o[operation]();
