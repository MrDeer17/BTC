package gr.btc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class BookTownControl extends JavaPlugin {
    public static final String SAVE_TOWN_ADDITION_MAP_FILE_PATH = "plugins/BookTownControl/townAdditionMap.dat";
    public static final String SAVE_ARMYS_FILE_PATH = "plugins/BookTownControl/armys.dat";
    public static Map<UUID, TownAddition> townAddition = new HashMap<>();
    public static List<Army> Armys = new ArrayList<>();
    @Override
    public void onEnable() {
        loadTownAdditionMap();
        loadArmies();
        /*loadTownItemMap();*/
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new OnBookWrite(), this);
        getServer().getPluginManager().registerEvents(new OtherEvents(), this);
        getServer().getPluginManager().registerEvents(new InventoryManager(getPlugin(BookTownControl.class)), this);
        getServer().getPluginManager().registerEvents(new CountryInventoryManager(getPlugin(BookTownControl.class)), this);
        Objects.requireNonNull(getCommand("country")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("amendlaws")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("laws")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("tinvite")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("menu")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("army")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("openbookinmainhand")).setExecutor(new cmds());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveTownAdditionMap();
        saveArmies();
    }
    public static void saveTownAdditionMap() {
        try {
            File folder = new File("plugins/BookTownControl");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            FileOutputStream fileOut = new FileOutputStream(SAVE_TOWN_ADDITION_MAP_FILE_PATH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(townAddition);
            objectOut.close();
            fileOut.close();

            System.out.println("Map of TownAddition successfully saved to file townAdditionMap.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadTownAdditionMap() {
        try {
            FileInputStream fileIn = new FileInputStream(SAVE_TOWN_ADDITION_MAP_FILE_PATH);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            townAddition = (Map<UUID, TownAddition>) objectIn.readObject();
            objectIn.close();
            fileIn.close();

            System.out.println("File townAdditionMap.dat loaded");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void saveArmies() {
        try {
            File folder = new File("plugins/BookTownControl");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            FileOutputStream fileOut = new FileOutputStream(SAVE_ARMYS_FILE_PATH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(Armys);
            objectOut.close();
            fileOut.close();

            System.out.println("Список армий успешно сохранен в файл armys.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadArmies() {
        try {
            FileInputStream fileIn = new FileInputStream(SAVE_ARMYS_FILE_PATH);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Armys = (List<Army>) objectIn.readObject();
            objectIn.close();
            fileIn.close();

            System.out.println("Загружен файл armys.dat");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int GetAllDefaultChunks(Town town) {
        ChunkCoord wc;
        int result = 0;
        TownAddition ta = townAddition.get(town.getUUID());
        for(TownBlock pg : town.getTownBlocks()) {
            wc = new ChunkCoord(pg.getWorldCoord().getX(),pg.getWorldCoord().getZ(),pg.getWorldCoord().getWorldName());
            if(!ta.getChunckPriorityMap().containsKey(wc) || ta.getChunckPriorityMap().get(wc) == 0) {
                result++;
            }
        }
        return result;

    }
    public static int GetAllBasedChunks(Town town) {
        ChunkCoord wc;
        int result = 0;
        TownAddition ta = townAddition.get(town.getUUID());
        for(TownBlock pg : town.getTownBlocks()) {
            wc = new ChunkCoord(pg.getWorldCoord().getX(),pg.getWorldCoord().getZ(),pg.getWorldCoord().getWorldName());
            if(ta.getChunckPriorityMap().containsKey(wc) && ta.getChunckPriorityMap().get(wc) == 1) {
                result++;
            }
        }
        return result;
    }

    public static int GetRemainsDefaultChunks(Town town) {
        int all = GetAllDefaultChunks(town);
        int chscount = 50;
        for(Resident r : town.getResidents()) {
            if(!r.isMayor()) {
                chscount += 5;
            }

        }
        return chscount-all;
    }
    public static int GetRemainsBasedChunks(Town town) {
        int all = GetAllBasedChunks(town);
        int chscount = 25;
        for(Resident r : town.getResidents()) {
            if(!r.isMayor()) {
                chscount += 25;
            }

        }
        return chscount-all;
    }

    public static Army GetArmyPlayerIn(Player p) {
        for (Army army : Armys) {
            for (Army.ArmyPlayer armyPlayer : army.getPlayers()) {
                if (armyPlayer != null && armyPlayer.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                    return army;
                }
            }
        }
        return null;
    }
    public static Army getArmyByName(String name) {
        for (Army army : Armys) {
            if (army.getName().equalsIgnoreCase(name)) {
                return army;
            }
        }
        return null;
    }
}

