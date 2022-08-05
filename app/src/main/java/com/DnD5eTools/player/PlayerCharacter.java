package com.DnD5eTools.player;

import java.io.*;

import org.json.JSONObject;

/**
 * A PlayerCharacter represents one of the actual players of the D&D game.
 */
public class PlayerCharacter implements Serializable {
	private String name;
	private int bonus, ac, initiative;
	
	public PlayerCharacter(String name) {
		this.name = name;
		bonus = 0;
		ac = 0;
		initiative = 1;
	}
	 
	public PlayerCharacter(JSONObject json) {
		initFromJson(json);
	}
	
	private void initFromJson(JSONObject json) {
		try {
			name = json.getString("name");
			ac = json.getInt("ac");
			bonus = json.getInt("bonus");
			initiative = json.getInt("initiative");
		} catch (Exception e) {
			System.out.println("Error in PlayerCharacter(JSONObject): " + e.getMessage());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getAC() {
		return ac;
	}
	
	public int getBonus() {
		return bonus;
	}

	public int getInitiative() {
		return initiative;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setAC(int ac) {
		this.ac = ac;
	}
	
	public void setBonus(int bonus) {
		this.bonus = bonus;
	}

	public void setInitative(int initiative) {
		this.initiative = initiative;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("name", name);
			obj.put("ac", ac);
			obj.put("bonus", bonus);
			obj.put("initiative", initiative);
		} catch (Exception e) {
			System.out.println("Exception in PlayerCharacter.toJson: " + e.getMessage());
		}
		
		return obj;
	}
 }
		 
