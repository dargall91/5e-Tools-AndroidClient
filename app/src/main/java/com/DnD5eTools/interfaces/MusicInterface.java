package com.DnD5eTools.interfaces;

import static com.DnD5eTools.util.Util.getServerConnection;

import com.DnD5eTools.entities.Music;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MusicInterface {
    private static final String path = "5eTools/api/music/";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Music> getMusicList() {
        AtomicReference<Music[]> musicList = new AtomicReference<>();

        new Thread(() -> {
            try {
                URL url = new URL(getServerConnection().getUrl() + path + "getActive");
                musicList.set(mapper.readValue(url, Music[].class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return Arrays.asList(musicList.get());
    }
}
