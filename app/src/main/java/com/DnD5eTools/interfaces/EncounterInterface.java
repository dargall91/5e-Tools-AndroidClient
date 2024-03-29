package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.encounter.Encounter;
import com.DnD5eTools.entities.encounter.XpThresholds;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class EncounterInterface extends AbstractInterface {
    private static final String path = "5eTools/api/encounter/";

    public static List<NameIdProjection> getEncounterList() {
        return getListResult(new TypeReference<List<NameIdProjection>>() {}, path + "list");
    }

    public static NameIdProjection addEncounter(String name) {
        return putSingleResult(NameIdProjection.class, path + "add?name=" + name, null);
    }

    public static Encounter getEncounter(int encounterId) {
        return getSingleResult(Encounter.class, path + encounterId);
    }

    public static void updateEncounter(Encounter encounter) {
        postNoResult(path + "update", encounter);
    }

    public static void archiveEncounter(int encounterId) {
        postNoResult(path + encounterId + "/archive", null);
    }

    public static List<XpThresholds> getXpThresholds() {
        return getListResult(new TypeReference<List<XpThresholds>>() {}, path + "xp");
    }
}
