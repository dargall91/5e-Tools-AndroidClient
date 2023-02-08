package com.DnD5eTools.interfaces;

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
}
