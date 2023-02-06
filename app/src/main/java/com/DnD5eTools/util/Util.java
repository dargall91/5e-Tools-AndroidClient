package com.DnD5eTools.util;

import com.DnD5eTools.entities.Campaign;
import com.DnD5eTools.entities.Music;
import com.DnD5eTools.entities.encounter.Encounter;
import com.DnD5eTools.interfaces.CampaignInterface;
import com.DnD5eTools.models.ServerConnection;

import java.util.List;

/**
 * A class to store any data that may be needed by multiple different classes
 */
public class Util {
    private static ServerConnection serverConnection;
    private static Campaign campaign;
    private static Encounter loadedEncounter;

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
    }

    public static boolean isEncounterLoaded() {
        return loadedEncounter != null;
    }

    public static Encounter getLoadedEncounter() {
        return loadedEncounter;
    }
}
