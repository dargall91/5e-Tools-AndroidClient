package com.DnD5eTools.client;

import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.player.PlayerCharacter;

import java.io.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Combatant data to be sent to the server.
 */
public class Combatant implements Comparable<Combatant>, Serializable {
	private boolean monster, reinforcement, lairAction;
	private int initiative, bonus, quantity;
	private int weight; //used to break absolute ties (both initiative and bonus are ==), higher weight == higher initiative
	//private PlayerCharacter pc;
	private final String[] ac;
    private String[] hp;
	private int breaker;
	private boolean[] alive;
	private String name, displayName;
	boolean tied;

	/**
	 * Constructor for a Player Character combatant
	 *
	 * @param pc the PlayerCharacter object
	 * @param initiative the PC's initiative
	 */
	public Combatant(PlayerCharacter pc, int initiative) {
		//this.pc = pc;
		monster = false;
		reinforcement = false;
		this.initiative = initiative;
		breaker = 0;
		weight = 0;
		ac = new String[1];
		ac[0] = Integer.toString(pc.getAC());
		hp = new String[] {"0"};
		displayName = pc.getName();
		lairAction = false;
		bonus = pc.getBonus();
		name = pc.getName();
		quantity = 1;
		alive = new boolean[] {true}; //PCs should always show up on the ServerCombatScreen
	}

	/**
	 * Constructor for a Monster combatant
	 * @param monData the Monster's MonsterData object
	 * @param mon the Monster object
	 */
	public Combatant(MonsterData monData, Monster mon) {
		//this.monData = monData;
		name = mon.getName();
		displayName = mon.getDisplayName();
		monster = true;
		reinforcement = monData.isReinforcement();
		bonus = mon.getInitiativeBonus();
		initiative = monData.getInitiative() + bonus;
		breaker = 0;
		this.quantity = monData.getQuantity();
		ac = new String[quantity];
		hp = new String[quantity];
		alive = new boolean[quantity];
		lairAction = false;
		
		for (int i = 0; i < quantity; i++)
			setAC(i, mon.getAC());
			
		for (int i = 0; i < quantity; i++)
			setHP(i, mon.getHP());

		for (int i = 0; i < quantity; i++)
			alive[i] = true;
			
		weight = 0;
	}

	/**
	 * Constructor used for adding in monsters from outside the predefined encounter
	 *
	 * @param mon The Monster(s)
	 * @param quantity the amount of this monster
	 * @param initiative the monsters initiative
	 */
	public Combatant(Monster mon, int quantity, int initiative) {
		name = mon.getName();
		displayName = mon.getDisplayName();
		monster = true;
		reinforcement = false;
		bonus = mon.getInitiativeBonus();
		this.initiative = initiative + bonus;
		breaker = 0;
		this.quantity = quantity;
		ac = new String[quantity];
		hp = new String[quantity];
		alive = new boolean[quantity];
		lairAction = false;

		for (int i = 0; i < quantity; i++)
			setAC(i, mon.getAC());

		for (int i = 0; i < quantity; i++)
			setHP(i, mon.getHP());

		for (int i = 0; i < quantity; i++)
			alive[i] = true;

		weight = 0;
	}

	/**
	 * Constructor for a Lair Actions
	 */
	public Combatant() {
		name = "Lair Action";
		displayName = "Lair Action";
		monster = false;
		reinforcement = false;
		lairAction = true;
		bonus = -9999; //set to an impossibly low number to ensure lair actions always lose ties
		initiative = 20;
		breaker = 0;
		ac = new String[] {"0"};
		hp = new String[] {"0"};
		alive = new boolean[] {true};
		quantity = 1;
		alive = new boolean[] {false};
	}

	public void initFromJson(JSONObject json) {
		//TODO: is this even needed?
	}
	
	public String getName() {
		return name;
	}
	
	public int getInitiative() {
		return initiative;
	}
	
	public int getBonus() {
		return bonus;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public int getBreaker() {
		return breaker;
	}
	
	public boolean isMonster() {
		return monster;
	}
	
	public boolean isReinforcement() {
		return reinforcement;
	}
	
	public String getAC(int index) {
		if (monster)
			return ac[index];
			
		return ac[0];
	}

	/**
	 * Gets the Hit Points of the combatant
	 * @param index THe index of the monster, only used if there is more than 1 of this smonster
	 * @return The HP of the monster, or 999 if this is a PC
	 */
	public String getHP(int index) {
		if (monster)
			return hp[index];
		
		return hp[0];
	}
	
	public void setPlayerCharacter(PlayerCharacter pc) {
		bonus = pc.getBonus();
		name = pc.getName();
		displayName = pc.getName();
		ac[0] = Integer.toString(pc.getAC());
	}
	
	public void setInitiative(int initiative) {
		this.initiative = initiative;
	}
	
	public void setBonus(int bonus) {
		this.bonus = bonus;
	}
	
	public void setMonster(boolean monster) {
		this.monster = monster;
	}
	
	public void setReinforcement(boolean reinforcement) {
		this.reinforcement = reinforcement;
	}
	
	public void setAC(int index, String ac) {
		if (monster)
			this.ac[index] = ac;
		
		else
			this.ac[0] = ac;
	}
	
	public void setHP(int index, String hp) {
		if (monster)
			this.hp[index] = hp;

		else
			this.hp[0] = hp;
	}
	
	public void increaseWeight() {
		weight++;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setBreaker(int breaker) {
		this.breaker = breaker;
	}

	/**
	 * For now, should only be used for monsters, since PCs have a chance of revival
	 *
	 * @param index
	 */
	public void kill(int index) {
		if (monster)
			alive[index] = false;
	}

	/**
	 * For now, should only be used for monsters, since PCs have a chance of revival
	 *
	 * @param index
	 */
	public void revive(int index) {
		if (monster)
			alive[index] = true;
	}

	/**
	 * Checks if this combatant is alive or dead. A PC is considered always alive for now, may update in future
	 *
	 * @param index
	 * @return true if alive, false otherwise
	 */
	public boolean isAlive(int index) {
		if (monster)
			return alive[index];

		else
			return alive[0];
	}

	/**
	 * Checks if this is a Lair Action comabatant or not
	 * @return
	 */
	public boolean isLairAction() {
		return lairAction;
	}

	public String getDisplayName() {
		if (!lairAction)
			return displayName;

		return name;
	}

	/**
	 * Set if this combatant is tied with another combatant
	 *
	 * @param tied true if tied, false if not
	 */
	private void setTied(boolean tied) {
		this.tied = tied;
	}

	/**
	 * Check if this combatant is tired with another combatant
	 *
	 * @return true if tied, false if not
	 */
	public boolean isTied() {
		return tied;
	}

	/**
	 * weighs this combatant against another to determine turn order
	 *
	 * @param c The combatant against which to weigh this one
	 */
	public void weigh(Combatant c) {
		//use total initiative to determine turn order
		if (getInitiative() > c.getInitiative()) {
			increaseWeight();
			setTied(false);
			return;
		}
			
		if (getInitiative() < c.getInitiative()) {
			c.increaseWeight();
			setTied(false);
			return;
		}
		
		//in the event of an initiative tie, priority goes to combatant with higher initiative bonus
		if (getBonus() > c.getBonus()) {
			increaseWeight();
			setTied(false);
			return;
		}
			
		if (getBonus() < c.getBonus()) {
			c.increaseWeight();
			setTied(false);
			return;
		}

		//check for ties, also check that all tied combatants have rolled a tie breaker die
		if (getBreaker() == 0 || c.getBreaker() == 0 || getBreaker() == c.getBreaker()) {
			setTied(true);
			return;
		}

		//highest tie breaker goes first
		if (getBreaker() > c.getBreaker()) {
			increaseWeight();
			setTied(false);
			return;
		}

		if (getBreaker() < c.getBreaker()) {
			c.increaseWeight();
			setTied(false);
			return;
		}

		//if somehow one of the above does not trigger, tied
		setTied(true);
	}

	public void reset() {
		weight = 0;
	}

	/**
	 * Used for sorting, combatants must be weighed first otherwise they will all be tied
	 *
	 * @param c The combatant to compare this one to
	 * @return 1 if this > c, -1 if this < c, 0 if tie
	 */
	public int compareTo(Combatant c) {
		if (weight > c.getWeight())
			return 1;
		
		if (weight < c.getWeight())
			return -1;
		
		return 0;
	}

	/**
	 * Stringifies the JSONObject created by toSimpleJson
	 * @return stringified JSONObject compatible with SimpleCombatant.java
	 */
	public String toSimpleJsonString() {
		String result = "{}";

		try {
			result = toSimpleJson().toString(4);
		} catch (Exception e) {
			System.out.println("Error in Combatant.toJsonString: " + e.getMessage());
		}

		return result;
	}

	/**
	 * Converts a combatant into a json string compatible with SimpleCombatant
	 * @return a JSONObject with only the information needed by SimpleCombatant.java
	 */
	public JSONObject toSimpleJson() throws JSONException {
		JSONObject obj = new JSONObject();

		obj.put("reinforcement", isReinforcement());
		obj.put("name", getDisplayName());
		obj.put("initiative", getInitiative());
		obj.put("weight", getWeight());

		return obj;
	}
}
