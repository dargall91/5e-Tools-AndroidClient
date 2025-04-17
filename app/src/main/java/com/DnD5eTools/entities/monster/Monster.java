package com.DnD5eTools.entities.monster;

import com.DnD5eTools.entities.abilityscore.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Monster {
    private Integer monsterId;
    private String name;
    private String displayName;
    private String size = "Medium";
    private String type = "Humanoid";
    private String alignment = "Neutral";
    private int armorClass = 10;
    private int hitPoints = 0;
    private String speed = "30 ft";
    private String senses = "Passive Perception 10";
    private String languages = "Common";
    private int bonusInitiative = 0;
    private int legendaryActionCount = 0;

    private Strength strength = new Strength();
    private Dexterity dexterity = new Dexterity();
    private Constitution constitution = new Constitution();
    private Intelligence intelligence = new Intelligence();
    private Wisdom wisdom = new Wisdom();
    private Charisma charisma = new Charisma();
    private ChallengeRating challengeRating;
    private List<Ability> abilities = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private List<LegendaryAction> legendaryActions = new ArrayList<>();

    public Monster() { }

    public Monster(String name, ChallengeRating challengeRating) {
        this.name = name;
        displayName = name;
        this.challengeRating = challengeRating;
    }

    public Integer getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getLegendaryActionCount() {
        return legendaryActionCount;
    }

    public void setLegendaryActionCount(int legendaryActionCount) {
        this.legendaryActionCount = legendaryActionCount;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenses() {
        return senses;
    }

    public void setSenses(String senses) {
        this.senses = senses;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public ChallengeRating getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(ChallengeRating challengeRating) {
        this.challengeRating = challengeRating;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public int getBonusInitiative() {
        return bonusInitiative;
    }

    @JsonIgnore
    public int getTotalInitiativeBonus() {
        return bonusInitiative + getDexterity().getScoreModifier();
    }

    public void setBonusInitiative(int bonusInitiative) {
        this.bonusInitiative = bonusInitiative;
    }

    public Strength getStrength() {
        return strength;
    }

    public void setStrength(Strength strength) {
        this.strength = strength;
    }

    public Dexterity getDexterity() {
        return dexterity;
    }

    public void setDexterity(Dexterity dexterity) {
        this.dexterity = dexterity;
    }

    public Constitution getConstitution() {
        return constitution;
    }

    public void setConstitution(Constitution constitution) {
        this.constitution = constitution;
    }

    public Intelligence getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(Intelligence intelligence) {
        this.intelligence = intelligence;
    }

    public Wisdom getWisdom() {
        return wisdom;
    }

    public void setWisdom(Wisdom wisdom) {
        this.wisdom = wisdom;
    }

    public Charisma getCharisma() {
        return charisma;
    }

    public void setCharisma(Charisma charisma) {
        this.charisma = charisma;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<LegendaryAction> getLegendaryActions() {
        return legendaryActions;
    }

    public void setLegendaryActions(List<LegendaryAction> legendaryActions) {
        this.legendaryActions = legendaryActions;
    }
}
