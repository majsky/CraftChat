package me.majsky.bukkit.craftChat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    
    @EventHandler
    public void chat(AsyncPlayerChatEvent event){
        if(CraftChat.instance.server != null)
            CraftChat.instance.server.dispatch(event.getMessage(), event.getPlayer().getName());
    }
    
    @EventHandler
    public void join(PlayerJoinEvent e){
        if(CraftChat.instance.server != null)
            CraftChat.instance.server.dispatch(e.getJoinMessage(), "SERVER");
    }
    
    @EventHandler
    public void leave(PlayerQuitEvent e){
        if(CraftChat.instance.server != null)
            CraftChat.instance.server.dispatch(e.getQuitMessage(), "SERVER");
    }
}
