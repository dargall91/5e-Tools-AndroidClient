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
    private static final String path = "/monster";

    public static List<NameIdProjection> getMonsterList() {
        return getListResult(new TypeReference<List<NameIdProjection>>() {}, path + "/all?archived=false");
    }

    public static Monster getMonster(int monsterId) {
        return getSingleResult(Monster.class, path + "/" + monsterId);
    }

    public static NameIdProjection addMonster(String name) {
        return putSingleResult(NameIdProjection.class, path + "?name=" + name, null);
    }

    public static void updateMonster(Monster monster) {
        postNoResult(path + "update", monster);
    }

    public static void archiveMonster(int monsterId) {
        postNoResult(path + "/" + monsterId + "/archive", null);
    }

    public static Monster copyMonster(int monsterId, String name) {
        return putSingleResult(Monster.class, path + "/" + monsterId + "/copy?name=" + name, null);
    }

    public static List<ChallengeRating> getChallengeRatings() {
        return getListResult(new TypeReference<List<ChallengeRating>>() {}, path + "challenge-ratings");
    }
}
