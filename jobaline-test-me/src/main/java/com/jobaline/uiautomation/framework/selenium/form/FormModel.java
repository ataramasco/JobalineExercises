package com.jobaline.uiautomation.framework.selenium.form;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by damian on 8/28/15.
 */
public class FormModel
{
	private String name;
	private List<IFormFieldModel> fields = new ArrayList<>();


	public FormModel(String name)
	{
		this.name = name;
	}


	public String getName()
	{
		return name;
	}


	public FormModel addField(IFormFieldModel field)
	{
		fields.add(field);
		return this;
	}


	public FormModel addTextFields(JSONObject data, Map<String, String> namesAndSelectors)
	{
		fields.addAll(namesAndSelectors.keySet().stream().filter(data::has).map(name -> new TextInputModel(namesAndSelectors.get(name), data.get(name).toString())).collect(Collectors.toList()));
		return this;
	}


	public FormModel addCheckBoxes(JSONObject data, Map<String, String> namesAndSelectors)
	{
		fields.addAll(namesAndSelectors.keySet().stream().filter(data::has).map(name -> new CheckboxModel(namesAndSelectors.get(name), data.getBoolean(name))).collect(Collectors.toList()));
		return this;
	}


	public FormModel addDropDownLists(JSONObject data, Map<String, String> namesAndSelectors)
	{
		fields.addAll(namesAndSelectors.keySet().stream().filter(data::has).map(name -> new SelectModel(namesAndSelectors.get(name), null, null, data.get(name).toString(), null)).collect(Collectors.toList()));
		return this;
	}


	public FormModel addHiddenInputs(JSONObject data, Map<String, String> namesAndSelectors)
	{
		fields.addAll(namesAndSelectors.keySet().stream().filter(data::has).map(name -> new HiddenInputModel(namesAndSelectors.get(name), data.get(name).toString())).collect(Collectors.toList()));
		return this;
	}


	public List<IFormFieldModel> getFields()
	{
		return fields;
	}
}
