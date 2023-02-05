package com.DnD5eTools.entities;

import com.DnD5eTools.entities.encounter.Encounter;
import com.DnD5eTools.entities.monster.Monster;

import java.util.List;

public class Campaign {
    private Integer id;
    private String name;
    private boolean madness;
    private boolean active = false;
    List<Monster> monsterList;
    List<Encounter> encounterList;

    public Campaign() { }

    public Campaign(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMadness() {
        return madness;
    }

    public void setMadness(boolean madness) {
        this.madness = madness;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
