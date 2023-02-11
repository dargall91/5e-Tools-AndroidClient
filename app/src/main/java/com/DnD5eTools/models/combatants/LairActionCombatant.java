package com.DnD5eTools.models.combatants;

public class LairActionCombatant extends Combatant {
    public LairActionCombatant() {
        setName("Lair Action");
        setInitiative(20);
    }
    @Override
    public boolean isReinforcement() {
        return false;
    }

    @Override
    public void setReinforcement(boolean reinforcement) { }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void setInvisible(boolean invisible) { }

    @Override
    public boolean isMonster() {
        return false;
    }

    @Override
    public boolean isLairAction() {
        return true;
    }

    @Override
    public int getQuantity() {
        return 1;
    }

    @Override
    public void setAc(int ac) {

    }

    @Override
    public int getAc() {
        return 0;
    }

    @Override
    public void setHitPoints(int hitPoints) {

    }

    @Override
    public int getHitPoints() {
        return 0;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void kill() {

    }

    @Override
    public void revive() {

    }

    @Override
    public int getInitiativeBonus() {
        ///an impossibly low never that can never win ties
        return -999;
    }
}
