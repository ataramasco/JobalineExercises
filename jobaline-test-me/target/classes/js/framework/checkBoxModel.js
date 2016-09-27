/**
 * Created by damian on 3/25/15.
 *
 *   - operation String, is required
 *   - selector String, is required
 *   - check String, is required
 *
 * IE 8 seems to update the dom after the scripts finishes. After clicking the checkbox, if we don't set the attribute 'checked', when we try to check the status of the checkbox, it will not be the expected!
 */


var operation = args[0];
var selector = args[1];
var check = args[2];

var wait = 1500;

var CheckBoxModel = function()
{
	this.preValidation = function()
	{
		if($(selector).length == 0)
		{
			throw "The checkbox with css selector '" + selector + "' was not found in the page";
		}
	};

	this.update = function()
	{
		if(check !== null)
		{
			if(check)
			{
				if(!$(selector).is(':checked'))
				{
					$(selector).click();
				}

				if(navigator.userAgent.indexOf('MSIE 8.0') !== -1)
				{
					$(selector).attr("checked", "checked");
				}
			}
			else
			{
				if($(selector).is(':checked'))
				{
					$(selector).click();
				}

				if(navigator.userAgent.indexOf('MSIE 8.0') !== -1)
				{
					$(selector).removeAttr("checked");
				}
			}
		}
	};

	this.postValidation = function()
	{
		if(check !== null)
		{
			if(check)
			{
				var checked = false;

				var start = new Date().getTime();
				while((new Date().getTime() - start) < wait && !checked)
				{
					checked = $(selector).is(':checked');
				}

				if(!checked)
				{
					throw "The checkbox with css selector '" + selector + "' was not checked";
				}
			}
			else
			{
				var checked = true;

				/* TODO this should be received by parameter */
				var start = new Date().getTime();
				while((new Date().getTime() - start) < wait && checked)
				{
					checked = $(selector).is(':checked');
				}

				if(checked)
				{
					throw "The checkbox with css selector '" + selector + "' is checked. It should have been unchecked.";
				}
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

var o = new CheckBoxModel();
o[operation]();
