package gr.btc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredListener;

import java.util.concurrent.CompletableFuture;

public class ChatListener implements Listener {
    private final Player player;
    private final CompletableFuture<Void> future;
    private String response;
    private boolean chatEnabled;

    public ChatListener(Player player, CompletableFuture<Void> future) {
        this.player = player;
        this.future = future;
        this.chatEnabled = true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (chatEnabled && event.getPlayer() == player) {
            event.setCancelled(true); // Cancel the chat message
            response = event.getMessage();
            future.complete(null); // Notify completion
        }
    }

    public String getResponse() {
        return response;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public void unregisterChatListener(ChatListener chatListener) {
        HandlerList.unregisterAll(chatListener);
    }
}
