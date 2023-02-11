package com.DnD5eTools.util;

import com.DnD5eTools.entities.Campaign;
import com.DnD5eTools.entities.Music;
import com.DnD5eTools.entities.encounter.Encounter;
import com.DnD5eTools.entities.monster.ChallengeRating;
import com.DnD5eTools.interfaces.CampaignInterface;
import com.DnD5eTools.interfaces.MonsterInterface;
import com.DnD5eTools.interfaces.MusicInterface;
import com.DnD5eTools.models.ServerConnection;
import com.DnD5eTools.models.projections.NameIdProjection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class to store any data that may be needed by multiple different classes
 */
public class Util {
    private static ServerConnection serverConnection;
    private static Campaign campaign;
    private static Encounter loadedEncounter;
    private static List<NameIdProjection> monsterList;
    private static List<String> monsterNameList;
    private static List<ChallengeRating> challengeRatingList;
    private static List<String> challengeRatingCrList;
    private static List<Integer> challengeRatingXpList;

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static void setServerConnection(ServerConnection serverConnection) {
        Util.serverConnection = serverConnection;
    }

    public static boolean isConnectedToServer() {
        if (campaign != null) {
            return true;
        }

        campaign = CampaignInterface.getActiveCampaign();
        return campaign != null;
    }

    public static int getCampaignId() {
        return campaign.getId();
    }

    public static void setCampaign(Campaign campaign) {
        Util.campaign = campaign;
    }

    public static Campaign getCampaign() {
        return campaign;
    }

    public static void loadEncounter(Encounter encounter) {
        loadedEncounter = encounter;

        if (loadedEncounter != null) {
            MusicInterface.playMusic(loadedEncounter.getMusic().getId());
        }
    }

    public static boolean isEncounterLoaded() {
        return loadedEncounter != null;
    }

    public static Encounter getLoadedEncounter() {
        return loadedEncounter;
    }

    public static List<NameIdProjection> getMonsterList() {
        if (monsterList == null) {
            setMonsterList();
        }

        return monsterList;
    }

    public static void setMonsterList() {
        Util.monsterList = MonsterInterface.getMonsterList();
    }

    public static List<String> getMonsterNameList() {
        //ensure both lists are initialized and same size, otherwise update this list
        if (monsterNameList == null || monsterNameList.size() != getMonsterList().size()) {
            monsterNameList = getMonsterList().stream()
                    .map(NameIdProjection::getName)
                    .collect(Collectors.toList());
        }

        return monsterNameList;
    }

    private static List<ChallengeRating> getChallengeRatingList() {
        if (challengeRatingList == null) {
            challengeRatingList = MonsterInterface.getChallengeRatings();
        }

        return challengeRatingList;
    }

    public static String[] getChallengeRatingCrList() {
        if (challengeRatingCrList == null) {
            challengeRatingCrList = getChallengeRatingList().stream()
                    .map(ChallengeRating::getCr)
                    .collect(Collectors.toList());
        }

        return challengeRatingCrList.toArray(new String[0]);
    }

    public static ChallengeRating getChallengeRating(int index) {
        return getChallengeRatingList().get(index);
    }
}
