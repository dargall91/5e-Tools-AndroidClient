package com.DnD5eTools.models.combatants;

public class CombatantDto {
    private int order;
    private String name;

    public CombatantDto() { }

    public CombatantDto(int order, String name) {
        this.order = order;
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
