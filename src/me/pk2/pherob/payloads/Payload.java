package me.pk2.pherob.payloads;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Payload implements Listener {
    protected boolean running = false;
    protected boolean failed = false;
    public boolean isRunning() { return running; }
    public void setRunning(Player p) {
        this.running = true;
        this.failed = false;
        run(p);
    }

    abstract void run(Player p);
}