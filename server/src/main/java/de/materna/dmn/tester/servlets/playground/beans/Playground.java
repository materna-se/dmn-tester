package de.materna.dmn.tester.servlets.playground.beans;

import java.util.LinkedHashMap;
import java.util.Map;

import de.materna.dmn.tester.helpers.Serializable;
import de.materna.jdec.serialization.SerializationHelper;

public class Playground extends Serializable {
	public String name;
	public String description;
	public String expression;
	protected Map<String, ?> context = new LinkedHashMap<>();
	
	public Playground() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Map<String, ?> getContext() {
		return context;
	}

	public void setContext(Map<String, ?> context) {
		this.context = context;
	}
	
	public void fromJson(String json) {
		Playground temp = (Playground) SerializationHelper.getInstance().toClass(json, Playground.class);
		this.name = temp.getName();
		this.description = temp.getDescription();
		this.expression = temp.getExpression();
		this.context = temp.getContext();
	}
}
