package gr.btc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    public static final String SAVE_WARS_FILE_PATH = "plugins/BookTownControl/wars.dat";
    public static Map<UUID, TownAddition> townAddition = new HashMap<>();
    public static List<Army> Armys = new ArrayList<>();
    public static List<War> Wars = new ArrayList<>();
    @Override
    public void onEnable() {
        loadTownAdditionMap();
        loadArmies();
        loadWars();
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

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                // Ваш код проверки, который должен выполняться каждые 10 минут
                // Например, вызов метода TryToEndWar()
                TryToEndAllWars();
            }
        }, 0, 20); // 10*60*20
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveTownAdditionMap();
        saveArmies();
        saveWars();
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

    public static void saveWars() {
        try {
            File folder = new File("plugins/BookTownControl");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            FileOutputStream fileOut = new FileOutputStream(SAVE_WARS_FILE_PATH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(Wars);
            objectOut.close();
            fileOut.close();

            System.out.println("List of wars successfully saved to file wars.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadWars() {
        try {
            FileInputStream fileIn = new FileInputStream(SAVE_WARS_FILE_PATH);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Wars = (List<War>) objectIn.readObject();
            objectIn.close();
            fileIn.close();

            System.out.println("File wars.dat loaded");
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

    public static Army GetArmyPlayerIn(OfflinePlayer p) {
        for (Army army : Armys) {
            for (Army.ArmyPlayer armyPlayer : army.getPlayers()) {
                if (armyPlayer.getPlayer() != null && armyPlayer.getPlayer().getUniqueId().equals(p.getUniqueId())) {
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

    public static void TryToEndAllWars() {
        for (War war : BookTownControl.Wars) {
            if(war.TryToEndWar()) {
                saveWars();
            }
        }
    }
    public static War FireNewWar(Town town1, Town town2) {
        War war = new War(town1.getUUID(),town2.getUUID());
        Wars.add(war);
        return war;
    }
    public static boolean CheckForWar(Town town) {
        if(town == null) {
            return false;
        }
        UUID townt = town.getUUID();
        if(townt == null) {
            return false;
        }
        for (War war : BookTownControl.Wars) {
                if(war.sides1.get(0).equals(townt) || war.sides2.get(0).equals(townt)) {
                    return true;
                }

        }
        return false;
    }

    public static List<War> CheckForWarsInArmy(Army army) {
        List<War> wars = new ArrayList<>();
        for (War war : BookTownControl.Wars) {
            if(war.GetArmy().contains(army)) {
                wars.add(war);
            }
        }
        return wars;
    }
    public static War CheckForWarInTown(Town town) {
        for (War war : BookTownControl.Wars) {
            if(war.GetTowns().contains(town)) {
                return war;
            }
        }
        return null;
    }
    public static Town IsPlayerInWar(Player player) {
        List<Town> towns = new ArrayList<>();
        for (War war : BookTownControl.Wars) {
            if(war.side1Warriors.contains(player) || war.side2Warriors.contains(player)) {
                return war.ReturnTownByPlayer(player);
            }
        }
        return null;
    }

}

