package gr.btc;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.*;
import java.util.*;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

public final class BookTownControl extends JavaPlugin {
    public static final String SAVE_TOWN_ADDITION_MAP_FILE_PATH = "plugins/BookTownControl/townAdditionMap.dat";
    public static final String SAVE_ARMYS_FILE_PATH = "plugins/BookTownControl/armys.dat";
    public static final String SAVE_WARS_FILE_PATH = "plugins/BookTownControl/wars.dat";
    public static Map<UUID, TownAddition> townAddition = new HashMap<>();
    public static Map<UUID, AFBSerialized> PlayerFillingBook = new HashMap<>();
    public static List<Army> Armys = new ArrayList<>();
    public static List<War> Wars = new ArrayList<>();
    @Override
    public void onEnable() {
        loadPFB();
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
        Objects.requireNonNull(getCommand("skipthis")).setExecutor(new cmds());

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                TryToEndAllWars();
            }
        }, 0, 10 * 60 * 20); // 10 минут (10 * 60 секунд) * 20 тиков 10*60*20

        Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(BookTownControl.class), new Runnable() {
            @Override
            public void run() {
                UpdateChuncksInDynMap();
            }
        }, 0L, 20*60);
    }

    private void UpdateChuncksInDynMap() {
        DynmapAPI dynmapAPI = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapAPI == null) {
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }

        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();
        Map<WorldCoord, Integer> chunks = new HashMap<>();

        for (Town town : TownyUniverse.getInstance().getTowns()) {
            Collection<TownBlock> tbs = town.getTownBlocks();
            TownAddition ta = townAddition.get(town.getUUID());
            for (TownBlock tb : tbs) {
                if (tb.isHomeBlock()) {
                    chunks.put(tb.getWorldCoord(), 5); // Домашний чанк с приоритетом 5
                } else if (ta.getChunckPriorityMap().containsKey(new ChunkCoord(tb.getX(), tb.getZ(), tb.getWorldCoord().getWorldName()))) {
                    chunks.put(tb.getWorldCoord(), 2); // Защищенный чанк с приоритетом 2
                } else {
                    chunks.put(tb.getWorldCoord(), 1); // Обычный чанк с приоритетом 1
                }
            }
        }
        removeAllMarkers();
        for (Map.Entry<WorldCoord, Integer> entry : chunks.entrySet()) {
            WorldCoord coord = entry.getKey();
            int priority = entry.getValue();

            // Получаем координаты чанка
            int chunkX = coord.getX() * 16; // Предполагая, что размер чанка равен 16 блокам
            int chunkZ = coord.getZ() * 16; // Предполагая, что размер чанка равен 16 блокам

            // Создаем маркерный набор (MarkerSet)
            MarkerSet markerSet = dynmapAPI.getMarkerAPI().createMarkerSet(
                    "Чанк_" + coord.getWorldName() + "_" + coord.getX() + "_" + coord.getZ(),
                    "Чанки государств",
                    dynmapAPI.getMarkerAPI().getMarkerIcons(),
                    false
            );

            // Создаем областной маркер (AreaMarker) для всего чанка
            String markerId = coord.getWorldName() + "_" + "Chunk_" + coord.getX() + "_" + coord.getZ();
            AreaMarker areaMarker = markerSet.createAreaMarker(
                    markerId,
                    "Чанк " + coord.getX() + ", " + coord.getZ(),
                    false,
                    coord.getWorldName(),
                    new double[]{chunkX, chunkX + 16}, // Границы чанка по оси X
                    new double[]{chunkZ, chunkZ + 16}, // Границы чанка по оси Z
                    false
            );
            // Получаем информацию о территории (строку "Страна")
            Town towninfo = TownyUniverse.getInstance().getTownBlockOrNull(coord).getTownOrNull();
            String countryInfo = (towninfo != null) ? "Страна: " + towninfo : "Страна: Неизвестная ошибка";

            // Строка с координатами чанка
            String chunkCoordinates = "Координаты чанка: (" + chunkX/16 + ", " + chunkZ/16 + ")";

            // Строка с обычными координатами мира
            String worldCoordinates = "Координаты: (" + (chunkX) + ", " + (chunkZ+16) + " x " + (chunkX+16) + ", " + (chunkZ) + ")";

            // Устанавливаем описание для чанка с требуемой информацией
            areaMarker.setDescription(countryInfo + "<br></br>" + chunkCoordinates + "<br></br>" + worldCoordinates);
            if(priority == 5) {
                areaMarker.setFillStyle(0.65,0x57bd4a); // Формат цвета: 0xRRGGBB
            }
            else if(priority == 2) {
                areaMarker.setFillStyle(0.5,0xc7782a); // Формат цвета: 0xRRGGBB
            }
            else if(priority == 1) {
                areaMarker.setFillStyle(0.6,0xFF0000); // Формат цвета: 0xRRGGBB
            }
            else {
                System.out.println("Ошибка");
            }

        }
    }

    private void removeAllMarkers() {
        DynmapAPI dynmapAPI = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapAPI == null) {
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return; // Выходим из метода, если Dynmap не найден
        }

        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        // Получаем все маркерные наборы
        Set<MarkerSet> markerSets = markerAPI.getMarkerSets();

        // Удаляем все маркеры из каждого набора
        for (MarkerSet markerSet : markerSets) {
            markerSet.deleteMarkerSet();
        }
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

    public static void savePFB() {
        try {
            File folder = new File("plugins/BookTownControl");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            FileOutputStream fileOut = new FileOutputStream("plugins/BookTownControl/PFB.dat");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(PlayerFillingBook);
            objectOut.close();
            fileOut.close();

            System.out.println("PFB.dat registered");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadPFB() {
        try {
            FileInputStream fileIn = new FileInputStream("plugins/BookTownControl/PFB.dat");
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            PlayerFillingBook = (Map<UUID, AFBSerialized>) objectIn.readObject();
            objectIn.close();
            fileIn.close();
            System.out.println("File PFB.dat loaded");
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
    public static int GetAllBasedChunks(Town town) {
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
    public static int GetAllDefaultChunks(Town town) {
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
        for (War war : BookTownControl.Wars) {
            if(war.side1Warriors.contains(player) || war.side2Warriors.contains(player)) {
                return war.ReturnTownByPlayer(player);
            }
        }
        return null;
    }

}

