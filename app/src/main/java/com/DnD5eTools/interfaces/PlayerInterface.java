package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.PlayerCharacter;
import com.DnD5eTools.util.Util;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class PlayerInterface extends AbstractInterface {
    private static final String path = "5eTools/api/pc/";

    public static List<PlayerCharacter> getPlayerList() {
        return getListResult(new TypeReference<List<PlayerCharacter>>() {}, path + "campaignList/"
                + Util.getCampaignId());
    }

    public static void updatePlayer(PlayerCharacter playerCharacter) {
        postNoResult(path + playerCharacter.getId() +
                        "?rolledInitiative=" + playerCharacter.getRolledInitiative() +
                        "&initiativeBonus=" + playerCharacter.getInitiativeBonus() +
                        "&combatant=" + playerCharacter.isCombatant(),
                playerCharacter);
    }
}
