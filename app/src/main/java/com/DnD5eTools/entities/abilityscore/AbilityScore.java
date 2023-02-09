package com.DnD5eTools.entities.abilityscore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public int getScoreModifier() {
        return (int) Math.floor((score - 10) / 2.0);
    }

    /**
     * THis method does nothing because the bonus is a calculated value, it cannot be set. But JPA complains if this
     * method is not here, so here it is
     * @param scoreModifier
     */
    public void setScoreModifier(int scoreModifier) { }

    public boolean isProficient() {
        return proficient;
    }

    public void setProficient(boolean proficient) {
        this.proficient = proficient;
    }
}
