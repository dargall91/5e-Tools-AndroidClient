package com.DnD5eTools.interfaces;

import com.DnD5eTools.models.combatants.Combatant;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.DnD5eTools.util.Util;

import java.util.ArrayList;
import java.util.List;

public class CombatInterface extends AbstractInterface {
    private static final String path = "5eTools/api/combat/";

    public static void updateCombatantList(List<Combatant> combatantList) {
        List<NameIdProjection> serverCombatantList = new ArrayList<>();
        for (int i = 0; i < combatantList.size(); i++) {
            //add to list if: alive, not reinforcement, not invisible
            if (combatantList.get(i).isAlive() && !combatantList.get(i).isReinforcement() && !combatantList.get(i).isInvisible()) {
                serverCombatantList.add(new NameIdProjection(serverCombatantList.size() + 1,
                        combatantList.get(i).getName()));
            }
        }

        postNoResult(path + "setCombatants", serverCombatantList);
    }

    public static void endCombat() {
        Util.unloadEncounter();
        postNoResult(path + "endCombat", null);
    }
}
