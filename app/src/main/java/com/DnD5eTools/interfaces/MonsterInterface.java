package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.monster.Ability;
import com.DnD5eTools.entities.monster.Action;
import com.DnD5eTools.entities.monster.ChallengeRating;
import com.DnD5eTools.entities.monster.LegendaryAction;
import com.DnD5eTools.entities.monster.Monster;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class MonsterInterface extends AbstractInterface {
    private static final String path = "5eTools/api/monster/";

    public static List<NameIdProjection> getMonsterList() {
        return getListResult(new TypeReference<List<NameIdProjection>>() {}, path + "list");
    }

    public static Monster getMonster(int monsterId) {
        return getSingleResult(Monster.class, path + monsterId);
    }

    public static NameIdProjection addMonster(String name) {
        return putSingleResult(NameIdProjection.class, path + "add?name=" + name, null);
    }

    public static void updateMonster(Monster monster) {
        postNoResult(path + "update", monster);
    }

    public static void archiveMonster(int monsterId) {
        postNoResult(path + monsterId + "/archive", null);
    }

    public static Monster copyMonster(int monsterId, String name) {
        return putSingleResult(Monster.class, path + monsterId + "/copy?name=" + name, null);
    }

    public static List<ChallengeRating> getChallengeRatings() {
        return getListResult(new TypeReference<List<ChallengeRating>>() {}, path + "crList");
    }

    public static Ability addAbility(int monsterId) {
        return putSingleResult(Ability.class, path + monsterId + "/addAbility", null);
    }

    public static Action addAction(int monsterId) {
        return putSingleResult(Action.class, path + monsterId + "/addAction", null);
    }

    public static LegendaryAction addLegendaryAction(int monsterId) {
        return putSingleResult(LegendaryAction.class, path + monsterId + "/addLegendaryAction", null);
    }

    public static void deleteAbility(int monsterId, int index) {
        deleteNoResult(path + monsterId + "/abilities/" + index);
    }

    public static void deleteAction(int monsterId, int actionId) {
        deleteNoResult(path + monsterId + "/actions/" + actionId);
    }

    public static void deleteLegendaryAction(int monsterId, int legendaryActionId) {
        deleteNoResult(path + monsterId + "/legendaryActions/" + legendaryActionId);
    }
}
