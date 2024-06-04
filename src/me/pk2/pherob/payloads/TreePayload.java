package me.pk2.pherob.payloads;

import es.eltrueno.npc.TruenoNPC;
import me.pk2.pherob.PHerobrine;
import me.pk2.pherob.util.BlockUtil;
import me.pk2.pherob.util.Tree;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static me.pk2.pherob.util.NPCUtil.spawnHerobrine;

public class TreePayload extends Payload {
    private final Random random = new Random();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private Player seeking;
    private TruenoNPC npc;

    private boolean spawneable(Block block) {
        return block.getType() == Material.AIR
                && block.getRelative(BlockFace.DOWN).getType() != Material.AIR
                && block.getRelative(BlockFace.UP).getType() == Material.AIR;
    }

    public static boolean isClose(Location nl, Location loc, int r) {
        if(!loc.getWorld().getName().equals(nl.getWorld().getName()))
            return false;
        int lx = loc.getBlockX();
        int ly = loc.getBlockY();
        int lz = loc.getBlockZ();

        int nx = nl.getBlockX();
        int ny = nl.getBlockY();
        int nz = nl.getBlockZ();

        int dx = Math.abs(lx - nx);
        int dy = Math.abs(ly - ny);
        int dz = Math.abs(lz - nz);

        return dx < r && dy < r && dz < r;
    }

    @Override
    void run(Player p) {
        this.tasks.clear();
        this.seeking = p;

        World world = p.getWorld();

        Location loc = p.getLocation();
        int lx = loc.getBlockX();
        int ly = loc.getBlockY();
        int lz = loc.getBlockZ();

        ArrayList<Block> blocks = new ArrayList<>();
        ArrayList<Tree> trees = new ArrayList<>();
        if(ly > 50) {
            for(int x = lx-32; x <= lx+32; x++) {
                for(int y = ly-10; y <= ly+30; y++) {
                    if(y < 1 || y > 254)
                        continue;

                    z:
                    for(int z = lz-32; z <= lz+32; z++) {
                        Block base = world.getBlockAt(x, y, z);
                        if(base == null || (base.getType() != Material.LOG && base.getType() != Material.LOG_2))
                            continue;

                        for(Block b : blocks)
                            if(b.getX() == base.getX() && b.getZ() == base.getZ())
                                continue z;
                        blocks.add(base);
                    }
                }
            }
        }

        for(Block block : blocks) {
            Tree tree = Tree.getTreeAt(block);
            if(tree == null)
                continue;

            trees.add(tree);
        }

        blocks.clear();

        ArrayList<Location> canSpawn = new ArrayList<>();
        for(Tree tree : trees) {
            Block base = tree.getBaseBlock();

            Block north = base.getRelative(BlockFace.NORTH);
            Block east = base.getRelative(BlockFace.EAST);
            Block south = base.getRelative(BlockFace.SOUTH);
            Block west = base.getRelative(BlockFace.WEST);
            Block add = null;

            nesw: {
                if (spawneable(north)) {
                    add = north;
                    break nesw;
                }

                if (spawneable(east)) {
                    add = east;
                    break nesw;
                }

                if (spawneable(south)) {
                    add = south;
                    break nesw;
                }

                if (spawneable(west))
                    add = west;
            }

            if(add != null)
                canSpawn.add(add.getLocation());
        }

        if(canSpawn.isEmpty()) {
            running = false;
            failed = true;
            return;
        }
        int rIdx = random.nextInt(canSpawn.size());
        Location spawn = canSpawn.get(rIdx);

        float[] yawPitch = BlockUtil.calculateYawPitch(spawn, p.getLocation());
        spawn.setYaw(yawPitch[0]);
        spawn.setPitch(yawPitch[1]);

        npc = spawnHerobrine(spawn.add(0.5, 0, 0.5));
        PHerobrine.INSTANCE.getLogger().info("{TREE} Seeking " + p.getName() + " at [" + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() + "]");

        tasks.add(Bukkit.getScheduler().runTaskLater(PHerobrine.INSTANCE, () -> {
            if(!running)
                return;
            npc.delete();
            running = false;

            PHerobrine.INSTANCE.getLogger().info("{TREE} Unseeking");
            tasks.forEach(BukkitTask::cancel);
        }, 20L*40));
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
        if(npc.isDeleted() || !isClose(npc.getLocation(), p.getLocation(), 25))
            return;

        //if(BlockUtil.isInsideCone(npc.getLocation(), p.getEyeLocation(), 20, 90, p.getLocation().getYaw()) && !stopping) {
        if(BlockUtil.isInFOV(p, npc.getLocation(), 130) && !stopping) {
            int sec = random.nextInt(3)+1;
            stopping = true;

            PHerobrine.INSTANCE.getLogger().info("{TREE} Unseeking in " + sec + "s");
            tasks.add(Bukkit.getScheduler().runTaskLater(PHerobrine.INSTANCE, () -> {
                if(!running)
                    return;
                npc.delete();
                running = false;
                stopping = false;

                PHerobrine.INSTANCE.getLogger().info("{TREE} Unseeking");
                tasks.forEach(BukkitTask::cancel);
            }, 20L * sec));
        }
    }
}