package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.Campaign;
import com.DnD5eTools.util.Util;

public class CampaignInterface extends AbstractInterface {
    private static final String path = "/campaign";

    public static Campaign getActiveCampaign() {
        return getSingleResult(Campaign.class, path + "/active");
    }
}
