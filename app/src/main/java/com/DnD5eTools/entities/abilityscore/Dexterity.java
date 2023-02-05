package com.DnD5eTools.entities.abilityscore;

public class Dexterity extends AbilityScore {
    private int acrobatics;
    private int sleightOfHand;
    private int stealth;

    public int getAcrobatics() {
        return acrobatics;
    }

    public void setAcrobatics(int acrobatics) {
        this.acrobatics = acrobatics;
    }

    public int getSleightOfHand() {
        return sleightOfHand;
    }

    public void setSleightOfHand(int sleightOfHand) {
        this.sleightOfHand = sleightOfHand;
    }

    public int getStealth() {
        return stealth;
    }

    public void setStealth(int stealth) {
        this.stealth = stealth;
    }
}
