package com.DnD5eTools.util;

import com.DnD5eTools.models.ServerConnection;

public class Util {
    private static ServerConnection serverConnection;

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static void setServerConnection(ServerConnection serverConnection) {
        Util.serverConnection = serverConnection;
    }

    public static boolean isConnectedToServer() {
        return CampaignManager.getActiveCampaign() != null;
    }
}
