package com.DnD5eTools.entities;

public class PlayerCharacter {
    private Integer playerCharacterId;
    private String playerCharacterName;
    private int totalArmorClass;
    private int initiativeBonus;
    private int initiativeRoll;
    private boolean isCombatant;

    public PlayerCharacter() { }

    public PlayerCharacter(String playerCharacterName) {
        this.playerCharacterName = playerCharacterName;
    }

    public Integer getPlayerCharacterId() {
        return playerCharacterId;
    }

    public void setPlayerCharacterId(Integer playerCharacterId) {
        this.playerCharacterId = playerCharacterId;
    }

    public String getPlayerCharacterName() {
        return playerCharacterName;
    }

    public void setPlayerCharacterName(String playerCharacterName) {
        this.playerCharacterName = playerCharacterName;
    }

    public int getTotalArmorClass() {
        return totalArmorClass;
    }

    public void setTotalArmorClass(int totalArmorClass) {
        this.totalArmorClass = totalArmorClass;
    }

    public int getInitiativeBonus() {
        return initiativeBonus;
    }

    public void setInitiativeBonus(int initiativeBonus) {
        this.initiativeBonus = initiativeBonus;
    }

    public int getInitiativeRoll() {
        return initiativeRoll;
    }

    public void setInitiativeRoll(int initiativeRoll) {
        this.initiativeRoll = initiativeRoll;
    }

    public boolean isCombatant() {
        return isCombatant;
    }

    public void setCombatant(boolean combatant) {
        this.isCombatant = combatant;
    }
}
