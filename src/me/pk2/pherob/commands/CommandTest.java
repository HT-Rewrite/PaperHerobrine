package me.pk2.pherob.commands;

import es.eltrueno.npc.TruenoNPC;
import me.pk2.pherob.PHerobrine;
import me.pk2.pherob.payloads.Payload;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player))
            return false;
        Player p = (Player) sender;

        Payload tree = PHerobrine.INSTANCE.getPayloadManager().get("tree");
        if(!tree.isRunning())
            tree.setRunning(p);
        return true;
    }
}