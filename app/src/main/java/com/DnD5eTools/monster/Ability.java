package com.DnD5eTools.monster;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

//TODO: Ability, Action, and LegendaryAction could all extend an abstract class (MonsterAbility?)
//Also, possibly use Factory DP to create these objects.

/**
 * Montser Ability object. An ability is simply a name and a description of that ability.
 */
public class Ability implements Serializable {
	private String name;
	private String description;
	
	public Ability() {
		name = "name";
		description = "description";
	}
	
	/**
	 * Constructs a monster ability from json styled string
	 */
	public Ability(String jsonString) throws JSONException {
		this(new JSONObject(jsonString));
	}
	
	/**
	 * Constructs a monster ability from json object
	 */
	Ability(JSONObject jsonObj) {
		try {
			name = jsonObj.getString("name");
			description = jsonObj.getString("description");
		} catch (Exception e) {
			System.out.println("Error in Action(JsonObject): " + e.getMessage());
		}
	}
	
	/**
	 * Gets the name of this ability
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the description of this ability
	 */
	public String getDescription() {
		return description;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String desc) {
		description = desc;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("name", name);
			obj.put("description", description);
		} catch (Exception e) {
			System.out.println("Error in Ability.toJson: " + e.getMessage());
		}
		
		return obj;
	}
}
