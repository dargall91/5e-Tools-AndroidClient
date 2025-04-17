package com.DnD5eTools.entities.monster;

public abstract class MonsterFeature {
    private String name = "Name";
    private String description = "Description";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
