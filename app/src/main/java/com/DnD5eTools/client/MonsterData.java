package com.DnD5eTools.client;

import java.io.Serializable;
import org.json.JSONObject;

/**
 * MonsterData is... the metadata(?) for a Monster in an Encounter.
 * i.e., it is is how many instances of the monster are part of the encounter, their initiative roll, reinforcement status, etc
 */
public class MonsterData implements Serializable {
	private String monster;
	private int quantity, xp, initiative;
	private boolean minion, reinforcement;
	
	public MonsterData(JSONObject json) {
		try {
			monster = json.getString("monster");
			quantity = json.getInt("quantity");
			xp = json.getInt("xp");
			minion = json.getBoolean("minion");
			reinforcement = json.getBoolean("reinforcement");
			initiative = json.getInt("initiative");
		} catch (Exception e) {
			System.out.println("Error in MonsterData(json) " + e.getMessage());
		}
	}
	
	public MonsterData(String monster, int xp) {
		this.monster = monster;
		this.xp = xp;
		quantity = 1;
		minion = false;
		reinforcement = false;
		initiative = 0;
	}
	
	public String getMonster() {
		return monster;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public int getXP() {
		return xp;
	}
	
	public int getInitiative() {
		return initiative;
	}
	
	public boolean isMinion() {
		return minion;
	}
	
	public boolean isReinforcement() {
		return reinforcement;
	}
	
	public void setMonster(String monster) {
		this.monster = monster;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public void setXP(int xp) {
		this.xp = xp;
	}
	
	public void setInitiative(int initiative) {
		this.initiative = initiative;
	}
	
	public void setMinion(boolean minion) {
		this.minion = minion;
	}
	
	public void setReinforcement(boolean reinforcement) {
		this.reinforcement = reinforcement;
	}
	
	public String toString() {
		return "monster: " + monster + " quantity: " + quantity;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("monster", monster);
			obj.put("quantity", quantity);
			obj.put("xp", xp);
			obj.put("minion", minion);
			obj.put("reinforcement", reinforcement);
			obj.put("initiative", initiative);
		} catch (Exception e) {
			System.out.println("Error in MonsterData.toJson: " + e.getMessage());
		}
		
		return obj;
	}
}
