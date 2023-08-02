package gr.btc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class BookTownControl extends JavaPlugin {
    public static final String SAVE_FILE_PATH = "plugins/BookTownControl/townItemMap.dat";
    public static Map<UUID, List<String>> townItemMap = new HashMap<>();
    public static Map<WorldCoord, Integer> ChunckPriorityMap = new HashMap<>(); //Chunck, Priority ID - 0 - NonSafe, 1 - Safe
    @Override
    public void onEnable() {
        loadTownItemMap();
        /*loadTownItemMap();*/
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new OnBookWrite(), this);
        getServer().getPluginManager().registerEvents(new OtherEvents(), this);
        Objects.requireNonNull(getCommand("countryremove")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("amendlaws")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("laws")).setExecutor(new cmds());
        Objects.requireNonNull(getCommand("tinvite")).setExecutor(new cmds());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveTownItemMap();
    }
    public static void saveTownItemMap() {
        try {
            File folder = new File("plugins/BookTownControl");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            // Создаем объект FileOutputStream для записи в файл
            FileOutputStream fileOut = new FileOutputStream(SAVE_FILE_PATH);

            // Создаем объект ObjectOutputStream для записи объекта в файл
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

            // Записываем объект townItemMap в файл
            objectOut.writeObject(townItemMap);

            // Закрываем потоки
            objectOut.close();
            fileOut.close();

            System.out.println("Переменная townItemMap успешно сохранена в файл townItemMap.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTownItemMap() {
        try {
            // Создаем объект FileInputStream для чтения из файла
            FileInputStream fileIn = new FileInputStream(SAVE_FILE_PATH);

            // Создаем объект ObjectInputStream для чтения объекта из файла
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            // Читаем объект townItemMap из файла
            townItemMap = (Map<UUID, List<String>>) objectIn.readObject();

            // Закрываем потоки
            objectIn.close();
            fileIn.close();

            System.out.println("Loaded townItemMap.dat");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int GetAllBasedChunks(Town town) {
        WorldCoord wc;
        int result = 0;
        for(TownBlock pg : town.getTownBlocks()) {
            wc = pg.getWorldCoord();
            if(!ChunckPriorityMap.containsKey(wc) || ChunckPriorityMap.get(wc) == 0) {
                result++;
            }
        }
        return result;

    }
    public static int GetAllDefaultChunks(Town town) {
        WorldCoord wc;
        int result = 0;
        for(TownBlock pg : town.getTownBlocks()) {
            wc = pg.getWorldCoord();
            if(ChunckPriorityMap.containsKey(wc) && ChunckPriorityMap.get(wc) == 1) {
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
}

