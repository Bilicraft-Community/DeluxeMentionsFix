package com.bilicraft.deluxementionsfix;

import org.bukkit.Bukkit;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DeluxeMentionsFix extends JavaPlugin implements Listener {
    private final Set<RegisteredListener> deluxeMentionsListener = new HashSet<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this,this);
        deluxeMentionsListener.clear();
        scanListener();
    }

    @Override
    public void onDisable() {
        // Re-register listeners to make sure hot-reload DeluxeMentionsFix working correctly.
        AsyncPlayerChatEvent.getHandlerList().registerAll(deluxeMentionsListener);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event){
        deluxeMentionsListener.forEach(registeredListener -> {
            try {
                registeredListener.callEvent(event);
            } catch (EventException e) {
                e.printStackTrace();
            }
        });
    }

    private void scanListener(){
        List<RegisteredListener> pendingToUnregister = new ArrayList<>();
        for (RegisteredListener registeredListener : AsyncPlayerChatEvent.getHandlerList().getRegisteredListeners()) {
            if(registeredListener.getPlugin().getName().equals("DeluxeMentions")){
               deluxeMentionsListener.add(registeredListener);
               pendingToUnregister.add(registeredListener);
               getLogger().info("Unregistered: "+registeredListener.getListener().getClass());
            }
        }
        pendingToUnregister.forEach(listener -> AsyncPlayerChatEvent.getHandlerList().unregister(listener));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginLoaded(PluginEnableEvent event){
        if(event.getPlugin().getName().equals("DeluxeMentions")){
            // Execute in next tick to make sure DM's listener registered.
            Bukkit.getScheduler().runTask(this, this::scanListener);
        }

    }
}
