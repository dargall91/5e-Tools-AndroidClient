package com.DnD5eTools.models.combatants;

import com.DnD5eTools.entities.encounter.EncounterMonster;

public class MonsterCombatant extends Combatant {
    private boolean invisible;
    private boolean reinforcement;
    private int quantity;
    private int hitPoints;
    private boolean alive;
    private boolean removed;
    boolean expanded;

    public MonsterCombatant(EncounterMonster monster) {
        setServerName(monster.getMonster().getDisplayName());
        setLocalName(monster.getMonster().getName());
        setInitiative(monster.getInitiative() + monster.getMonster().getTotalInitiativeBonus());
        setInitiativeBonus(monster.getMonster().getTotalInitiativeBonus());
        invisible = monster.isInvisible();
        reinforcement = monster.isReinforcement();
        quantity = monster.getQuantity();
        setAc(monster.getMonster().getArmorClass());
        hitPoints = monster.getMonster().getHitPoints();
        alive = true;
        expanded = quantity == 1;
    }

    public MonsterCombatant(MonsterCombatant combatant) {
        setServerName(combatant.getServerName());
        setLocalName(combatant.getLocalName());
        setInitiative(combatant.getInitiative());
        setInitiativeBonus(combatant.getInitiativeBonus());
        invisible = combatant.isInvisible();
        reinforcement = combatant.isReinforcement();
        quantity = combatant.getQuantity();
        setAc(combatant.getAc());
        hitPoints = combatant.getHitPoints();
        alive = true;
        expanded = true;
        setWeight(combatant.getWeight());
        removed = false;
    }

    @Override
    public boolean isReinforcement() {
        return reinforcement;
    }

    @Override
    public void setReinforcement(boolean reinforcement) {
        this.reinforcement = reinforcement;
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    @Override
    public boolean isMonster() {
        return true;
    }

    @Override
    public boolean isLairAction() {
        return false;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    @Override
    public int getHitPoints() {
        return hitPoints;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void kill() {
        alive = false;
    }

    @Override
    public void revive() {
        alive = true;
    }

    @Override
    public void setRemoved(boolean removed) {
        //remove combatants cannot return
        if (this.removed) {
            return;
        }

        alive = false;
        this.removed = removed;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }
}
