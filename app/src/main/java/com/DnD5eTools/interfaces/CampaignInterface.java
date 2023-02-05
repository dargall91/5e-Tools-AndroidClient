package com.DnD5eTools.interfaces;

import static com.DnD5eTools.util.Util.getServerConnection;

import com.DnD5eTools.entities.Campaign;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class CampaignInterface {
    private static final String path = "5eTools/api/campaign/";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Campaign getActiveCampaign() {
        AtomicReference<Campaign> campaign = new AtomicReference<>();

        new Thread(() -> {
            try {
                URL url = new URL(getServerConnection().getUrl() + path + "getActive");
                campaign.set(mapper.readValue(url, Campaign.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return campaign.get();
    }
}
