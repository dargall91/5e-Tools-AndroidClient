package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.Music;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class MusicInterface extends AbstractInterface {
    private static final String path = "5eTools/api/music/";

    public static void playMusic(int musicId) {
        postNoResult(path + "play?musicId=" + musicId, null);
    }

    public static void pauseMusic() {
        postNoResult(path + "pause", null);
    }

    public static void stopMusic() {
        postNoResult(path + "stop", null);
    }

    public static List<Music> getMusicList() {
        return getListResult(new TypeReference<List<Music>>() {}, path + "list");
    }
}
