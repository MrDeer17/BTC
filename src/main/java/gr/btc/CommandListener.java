package gr.btc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
    private Player player;
    private boolean blockCommands;
    private CommandExecutedCallback onCommandExecuted;

    public CommandListener(Player player) {
        this.player = player;
    }

    public void setBlockCommands(boolean blockCommands) {
        this.blockCommands = blockCommands;
    }

    public void setOnCommandExecuted(CommandExecutedCallback callback) {
        this.onCommandExecuted = callback;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (blockCommands && event.getPlayer() == player) {
            event.setCancelled(true);
            if (onCommandExecuted != null) {
                onCommandExecuted.onCommandExecuted(event.getMessage());
            }
        }
    }

    public interface CommandExecutedCallback {
        void onCommandExecuted(String command);
    }
}