package com.DnD5eTools.entities.encounter;

import com.DnD5eTools.entities.monster.Monster;

public class EncounterMonster {
    private int monsterId;
    private Monster monster;
    private String name;
    private int quantity = 1;
    private int initiativeRoll = 1;
    private boolean isInvisible = false;
    private boolean isReinforcement = false;
    private boolean isMinion = false;

    public EncounterMonster() { }

    public EncounterMonster(Monster monster) {
        this.monster = monster;
        this.name = monster.getName();
        this.monsterId = monster.getMonsterId();
    }

    public EncounterMonster(Monster monster, int quantity, int initiativeRoll) {
        this.monster = monster;
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

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
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

    public boolean isInvisible() {
        return isInvisible;
    }

    public void setInvisible(boolean invisible) {
        this.isInvisible = invisible;
    }

    public boolean isReinforcement() {
        return isReinforcement;
    }

    public void setReinforcement(boolean reinforcement) {
        this.isReinforcement = reinforcement;
    }

    public boolean isMinion() {
        return isMinion;
    }

    public void setMinion(boolean minion) {
        this.isMinion = minion;
    }
}
