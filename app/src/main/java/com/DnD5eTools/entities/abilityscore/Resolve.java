package com.DnD5eTools.entities.abilityscore;

public class Resolve extends AbilityScore {
    /**
     * Players cannot have proficiency in Resolve
     * @param proficient
     */
    @Override
    public void setProficient(boolean proficient) {
        super.setProficient(false);
    }
}
