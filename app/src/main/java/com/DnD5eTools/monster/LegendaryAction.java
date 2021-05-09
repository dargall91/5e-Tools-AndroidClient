package com.DnD5eTools.monster;

import java.io.Serializable;
import org.json.JSONObject;

/**
 * Monster LegendaryAction object. A LegendaryAction is like a normal action except that is has a cost associated with it.
 */
public class LegendaryAction implements Serializable {
	private	String name, description;
	private int cost;

	
	public LegendaryAction() {
		name = "name";
		description = "description";
		cost = 1;
	}
	
	LegendaryAction(JSONObject jsonObj) {
		try {
			name = jsonObj.getString("name");
			description = jsonObj.getString("description");
			cost = jsonObj.getInt("cost");
		} catch (Exception e) {
			System.out.println("Error in LegendaryAction(JsonObject): " + e.getMessage());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public int getCost() {
		return cost;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("name", name);
			obj.put("description", description);
			obj.put("cost", cost);
			
			System.out.println(obj.toString());
		} catch (Exception e) {
			System.out.println("Error in LegendaryAction.toJson: " + e.getMessage());
		}
		
		return obj;
	}
}
