package com.DnD5eTools.entities;

public class PlayerCharacter {
    private Integer id;
    private String name;
    private int ac;
    private int initiativeBonus;
    private int rolledInitiative;
    private Campaign campaign;

    protected PlayerCharacter() { }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAc() {
        return ac;
    }

    public int getInitiativeBonus() {
        return initiativeBonus;
    }

    public int getRolledInitiative() {
        return rolledInitiative;
    }

    public Campaign getCampaign() {
        return campaign;
    }
}
