package gr.btc;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.*;

public class Army implements Serializable {
    private String name;
    private List<ArmyPlayer> players;
    private static final long serialVersionUID = 7512600613827387231L;
    private List<UUID> connectedCountrys = new ArrayList<>();
    private boolean isOpen;

    private List<UUID> invitedPlayers = new ArrayList<>();
    private List<UUID> invitedCountries = new ArrayList<>();
    private Map<UUID, UUID> LinkedPlayers = new HashMap<>(); //Player, Town
    public Army(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        this.isOpen = false;
        BookTownControl.saveArmies();
    }

    public String getName() {
        return name;
    }

    public List<ArmyPlayer> getPlayers() {
        return players;
    }

    public ArmyPlayer getPlayer(Player player) {
        for (int i = 0; i < players.size(); i++) {
            Army.ArmyPlayer armyPlayer = players.get(i);
            if (armyPlayer.getUUID().equals(player.getUniqueId())) {
                return armyPlayer;
            }
        }
        return null;
    }

    public UUID GetLink(ArmyPlayer player) {
        return LinkedPlayers.getOrDefault(player.getPlayer().getUniqueId(), null);
    }

    public void SetLink(Player player, UUID town) {
        LinkedPlayers.put(player.getUniqueId(),town);
        BookTownControl.saveArmies();
    }

    public List<UUID> GetConnectedCountrys() {
        return connectedCountrys;
    }
    public boolean IsCountryConnected(Town town) {
        if(connectedCountrys.contains(town.getUUID())) {
            return true;
        }
        else {
            return false;
        }
    }
    public void ConnectCountry(Town town) {
        if(connectedCountrys.contains(town.getUUID())) {
            connectedCountrys.remove(town.getUUID());
            connectedCountrys.add(town.getUUID());
        }
        else {
            connectedCountrys.add(town.getUUID());
        }
        BookTownControl.saveArmies();
    }

    public void UnConnectCountry(Town town) {
        if(connectedCountrys.contains(town.getUUID())) {
            connectedCountrys.remove(town.getUUID());
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public void invitePlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        if(invitedPlayers.contains(player.getUniqueId())) {
            invitedPlayers.remove(playerUUID);
            invitedPlayers.add(playerUUID);
        }
        else {
            invitedPlayers.add(playerUUID);
        }


        // Запланировать удаление приглашения через 10 минут
        Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), () -> {
            invitedPlayers.remove(playerUUID);
        }, 10 * 60 * 20); // 10 минут в тиках
    }
    public boolean isCountryInvited(Town town) {
        UUID townUUID = town.getUUID();

        if (invitedCountries.contains(townUUID)) {
            invitedCountries.remove(townUUID);
            return true;
        }

        return false;
    }

    public void inviteCountry(Town town) {
        UUID townUUID = town.getUUID();
        if (invitedCountries.contains(townUUID)) {
            invitedCountries.remove(townUUID);
            invitedCountries.add(townUUID);
        }
        else {
            invitedCountries.add(townUUID);
        }


        // Запланировать удаление приглашения через 10 минут
        Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), () -> {
            invitedCountries.remove(townUUID);
        }, 10 * 60 * 20); // 10 минут в тиках
    }
    public boolean isPlayerInvited(Player player) {
        UUID playerUUID = player.getUniqueId();
        if(invitedPlayers.contains(playerUUID)) {
            invitedPlayers.remove(playerUUID);
            return true;
        }

        return false;
    }
    public void addPlayer(UUID playerUUID, int priority) {
        try{
            removePlayer(playerUUID);
        }
        finally {

        }
        ArmyPlayer player = new ArmyPlayer(playerUUID, priority);
        players.add(player);
        BookTownControl.saveArmies();
    }

    public void removePlayer(UUID playerUUID) {
        players.removeIf(player -> player.getUUID().equals(playerUUID));
    }

    // Другие методы и функциональность для работы с армией

    public static class ArmyPlayer implements Serializable {
        private UUID uuid;
        private int priority;

        public ArmyPlayer(UUID uuid, int priority) {
            this.uuid = uuid;
            this.priority = priority;
        }

        public UUID getUUID() {
            return uuid;
        }

        public int getPriority() {
            return priority;
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(uuid);
        }
    }
}