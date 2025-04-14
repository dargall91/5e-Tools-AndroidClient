package com.DnD5eTools.entities.encounter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Encounter {
    private Integer encounterId;
    private String name;
    private int musicId;
    private boolean hasLairAction = false;
    private List<EncounterMonster> encounterMonsters = new ArrayList<>();

    public Integer getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(int encounterId) {
        this.encounterId = encounterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public boolean getHasLairAction() {
        return hasLairAction;
    }

    public void setHasLairAction(boolean hasLairAction) {
        this.hasLairAction = hasLairAction;
    }

    public List<EncounterMonster> getEncounterMonsters() {
        return encounterMonsters;
    }

    public void setEncounterMonsters(List<EncounterMonster> encounterMonsters) {
        this.encounterMonsters = encounterMonsters;
    }

    public int getXpTotal() {
        int rawTotal = 0;
        int quanity = 0;

        for (EncounterMonster monster : encounterMonsters) {
            if (!monster.getIsMinion()) {
                rawTotal += monster.getXp() * monster.getQuantity();
                quanity += monster.getQuantity();
            }
        }

        //1 monster (or 0, for cases where a monster has yet to be added to the encounter)
        if (quanity <= 1) {
            return rawTotal;
        }

        //2 monsters
        if (quanity <= 2) {
            return (int) (rawTotal * 1.5);
        }

        //3-6 monsters
        if (quanity <= 6) {
            return rawTotal * 2;
        }

        //7-10 monsters
        if (quanity <= 10) {
            return (int) (rawTotal * 2.5);
        }

        //11-14 monsters
        if (quanity <= 14) {
            return rawTotal * 3;
        }

        //15+ monsters
        return rawTotal * 4;
    }
}
