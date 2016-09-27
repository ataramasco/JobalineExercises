/**
 * Requires jQuery
 *
 * Works in any browser supported by the jQuery version used in the app
 *
 * Created by damian on 09/13/16.
 */

var cssSelector = args[0];

var element = jQuery(cssSelector);

data.isDisabled = element.prop("disabled");
