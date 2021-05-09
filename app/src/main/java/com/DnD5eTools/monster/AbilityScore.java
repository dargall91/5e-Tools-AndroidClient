package com.DnD5eTools.monster;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Monster Ability Score object. Tracks the score itself and if the monster is proficient in the score.
 */
public class AbilityScore implements Serializable {
	private int score;
	boolean proficient;
	private String stat;
	
	public AbilityScore() {
		score = 10;
		proficient = false;
	}
	
	/**
	 * Constructs a monster ability score from json styled string
	 */
	public AbilityScore(String jsonString) throws JSONException {
		this(new JSONObject(jsonString));
	}
	
	/**
	 * Constructs a monster ability from json object
	 */
	AbilityScore(JSONObject jsonObj) {
		try {
			stat = jsonObj.getString("stat");
			score = jsonObj.getInt("score");
			proficient = jsonObj.getBoolean("proficient");
		} catch (Exception e) {
			System.out.println("Error in Action(JsonObject): " + e.getMessage());
		}
	}
	
	public int getScore() {
		return score;
	}
	
	public boolean getProficient() {
		return proficient;
	}
	
	public void setStat(String stat) {
		this.stat = stat;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public void setProficient(boolean proficient) {
		this.proficient = proficient;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("stat", stat);
			obj.put("score", score);
			obj.put("proficient", proficient);
		} catch (Exception e) {
			System.out.println("Error in Ability.toJson: " + e.getMessage());
		}
		
		return obj;
	}
}
