package com.DnD5eTools.client;

import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.player.PlayerCharacter;

import java.io.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Combatant data to be sent to the server.
 */
public class Combatant implements Comparable<Combatant>, Serializable {
	private boolean monster, reinforcement, lairAction;
	private int initiative, bonus, quantity;
	private int weight;
	private final ArrayList<String> ac;
    private ArrayList<String> hp;
	private int breaker; //used to break absolute ties (both initiative and bonus are ==), higher weight == higher initiative
	private ArrayList<Boolean> alive;
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
		ac = new ArrayList<>();
		ac.add(Integer.toString(pc.getAC()));

		hp = new ArrayList<>();
		hp.add("0");

		displayName = pc.getName();
		lairAction = false;
		bonus = pc.getBonus();
		name = pc.getName();
		quantity = 1;
		alive = new ArrayList<>();
		alive.add(true);//PCs should always show up on the ServerCombatScreen
	}

	/**
	 * Constructor for a predefined Monster combatant
	 *
	 * @param monData the Monster's MonsterData object
	 * @param mon the Monster object
	 */
	public Combatant(MonsterData monData, Monster mon) {
		name = mon.getName();
		displayName = mon.getDisplayName();
		monster = true;
		reinforcement = monData.isReinforcement();
		bonus = mon.getInitiativeBonus();
		initiative = monData.getInitiative() + bonus;
		breaker = 0;
		this.quantity = monData.getQuantity();
		ac = new ArrayList<>();
		hp = new ArrayList<>();
		alive = new ArrayList<>();

		lairAction = false;
		
		for (int i = 0; i < quantity; i++) {
			ac.add(mon.getAC());
			hp.add(mon.getHP());
			alive.add(true);
		}
			
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
		ac = new ArrayList<>();
		hp = new ArrayList<>();
		alive = new ArrayList<>();

		lairAction = false;

		for (int i = 0; i < quantity; i++) {
			ac.add(mon.getAC());
			hp.add(mon.getHP());
			alive.add(true);
		}

		weight = 0;
	}

	/**
	 * Constructor for a Lair Action
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

		ac = new ArrayList<>();
		ac.add("0");
		hp = new ArrayList<>();
		hp.add("0");
		alive = new ArrayList<>();
		alive.add(true);
		quantity = 1;
	}

	/**
	 * Name getter
	 *
	 * @return combatant name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Initiative getter
	 *
	 * @return combatant initiative
	 */
	public int getInitiative() {
		return initiative;
	}

	/**
	 * Initiative bonus getter
	 *
	 * @return combatant initiative bonus
	 */
	public int getBonus() {
		return bonus;
	}

	/**
	 * Quantity getter
	 *
	 * @return number of this combatant
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Tie breaker getter
	 *
	 * @return combatant tie breaker value
	 */
	public int getBreaker() {
		return breaker;
	}

	/**
	 * Checks if this is a monster or not
	 *
	 * @return true if monster, otherwise false
	 */
	public boolean isMonster() {
		return monster;
	}

	/**
	 * Checks if this is a reinforcement or not
	 *
	 * @return true if a reinforcement, otherwise false
	 */
	public boolean isReinforcement() {
		return reinforcement;
	}

	/**
	 * AC getter
	 *
	 * @param index The index of the combatant, only used if there is more than 1 of this monster
	 * @return combatant AC
	 */
	public String getAC(int index) {
		if (monster)
			return ac.get(index);
			
		return ac.get(0);
	}

	/**
	 * Gets the Hit Points of the combatant
	 *
	 * @param index The index of the combatant, only used if there is more than 1 of this monster
	 * @return The HP of the combatant
	 */
	public String getHP(int index) {
		if (monster)
			return hp.get(index);
		
		return hp.get(0);
	}

	/**
	 * Sets the PC for this combatant
	 *
	 * @param pc The player character
	 */
	public void setPlayerCharacter(PlayerCharacter pc) {
		bonus = pc.getBonus();
		name = pc.getName();
		displayName = pc.getName();
		ac.set(0, Integer.toString(pc.getAC()));
	}

	/**
	 * Initiative setter
	 *
	 * @param initiative
	 */
	public void setInitiative(int initiative) {
		this.initiative = initiative;
	}

	/**
	 * Initiative bonus setter
	 *
	 * @param bonus
	 */
	public void setBonus(int bonus) {
		this.bonus = bonus;
	}

	/**
	 * Sets flag that marks if this is a monster or not
	 *
	 * @param monster boolean
	 */
	public void setMonster(boolean monster) {
		this.monster = monster;
	}

	/**
	 * Sets flag that marks if this is a reinforcement or not
	 *
	 * @param reinforcement boolean
	 */
	public void setReinforcement(boolean reinforcement) {
		this.reinforcement = reinforcement;
	}

	/**
	 * Sets the combatant's AC
	 *
	 * @param index The index of the combatant, only used if this is a monster
	 * @param ac The AC to set
	 */
	public void setAC(int index, String ac) {
		if (monster)
			this.ac.set(index, ac);
		
		else
			this.ac.set(0, ac);
	}

	/**
	 * Sets the combatant's HP
	 *
	 * @param index The index of the combatant, only used if this is a monster
	 * @param hp The HP to set
	 */
	public void setHP(int index, String hp) {
		if (monster)
			this.hp.set(index, hp);

		else
			this.hp.set(0, hp);
	}
	
	private void increaseWeight() {
		weight++;
	}
	
	private int getWeight() {
		return weight;
	}

	/**
	 * Sets the tie breaker value for this combatant
	 *
	 * @param breaker Tie breaker value
	 */
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
			alive.set(index, false);
	}

	/**
	 * For now, should only be used for monsters, since PCs have a chance of revival
	 *
	 * @param index
	 */
	public void revive(int index) {
		if (monster)
			alive.set(index, true);
	}

	/**
	 * Checks if this combatant is alive or dead. A PC is considered always alive for now, may update in future
	 *
	 * @param index
	 * @return true if alive, false otherwise
	 */
	public boolean isAlive(int index) {
		if (monster)
			return alive.get(index);

		else
			return alive.get(0);
	}

	/**
	 * Permanently removes this combatant.
	 * This method should only be called on monsters
	 *
	 * @param index
	 * @return true if alive, false otherwise
	 */
	public void remove(int index) {
		if (monster) {
			ac.remove(index);
			hp.remove(index);
			alive.remove(index);

			quantity--;
		}
	}

	/**
	 * Checks if this is a Lair Action combatant or not
	 * 
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
