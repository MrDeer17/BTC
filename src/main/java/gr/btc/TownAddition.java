package gr.btc;

import com.palmergames.bukkit.towny.object.WorldCoord;

import java.io.Serializable;
import java.util.*;

public class TownAddition implements Serializable {
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
}
