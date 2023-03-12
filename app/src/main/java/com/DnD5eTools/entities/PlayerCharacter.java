package com.DnD5eTools.entities;

public class PlayerCharacter {
    private Integer id;
    private String name;
    private int ac;
    private int acBonus;
    private int initiativeBonus;
    private int rolledInitiative;
    private boolean combatant;

    public PlayerCharacter() { }

    public PlayerCharacter(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public int getAcBonus() {
        return acBonus;
    }

    public void setAcBonus(int acBonus) {
        this.acBonus = acBonus;
    }

    public int getInitiativeBonus() {
        return initiativeBonus;
    }

    public void setInitiativeBonus(int initiativeBonus) {
        this.initiativeBonus = initiativeBonus;
    }

    public int getRolledInitiative() {
        return rolledInitiative;
    }

    public void setRolledInitiative(int rolledInitiative) {
        this.rolledInitiative = rolledInitiative;
    }

    public boolean isCombatant() {
        return combatant;
    }

    public void setCombatant(boolean combatant) {
        this.combatant = combatant;
    }
    public int getTotalAc() {
        return ac + acBonus;
    }
}
