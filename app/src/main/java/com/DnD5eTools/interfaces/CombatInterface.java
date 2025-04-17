package com.DnD5eTools.interfaces;

import com.DnD5eTools.models.combatants.Combatant;
import com.DnD5eTools.models.combatants.CombatantDto;
import com.DnD5eTools.util.Util;

import java.util.ArrayList;
import java.util.List;

public class CombatInterface extends AbstractInterface {
    private static final String path = "/combatant";

    public static void updateCombatantList(List<Combatant> combatantList) {
        List<CombatantDto> serverCombatantList = new ArrayList<>();
        for (int i = 0; i < combatantList.size(); i++) {
            //add to list if: alive, not reinforcement, not invisible, not a lair action
            if (combatantList.get(i).isAlive()
                    && !combatantList.get(i).isReinforcement()
                    && !combatantList.get(i).isInvisible()
                    && !combatantList.get(i).isLairAction()) {
                serverCombatantList.add(new CombatantDto(serverCombatantList.size() + 1,
                        combatantList.get(i).getServerName()));
            }
        }

        postNoResult(path, serverCombatantList);
    }

    public static void endCombat() {
        Util.unloadEncounter();
        postNoResult(path, new ArrayList<CombatantDto>());
    }
}
