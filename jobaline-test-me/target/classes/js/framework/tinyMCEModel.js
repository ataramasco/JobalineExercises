/**
 * Created by damian on 7/19/15.
 *
 *   - operation String, is required
 *   - id String, is required
 *   - text String, is required
 */

var operation = args[0];
var id = args[1];
var text = args[2];

var TinyMCEModel = function()
{
	this.preValidation = function()
	{
		if(typeof tinymce === "undefined")
		{
			throw "The TinyMCE library is not included in the page";
		}
		else if(tinymce.get(id) === null)
		{
			throw "The TinyMCE editor with id '" + id + "' was not found in the page";
		}
	};

	this.update = function()
	{
		tinymce.get(id).setContent(text);
		tinymce.get(id).save();
	};

	this.postValidation = function()
	{
		if(tinymce.get(id).getContent().indexOf(text) === -1)
		{
			throw "Could not set the text to the TinyMCE editor with id '" + id + "'";
		}
	};

	this.doAll = function()
	{
		this.preValidation();
		this.update();
		this.postValidation();
	}
};

var o = new TinyMCEModel();
o[operation]();
