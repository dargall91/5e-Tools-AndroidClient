package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.Music;
import com.DnD5eTools.models.ResponseWrapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class MusicInterface extends AbstractInterface {
    private static final String path = "/music/";

    public static void playMusic(int musicId) {
        postNoResult(path + "play/" + musicId, null);
    }

    public static void pauseMusic() {
        postNoResult(path + "pause", null);
    }

    public static void stopMusic() {
        postNoResult(path + "stop", null);
    }

    public static List<Music> getMusicList() {
        return getListResult(new TypeReference<ResponseWrapper<List<Music>>>() {}, path + "all");
    }
}
