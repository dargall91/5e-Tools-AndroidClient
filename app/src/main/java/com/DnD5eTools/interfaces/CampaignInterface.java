package com.DnD5eTools.interfaces;

import com.DnD5eTools.entities.Campaign;
import com.DnD5eTools.models.ResponseWrapper;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.DnD5eTools.util.Util;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CampaignInterface extends AbstractInterface {
    private static final String path = "/campaign";

    public static Campaign getActiveCampaign() {
        return getSingleResult(new TypeReference<ResponseWrapper<Campaign>>() {}, path + "/active");
    }
}
