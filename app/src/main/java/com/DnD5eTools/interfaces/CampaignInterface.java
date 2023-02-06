package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.Campaign;
import com.DnD5eTools.util.Util;

public class CampaignInterface extends AbstractInterface {
    private static final String path = "5eTools/api/campaign/";

    public static Campaign getActiveCampaign() {
        Util.setCampaign(getSingleResult(Campaign.class, path + "getActive"));
        return Util.getCampaign();
    }
}
