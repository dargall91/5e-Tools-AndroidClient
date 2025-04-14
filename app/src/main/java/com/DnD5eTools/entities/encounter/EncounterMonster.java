package com.DnD5eTools.entities.encounter;

import com.DnD5eTools.entities.monster.Monster;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterMonster {
    private int monsterId;
    private String name;
    private int quantity = 1;
    private int initiativeRoll = 1;
    private boolean isInvisible = false;
    private boolean isReinforcement = false;
    private boolean isMinion = false;
    private int xp;
    private String displayName;
    private int initiativeBonus;
    private int dexterity;
    private int armorClass;
    private int hitPoints;

    public EncounterMonster() { }

    public EncounterMonster(Monster monster) {
        this.name = monster.getName();
        this.monsterId = monster.getMonsterId();
        quantity = 1;
        initiativeRoll = 1;
    }

    public EncounterMonster(Monster monster, int quantity, int initiativeRoll) {
        this.name = monster.getName();
        this.monsterId = monster.getMonsterId();
        this.quantity = quantity;
        this.initiativeRoll = initiativeRoll;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getInitiativeRoll() {
        return initiativeRoll;
    }

    public void setInitiativeRoll(int initiativeRoll) {
        this.initiativeRoll = initiativeRoll;
    }

    public boolean getIsInvisible() {
        return isInvisible;
    }

    public void getIsInvisible(boolean invisible) {
        this.isInvisible = invisible;
    }

    public boolean getIsReinforcement() {
        return isReinforcement;
    }

    public void setIsReinforcement(boolean reinforcement) {
        this.isReinforcement = reinforcement;
    }

    public boolean getIsMinion() {
        return isMinion;
    }

    public void setIsMinion(boolean minion) {
        this.isMinion = minion;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getInitiativeBonus() {
        return initiativeBonus;
    }

    public void setInitiativeBonus(int initiativeBonus) {
        this.initiativeBonus = initiativeBonus;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }
}
