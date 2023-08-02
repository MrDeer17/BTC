package gr.btc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Consumer;

public class ChatListener implements Listener {
    private final Player player;
    private boolean chatEnabled;
    private Consumer<String> onChatMessageReceived;

    public ChatListener(Player player) {
        this.player = player;
        this.chatEnabled = false;
    }

    public void setChatEnabled(boolean enabled) {
        this.chatEnabled = enabled;
    }

    public void setOnChatMessageReceived(Consumer<String> onChatMessageReceived) {
        this.onChatMessageReceived = onChatMessageReceived;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer() == player && chatEnabled) {
            event.setCancelled(true);
            String message = event.getMessage();
            if (onChatMessageReceived != null) {
                onChatMessageReceived.accept(message);
            }
        }
    }
}
