package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.PlayerCharacter;
import com.DnD5eTools.util.Util;

import java.util.Arrays;
import java.util.List;

public class PlayerInterface extends AbstractInterface {
    private static final String path = "5eTools/api/pc/";

    public static List<PlayerCharacter> getPlayerList() {
        return Arrays.asList(getArrayResult(PlayerCharacter[].class, path + "campaignList/"
                + Util.getCampaignId()));
    }

    public static void updatePlayer(PlayerCharacter playerCharacter) {
        postNoResult(path + "update", playerCharacter);
    }

    public static void deletePlayerCharacter(int id) {
        deleteNoResult(path + "delete?id=" + id);
    }

    public static void addPlayerCharacter(String name) {
        postNoResult(path + "add", new PlayerCharacter(name));
    }
}
