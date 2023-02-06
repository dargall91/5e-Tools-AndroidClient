package com.DnD5eTools.util;

import com.DnD5eTools.entities.Campaign;
import com.DnD5eTools.interfaces.CampaignInterface;
import com.DnD5eTools.models.ServerConnection;

public class Util {
    private static ServerConnection serverConnection;
    private static Campaign campaign;

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
}
