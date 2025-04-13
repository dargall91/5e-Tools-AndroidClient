package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.PlayerCharacter;
import com.DnD5eTools.models.ResponseWrapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class PlayerInterface extends AbstractInterface {
    private static final String path = "/player-character/combatant";

    public static List<PlayerCharacter> getPlayerList() {
        return getListResult(new TypeReference<ResponseWrapper<List<PlayerCharacter>>>() {}, path);
    }

    public static void updatePlayer(PlayerCharacter playerCharacter) {
        postNoResult(path, playerCharacter);
    }
}
