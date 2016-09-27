var cssSelector = args[0];
data.texts = [];
jQuery(cssSelector).each(function() {
	data.texts.push($(this).text());
});
