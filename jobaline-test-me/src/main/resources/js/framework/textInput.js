/**
 * Created by damian on 3/25/15.
 *
 *   - selector String, is required
 *   - value    String, is required
 */

var operation = args[0];
var selector = args[1];
var value = args[2];

var TextInputModel = function(selector, value)
{
	this.preValidation = function()
	{
		if(typeof jQuery !== "undefined")
		{
			if(jQuery(selector).length === 0)
			{
				throw "The text input with selector '" + selector + "' was not found in the page";
			}
		}
		else if(document.querySelector(selector) === null)
		{
			throw "The text input with selector '" + selector + "' was not found in the page";
		}
	};


	this.update = function()
	{
		var element;
		if(typeof jQuery !== 'undefined')
		{
			element = jQuery(selector);
			element.focus();
			element.focusin();
			element.val(value);
			element.focusout();
			element.blur();
		}
		else
		{
			element = document.querySelector(selector);
			element.focus();
			element.value = value;
			element.blur();
		}
	};


	this.postValidation = function()
	{
		var inputValue = typeof jQuery !== 'undefined' ? jQuery(selector).val() : document.querySelector(selector).value;

		if(inputValue !== value)
		{
			throw "Could not set the element '" + selector + "' with the value: '" + value + "'"
		}
	};


	this.doAll = function()
	{
		this.preValidation();
		this.update();
		this.postValidation();
	}
};

var o = new TextInputModel(selector, value);
o[operation]();
