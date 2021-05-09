package com.DnD5eTools.monster;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Monster Action object. An action is a name and a description.
 */
public class Action implements Serializable {
	private String name;
	private String description;
	
	public Action() {
		name = "name";
		description = "description";
	}
	
	/**
	 * Constructs a monster action from json styled string
	 */
	Action(String jsonString) throws JSONException {
		this(new JSONObject(jsonString));
	}
	
	/**
	 * Constructs a monster action from json object
	 */
	Action(JSONObject jsonObj) {
		try {
			name = jsonObj.getString("name");
			description = jsonObj.getString("description");
		} catch (Exception e) {
			System.out.println("Error in Action(JsonObject): " + e.getMessage());
		}
	}
	
	/**
	 * Gets the name of this action
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the descripton of this action
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the name of this action
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the descripton of this action
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("name", name);
			obj.put("description", description);
		} catch (Exception e) {
			System.out.println("Error in Action.toJson: " + e.getMessage());
		}
		
		return obj;
	}
}
