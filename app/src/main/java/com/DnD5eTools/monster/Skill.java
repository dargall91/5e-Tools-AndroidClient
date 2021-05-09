package com.DnD5eTools.monster;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Monster Skill is the monster's proficiency or expertise in a s specific skill.
 */
public class Skill implements Serializable {
	boolean proficient, expertise;
	private String skill;
	
	public Skill() {
		proficient = false;
		expertise = false;
	}
	
	/**
	 * Constructs a monster ability score from json styled string
	 */
	public Skill(String jsonString) throws JSONException {
		this(new JSONObject(jsonString));
	}
	
	/**
	 * Constructs a monster ability from json object
	 */
	Skill(JSONObject jsonObj) {
		try {
			skill = jsonObj.getString("skill");
			proficient = jsonObj.getBoolean("proficient");
			expertise = jsonObj.getBoolean("expertise");
		} catch (Exception e) {
			System.out.println("Error in Action(JsonObject): " + e.getMessage());
		}
	}

	public boolean getProficient() {
		return proficient;
	}
	
	public boolean getExpertise() {
		return expertise;
	}
	
	public void setSkill(String skill) {
		this.skill = skill;
	}
	
	public void setProficient(boolean proficient) {
		this.proficient = proficient;
	}
	
	public void setExpertise(boolean expertise) {
		this.expertise = expertise;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("skill", skill);
			obj.put("proficient", proficient);
			obj.put("expertise", expertise);
		} catch (Exception e) {
			System.out.println("Error in Ability.toJson: " + e.getMessage());
		}
		
		return obj;
	}
}
