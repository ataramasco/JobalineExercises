data.results = [];

var resultsContainers = document.querySelectorAll(".a-fixed-left-grid");

for(var i = 0; i < resultsContainers.length; i++)
{
	var result = {};

	var resultContainer = resultsContainers[i];

	var titleElement = resultContainer.querySelector(".s-access-title");
	if(titleElement === null)
	{
		throw "Could not find the result title";
	}
	result.title = titleElement.innerText;

	var priceElement = resultContainer.querySelector(".s-price");
	if(priceElement === null)
	{
		throw "Could not find the result price";
	}
	result.price = priceElement.innerText;

	data.results.push(result);
}
