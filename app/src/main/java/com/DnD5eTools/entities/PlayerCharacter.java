package com.DnD5eTools.entities;

import com.DnD5eTools.util.Util;

public class PlayerCharacter {
    private Integer id;
    private String name;
    private int ac;
    private int initiativeBonus;
    private int rolledInitiative;
    private Campaign campaign;
    private boolean combatant = false;

    public PlayerCharacter() { }

    public PlayerCharacter(String name) {
        this.name = name;
        campaign = Util.getCampaign();
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

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public boolean isCombatant() {
        return combatant;
    }

    public void setCombatant(boolean combatant) {
        this.combatant = combatant;
    }
}
