package com.DnD5eTools.models;

public class Combatant implements Comparable<Combatant> {
    int id;
    private boolean reinforcement;
    private String name;
    private int initiative;
    private int weight;

    public Combatant() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isReinforcement() {
        return reinforcement;
    }

    public void setReinforcement(boolean reinforcement) {
        this.reinforcement = reinforcement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInitiative() {
        return initiative;
    }

    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int compareTo(Combatant combatant) {
        return Integer.compare(weight, combatant.weight);
    }
}
