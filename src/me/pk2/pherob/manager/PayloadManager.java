package me.pk2.pherob.manager;

import me.pk2.pherob.PHerobrine;
import me.pk2.pherob.payloads.BehindPayload;
import me.pk2.pherob.payloads.Payload;
import me.pk2.pherob.payloads.TreePayload;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PayloadManager {
    private final HashMap<String, Payload> payloads;
    private final Random random;
    private BukkitTask executorTask;
    public PayloadManager() {
        this.payloads = new HashMap<>();
        this.payloads.put("tree", new TreePayload());
        this.payloads.put("behind", new BehindPayload());

        this.random = new Random();
    }

    public void init() {
        for(Payload payload : payloads.values())
            Bukkit.getPluginManager().registerEvents(payload, PHerobrine.INSTANCE);

        this.executorTask = Bukkit.getScheduler().runTaskTimer(PHerobrine.INSTANCE, () -> {
            if(Bukkit.getOnlinePlayers().isEmpty())
                return;
            int rand = random.nextInt(8);

            PHerobrine.INSTANCE.getLogger().info("$TICK{" + rand + "}$");
            if(rand == 5) {
                Collection<? extends Player> colP = Bukkit.getOnlinePlayers();
                Player p = ((List<? extends Player>) colP).get(random.nextInt(colP.size()));

                Payload[] arr = payloads.values().toArray(new Payload[0]);
                arr[random.nextInt(payloads.size())].setRunning(p);
            }
        }, 1L, 20L*60); // Every minute
    }

    public Payload get(String p) { return payloads.get(p); }
}