package com.DnD5eTools.entities.abilityscore;

public abstract class AbilityScore {
    private int id;
    private int score = 10;
    private boolean proficient = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getBonus() {
        return (int) Math.floor((score - 10) / 2.0);
    }

    /**
     * THis method does nothing because the bonus is a calculated value, it cannot be set. But JPA complains if this
     * method is not here, so here it is
     * @param bonus
     */
    public void setBonus(int bonus) { }

    public boolean isProficient() {
        return proficient;
    }

    public void setProficient(boolean proficient) {
        this.proficient = proficient;
    }
}
