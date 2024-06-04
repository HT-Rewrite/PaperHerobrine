package me.pk2.pherob.util;

import es.eltrueno.npc.TruenoNPC;
import es.eltrueno.npc.TruenoNPCApi;
import es.eltrueno.npc.skin.TruenoNPCSkinBuilder;
import me.pk2.pherob.PHerobrine;
import org.bukkit.Location;

public class NPCUtil {
    public static TruenoNPC spawnHerobrine(Location location) {
        return TruenoNPCApi.createNPC(PHerobrine.INSTANCE, location, TruenoNPCSkinBuilder.fromMineskin(PHerobrine.INSTANCE, 1100348385));
    }
}