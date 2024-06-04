package me.pk2.pherob;

import me.pk2.pherob.commands.CommandTest;
import me.pk2.pherob.listeners.PayloadListeners;
import me.pk2.pherob.manager.PayloadManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PHerobrine extends JavaPlugin {
    public static PHerobrine INSTANCE;
    private PayloadManager payloadManager;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.payloadManager = new PayloadManager();
        this.payloadManager.init();

        getCommand("htest").setExecutor(new CommandTest());

        Bukkit.getPluginManager().registerEvents(new PayloadListeners(), this);

        getLogger().info("lol");
    }

    public PayloadManager getPayloadManager() { return payloadManager; }
}