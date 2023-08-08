package gr.btc;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.*;

public class TownAddition implements Serializable {
    private UUID soowner = null;
    private List<Army> invitedArmys = new ArrayList<>();
    private List<String> townItemMap = new ArrayList<>();
    private Map<ChunkCoord, Integer> chunkPriorityMap = new HashMap<>();
    private boolean contractBook = false;
    public TownAddition() {
        BookTownControl.saveTownAdditionMap();
    }
    public List<String> getTownItemMap() {
        return townItemMap;
    }
    public void setTownItemMap(List<String> townItemMap) {
        this.townItemMap = townItemMap;
        BookTownControl.saveTownAdditionMap();
    }
    public Map<ChunkCoord, Integer> getChunckPriorityMap() {
        return chunkPriorityMap;
    }
    public void addChunckPriorityMap(ChunkCoord chunkCoord, int priority) {
        this.chunkPriorityMap.put(chunkCoord, priority);
        BookTownControl.saveTownAdditionMap();
    }
    public boolean isContractBook() {
        return contractBook;
    }
    public void setContractBook(boolean contractBook) {
        this.contractBook = contractBook;
        BookTownControl.saveTownAdditionMap();
    }

    public UUID retso() {
        return soowner;
    }
    public void setco(UUID player) {
        soowner = player;
    }
    public boolean isArmyInvited(Army army) {

        if (invitedArmys.contains(army)) {
            invitedArmys.remove(army);
            return true;
        }

        return false;
    }

    public void inviteArmy(Army army) {
        if (invitedArmys.contains(army)) {
            invitedArmys.remove(army);
            invitedArmys.add(army);
        }
        else {
            invitedArmys.add(army);
        }


        // Запланировать удаление приглашения через 10 минут
        Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), () -> {
            invitedArmys.remove(army);
        }, 10 * 60 * 20); // 10 минут в тиках
    }
}
