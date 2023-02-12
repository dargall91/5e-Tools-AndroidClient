package com.DnD5eTools.models.combatants;

public abstract class Combatant implements Comparable<Combatant> {
    private String name;
    private int initiative;
    private int initiativeBonus;
    private int weight;
    private int tieBreaker;

    public abstract boolean isReinforcement();
    public abstract void setReinforcement(boolean reinforcement);
    public abstract boolean isInvisible();
    public abstract void setInvisible(boolean invisible);
    public abstract boolean isMonster();
    public abstract boolean isLairAction();
    public abstract int getQuantity();
    public abstract void setAc(int ac);
    public abstract int getAc();
    public abstract void setHitPoints(int hitPoints);
    public abstract int getHitPoints();
    public abstract boolean isAlive();
    public abstract void kill();
    public abstract void revive();
    public abstract void setRemoved(boolean removed);
    public abstract boolean isRemoved();
    public abstract void setExpanded(boolean expanded);
    public abstract boolean isExpanded();

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

    public int getInitiativeBonus() {
        return initiativeBonus;
    }

    public void setInitiativeBonus(int initiativeBonus) {
        this.initiativeBonus = initiativeBonus;
    }

    public int getWeight() {
        return weight;
    }

    public void increaseWeight() {
        weight++;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Weighs this combatant against another combatant and determines if its a tie. Increases the weight of the
     * combatant with the lower initiative
     * @param combatant
     * @return true if these combatants are tied, false if they are not
     */
    public boolean weighAndGetTied(Combatant combatant) {
        int initiativeCompare = Integer.compare(initiative, combatant.getInitiative());

        if (initiativeCompare < 0) {
            increaseWeight();
            return false;
        }

        if (initiativeCompare > 0) {
            combatant.increaseWeight();
            return false;
        }

        int bonusCompare = Integer.compare(initiativeBonus, combatant.getInitiativeBonus());

        if (bonusCompare < 0) {
            increaseWeight();
            return false;
        }

        if (bonusCompare > 0) {
            combatant.increaseWeight();
            return false;
        }

        //tied but at least one has yet to roll a tie breaker die
        if (getTieBreaker() == 0 || combatant.getTieBreaker() == 0) {
            return true;
        }

        int tieBreakerCompare = Integer.compare(getTieBreaker(), combatant.getTieBreaker());

        if (tieBreakerCompare < 0) {
            increaseWeight();
            return false;
        }

        if (tieBreakerCompare > 0) {
            combatant.increaseWeight();
            return false;
        }

        return true;
    }

    public int getTieBreaker() {
        return tieBreaker;
    }

    public void setTieBreaker(int tieBreaker) {
        this.tieBreaker = tieBreaker;
    }

    public int compareTo(Combatant combatant) {
        return Integer.compare(weight, combatant.weight);
    }
}
