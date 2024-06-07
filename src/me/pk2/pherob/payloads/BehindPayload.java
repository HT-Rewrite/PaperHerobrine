package me.pk2.pherob.payloads;

import es.eltrueno.npc.TruenoNPC;
import me.pk2.pherob.PHerobrine;
import me.pk2.pherob.util.BlockUtil;
import me.pk2.pherob.util.Tree;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

import static me.pk2.pherob.util.NPCUtil.spawnHerobrine;

public class BehindPayload extends Payload {
    private final List<BukkitTask> tasks = new ArrayList<>();
    private Player seeking;
    private TruenoNPC npc;

    @Override
    void run(Player p) {
        this.tasks.clear();
        this.seeking = p;

        Location loc = p.getLocation();
        Location behind = loc.add(loc.getDirection().multiply(-1.5));

        if(behind.getBlock().getType() != Material.AIR) {
            behind = behind.add(0, 1, 0);
            if(behind.getBlock().getType() != Material.AIR) {
                running = false;
                failed = true;
                return;
            }
        }

        npc = spawnHerobrine(behind);
        PHerobrine.INSTANCE.getLogger().info("{BEHIND} Seeking " + p.getName() + " at [" + behind.getBlockX() + ", " + behind.getBlockY() + ", " + behind.getBlockZ() + "]");

        tasks.add(Bukkit.getScheduler().runTaskLater(PHerobrine.INSTANCE, () -> {
            npc.delete();
            running = false;

            PHerobrine.INSTANCE.getLogger().info("{BEHIND} Unseeking");
            tasks.forEach(BukkitTask::cancel);
        }, 20L*5));
    }

    private int moveCooldown = 0;
    private boolean stopping = false;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!running)
            return;

        moveCooldown++;
        if(moveCooldown > 2)
            moveCooldown = 1;
        if(moveCooldown != 1)
            return;

        Player p = event.getPlayer();
        if(npc.isDeleted())
            return;

        if(((seeking.getName().equals(p.getName()) && !TreePayload.isClose(npc.getLocation(), seeking.getLocation(), 8))
                || (TreePayload.isClose(npc.getLocation(), p.getLocation(), 16) && BlockUtil.isInFOV(p, npc.getLocation(), 130)))
                && !stopping) {
            stopping = true;
            tasks.add(Bukkit.getScheduler().runTaskLater(PHerobrine.INSTANCE, () -> {
                npc.delete();
                running = false;
                stopping = false;

                PHerobrine.INSTANCE.getLogger().info("{BEHIND} Forced unseek");
                tasks.forEach(BukkitTask::cancel);
            }, 2L));
        }
    }
}