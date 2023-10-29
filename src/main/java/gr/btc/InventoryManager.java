package gr.btc;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InventoryManager implements Listener {

    private Plugin plugin;

    public InventoryManager(Plugin plugin) {
        this.plugin = plugin;
        //Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openInventoryMAIN(Player player) {
        MyHolder holder = new MyHolder();
        Inventory inventory = Bukkit.createInventory(holder, 9*5, "Главное меню");

        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta crossbowMeta = crossbow.getItemMeta();
        crossbowMeta.setDisplayName(ChatColor.YELLOW+"Армии");
        crossbowMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        crossbowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        crossbow.setItemMeta(crossbowMeta);
        inventory.setItem(13, crossbow);

        // Добавьте NetheriteSword слева от CROSSBOW
        ItemStack netheriteSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta netheriteSwordMeta = netheriteSword.getItemMeta();
        netheriteSwordMeta.setDisplayName(ChatColor.RED+"Действующие войны");
        netheriteSwordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        netheriteSword.setItemMeta(netheriteSwordMeta);
        inventory.setItem(11, netheriteSword);

        netheriteSword = new ItemStack(Material.DIAMOND_SWORD);
        netheriteSwordMeta = netheriteSword.getItemMeta();
        netheriteSwordMeta.setDisplayName(ChatColor.RED+"-");
        netheriteSwordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        netheriteSword.setItemMeta(netheriteSwordMeta);
        inventory.setItem(15, netheriteSword);

        // Добавьте сундук внизу по центру
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta chestMeta = chest.getItemMeta();
        chestMeta.setDisplayName(ChatColor.GOLD+"Моя страна");
        chest.setItemMeta(chestMeta);
        inventory.setItem(30, chest);

        // Добавьте эндерсундук внизу справа
        ItemStack enderChest = new ItemStack(Material.ENDER_CHEST);
        ItemMeta enderChestMeta = enderChest.getItemMeta();
        enderChestMeta.setDisplayName(ChatColor.YELLOW+"Другие страны");
        enderChest.setItemMeta(enderChestMeta);
        inventory.setItem(32, enderChest);

        holder.setInventory(inventory);
        player.openInventory(inventory);
    }
    public static void openInventoryArmyCrOrJoin(Player player) {
        Army army = BookTownControl.GetArmyPlayerIn(player);
        if (army != null) {
            openInventoryArmyControl(player);
            return;
        }

        MyHolder holder = new MyHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, "Армии");

        ItemStack chain = new ItemStack(Material.CHAIN);
        ItemMeta chainMeta = chain.getItemMeta();
        chainMeta.setDisplayName(ChatColor.WHITE+"Присоединиться к армии");
        //chainMeta.setLore(Arrays.asList("Описание предмета"));
        chain.setItemMeta(chainMeta);

        ItemStack enderEye = new ItemStack(Material.ENDER_EYE);
        ItemMeta enderEyeMeta = enderEye.getItemMeta();
        enderEyeMeta.setDisplayName(ChatColor.YELLOW+"Создать армию");
        enderEyeMeta.setLore(Arrays.asList(ChatColor.WHITE+"Потребуется 2 стака золота"));
        enderEye.setItemMeta(enderEyeMeta);

        inventory.setItem(12, chain);
        inventory.setItem(14, enderEye);

        holder.setInventory(inventory);
        player.openInventory(inventory);
    }

    public void showArmiesMenu(Player player, int page) {
        List<Army> armies = new ArrayList<>(BookTownControl.Armys);
        Iterator<Army> iterator = armies.iterator();
        while (iterator.hasNext()) {
            Army army = iterator.next();
            if (!army.isOpen()) {
                iterator.remove();
            }
        }
        if (armies.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Нет открытых армий.");
            player.closeInventory();
            return;
        }
        MyHolder holder = new MyHolder();
        int pageSize = 25;
        int totalPages = (int) Math.ceil((double) armies.size() / pageSize);

        if (page < 1) {
            player.sendMessage(ChatColor.RED + "Некорректный номер страницы.");
            return;
        } else if (page > totalPages) {
            page = totalPages;
        }

        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.DARK_BLUE + "Список армий (Страница " + page + "/" + totalPages + ")");

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, armies.size());

        for (int i = startIndex; i < endIndex; i++) {
            Army army = armies.get(i);
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + army.getName());
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        if (page > 1) {
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backButtonMeta = backButton.getItemMeta();
            backButtonMeta.setDisplayName(ChatColor.YELLOW + "◀️ Назад");
            backButton.setItemMeta(backButtonMeta);
            inventory.setItem(35, backButton);
        }

        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextButtonMeta = nextButton.getItemMeta();
            nextButtonMeta.setDisplayName(ChatColor.YELLOW + "Вперед ▶️");
            nextButton.setItemMeta(nextButtonMeta);
            inventory.setItem(27, nextButton);
        }

        ItemStack exitButton = new ItemStack(Material.BARRIER);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
        exitButton.setItemMeta(exitButtonMeta);
        inventory.setItem(31, exitButton);

        player.openInventory(inventory);
    }
    public void showArmyMembersMenu(Player player) {
        MyHolder holder = new MyHolder();
        Army army = BookTownControl.GetArmyPlayerIn(player);
        int priority = -1;
        if (army != null) {
            Inventory inventory = Bukkit.createInventory(holder, 54, "Список бойцов");

            // Получаем список игроков в армии
            List<Army.ArmyPlayer> armyMembers = army.getPlayers();

            // Добавляем предметы в инвентарь для каждого игрока
            for (int i = 0; i < armyMembers.size(); i++) {
                Army.ArmyPlayer member = armyMembers.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwningPlayer(member.getPlayer());
                skullMeta.setDisplayName(ChatColor.YELLOW + Bukkit.getOfflinePlayer(member.getUUID()).getName());

                // Создаем список для отображения дополнительной информации об игроке
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + "Ранг: " + member.getPriority());
                if(army.GetLink(member) == null) {
                    lore.add(ChatColor.GREEN + "Ничего не охраняет");
                }
                else {
                    if(TownyUniverse.getInstance().getTown(army.GetLink(member)) == null) {
                        army.SetLink(member.getPlayer(),null);
                        lore.add(ChatColor.GREEN + "Ничего не охраняет");
                    }
                    else {
                        lore.add(ChatColor.GOLD + "Охраняет: " + TownyUniverse.getInstance().getTown(army.GetLink(member)).getName());
                    }
                }

                // Добавьте другую информацию, которую вы хотите отобразить

                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
                inventory.setItem(i, skull);
            }

            ItemStack nextButton = new ItemStack(Material.BARRIER);
            ItemMeta nextButtonMeta = nextButton.getItemMeta();
            nextButtonMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
            nextButton.setItemMeta(nextButtonMeta);
            inventory.setItem(49, nextButton);
            player.openInventory(inventory);
        } else {
            player.sendMessage("Вы не состоите в армии");
        }
    }

    public void openPlayerControlMenu(Player player, Army.ArmyPlayer selectedPlayer) {
        MyHolder holder = new MyHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, "Управление бойцом "+selectedPlayer.getPlayer().getName());

        // Добавляем предметы в инвентарь для каждого действия
        ItemStack assignToCityItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta assignToCityMeta = assignToCityItem.getItemMeta();
        assignToCityMeta.setDisplayName(ChatColor.YELLOW + "Поставить на службу в город");
        assignToCityMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        assignToCityItem.setItemMeta(assignToCityMeta);
        inventory.setItem(10, assignToCityItem);

        ItemStack removePlayerItem = new ItemStack(Material.STRUCTURE_VOID);
        ItemMeta removePlayerMeta = removePlayerItem.getItemMeta();
        removePlayerMeta.setDisplayName(ChatColor.YELLOW + "Удалить игрока");
        removePlayerItem.setItemMeta(removePlayerMeta);
        inventory.setItem(12, removePlayerItem);

        ItemStack changePriorityItem = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        ItemMeta changePriorityMeta = changePriorityItem.getItemMeta();
        changePriorityMeta.setDisplayName(ChatColor.YELLOW + "Изменить приоритет");
        changePriorityMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        changePriorityItem.setItemMeta(changePriorityMeta);
        inventory.setItem(14, changePriorityItem);

        changePriorityItem = new ItemStack(Material.BARRIER);
        changePriorityMeta = changePriorityItem.getItemMeta();
        changePriorityMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
        changePriorityItem.setItemMeta(changePriorityMeta);
        inventory.setItem(16, changePriorityItem);
        // Открываем инвентарь игроку
        player.openInventory(inventory);
    }
    public static void openInventoryArmyControl(Player player) {
        MyHolder holder = new MyHolder();
        Army army = BookTownControl.GetArmyPlayerIn(player);
        Inventory inventory = Bukkit.createInventory(holder, 27, "Моя армия "+army.getName());
        int priority = -1;
        if (army != null) {
            Army.ArmyPlayer armyPlayer = army.getPlayer(player);
            if(armyPlayer != null) {
                priority = armyPlayer.getPriority();
            }
        }
        else {
            throw new RuntimeException("Игрок не состоит в армии");
        }

        if(priority >= 0) {
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemMeta ironSwordMeta = ironSword.getItemMeta();
            ironSwordMeta.setDisplayName(ChatColor.RED+"Военные конфликты");
            ironSwordMeta.setLore(Arrays.asList(ChatColor.RED+"Ваша армия участвует в "+BookTownControl.CheckForWarsInArmy(army).size()+" военных конфликтах"));
            ironSwordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            ironSword.setItemMeta(ironSwordMeta);
            inventory.setItem(4, ironSword);

            ItemStack barrier = new ItemStack(Material.STRUCTURE_VOID);
            ItemMeta barrierMeta = barrier.getItemMeta();
            barrierMeta.setDisplayName(ChatColor.RED+"Покинуть армию");
            //barrierMeta.setLore(Arrays.asList("Описание предмета"));
            barrier.setItemMeta(barrierMeta);
            inventory.setItem(14, barrier);
        }

        if(priority >= 1) {
            ItemStack woodenSword = new ItemStack(Material.WOODEN_SWORD);
            ItemMeta woodenSwordMeta = woodenSword.getItemMeta();
            woodenSwordMeta.setDisplayName(ChatColor.YELLOW+"Список бойцов");
            woodenSwordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            //woodenSwordMeta.setLore(Arrays.asList("Описание предмета"));
            woodenSword.setItemMeta(woodenSwordMeta);
            inventory.setItem(13, woodenSword);
        }

        if(priority >= 2) {
            // Добавьте предметы для priority >= 2
        }

        if(priority >= 3) {
            ItemStack birchDoor = new ItemStack(Material.BIRCH_DOOR);
            ItemMeta birchDoorMeta = birchDoor.getItemMeta();
            birchDoorMeta.setDisplayName(ChatColor.YELLOW+"Пригласить в армию");
            //birchDoorMeta.setLore(Arrays.asList("Описание предмета"));
            birchDoor.setItemMeta(birchDoorMeta);
            inventory.setItem(11, birchDoor);
            if(army.isOpen()) {
                ItemStack redstoneTorch = new ItemStack(Material.REDSTONE_TORCH);
                ItemMeta redstoneTorchMeta = redstoneTorch.getItemMeta();
                redstoneTorchMeta.setDisplayName(ChatColor.GREEN+"Свободный вход включен");
                redstoneTorchMeta.setLore(Arrays.asList(ChatColor.YELLOW+"Нажмите для переключения"));
                redstoneTorch.setItemMeta(redstoneTorchMeta);
                inventory.setItem(20, redstoneTorch);
            } else {
                // Если армия закрыта, добавляем Lever
                ItemStack lever = new ItemStack(Material.LEVER);
                ItemMeta leverMeta = lever.getItemMeta();
                leverMeta.setDisplayName(ChatColor.RED+"Свободный вход выключен");
                leverMeta.setLore(Arrays.asList(ChatColor.YELLOW+"Нажмите для переключения"));
                lever.setItemMeta(leverMeta);
                inventory.setItem(20, lever);
            }
        }

        if(priority >= 4) {
            ItemStack ponder = new ItemStack(Material.GOAT_HORN);
            ItemMeta ponderMeta = ponder.getItemMeta();
            ponderMeta.setDisplayName(ChatColor.YELLOW+"Присоединиться к стране");
            //ponderMeta.setLore(Arrays.asList("Описание предмета"));
            ponderMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            ponder.setItemMeta(ponderMeta);
            inventory.setItem(12, ponder);
            ponder = new ItemStack(Material.TURTLE_HELMET);
            ponderMeta = ponder.getItemMeta();
            ponderMeta.setDisplayName(ChatColor.RED+"Отозвать армию из страны");
            ponderMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            //ponderMeta.setLore(Arrays.asList("Описание предмета"));
            ponder.setItemMeta(ponderMeta);
            inventory.setItem(21, ponder);

        }

        if(priority >= 5) {
            ItemStack pufferfish = new ItemStack(Material.BARRIER);
            ItemMeta pufferfishMeta = pufferfish.getItemMeta();
            pufferfishMeta.setDisplayName(ChatColor.RED+"Распустить армию");
            //pufferfishMeta.setLore(Arrays.asList("Описание предмета"));
            pufferfish.setItemMeta(pufferfishMeta);
            inventory.setItem(15, pufferfish);
        }

        ItemStack noPermissionItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta noPermissionMeta = noPermissionItem.getItemMeta();
        noPermissionMeta.setDisplayName(ChatColor.GRAY+"Недостаточно прав");
        noPermissionItem.setItemMeta(noPermissionMeta);

        for (int i = 11; i <= 15; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, noPermissionItem);
            }
        }

        holder.setInventory(inventory);
        player.openInventory(inventory);
    }

    public static void showGlobalCountriesMenu(Player player, int page) {
        List<Town> countries = new ArrayList<>(TownyUniverse.getInstance().getTowns());
        Army army = BookTownControl.GetArmyPlayerIn(player);
        Iterator<Town> iterator = countries.iterator();

        while (iterator.hasNext()) {
            Town town = iterator.next();
            if (army.IsCountryConnected(town)) {
                iterator.remove();
            }
        }

        if (countries.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Нет доступных стран для присоединения");
            player.closeInventory();
            return;
        }
        MyHolder holder = new MyHolder();
        int pageSize = 25; // Количество стран, отображаемых на одной странице
        int totalPages = (int) Math.ceil((double) countries.size() / pageSize); // Общее количество страниц

        // Проверка валидности номера страницы
        if (page < 1) {
            player.sendMessage(ChatColor.RED + "Некорректный номер страницы.");
            return;
        } else if (page > totalPages) {
            page = totalPages;
        }

        // Создание инвентаря для меню стран
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.DARK_BLUE + "Список стран (Страница " + (page) + "/" + totalPages + ")");

        // Вычисление индексов стран, отображаемых на текущей странице
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, countries.size());

        // Добавление стран на текущей странице в инвентарь
        for (int i = startIndex; i < endIndex; i++) {
            Town country = countries.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + country.getName());

            // Формирование описания страны
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Население: " + country.getResidents().size());
            lore.add(ChatColor.YELLOW + "Глава: " + country.getMayor());

            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        // Добавление кнопки "Назад", если есть предыдущая страница
        if (page > 1) {
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backButtonMeta = backButton.getItemMeta();
            backButtonMeta.setDisplayName(ChatColor.YELLOW + "◀️ Назад");
            backButton.setItemMeta(backButtonMeta);
            inventory.setItem(35, backButton);
        }

        // Добавление кнопки "Вперед", если есть следующая страница
        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextButtonMeta = nextButton.getItemMeta();
            nextButtonMeta.setDisplayName(ChatColor.YELLOW + "Вперед ▶️");
            nextButton.setItemMeta(nextButtonMeta);
            inventory.setItem(27, nextButton);
        }

        ItemStack exitButton = new ItemStack(Material.BARRIER);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
        exitButton.setItemMeta(exitButtonMeta);
        inventory.setItem(31, exitButton);

        // Открытие инвентаря для игрока
        player.openInventory(inventory);
    }

    public static void showLocalCountriesMenu(Player player, int page) {
        List<Town> countries = new ArrayList<>(TownyUniverse.getInstance().getTowns());
        Army army = BookTownControl.GetArmyPlayerIn(player);
        Iterator<Town> iterator = countries.iterator();

        while (iterator.hasNext()) {
            Town town = iterator.next();
            if (!(army.IsCountryConnected(town))) {
                iterator.remove();
            }
        }

        if (countries.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Вы не сопряжены ни с одной страной");
            player.closeInventory();
            return;
        }
        MyHolder holder = new MyHolder();
        int pageSize = 25; // Количество стран, отображаемых на одной странице
        int totalPages = (int) Math.ceil((double) countries.size() / pageSize); // Общее количество страниц

        // Проверка валидности номера страницы
        if (page < 1) {
            player.sendMessage(ChatColor.RED + "Некорректный номер страницы.");
            return;
        } else if (page > totalPages) {
            page = totalPages;
        }

        // Создание инвентаря для меню стран
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.DARK_BLUE + "Сопряжённые страны (Страница " + (page) + "/" + totalPages + ")");

        // Вычисление индексов стран, отображаемых на текущей странице
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, countries.size());

        // Добавление стран на текущей странице в инвентарь
        for (int i = startIndex; i < endIndex; i++) {
            Town country = countries.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + country.getName());

            // Формирование описания страны
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Население: " + country.getResidents().size());
            lore.add(ChatColor.YELLOW + "Глава: " + country.getMayor());

            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        // Добавление кнопки "Назад", если есть предыдущая страница
        if (page > 1) {
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backButtonMeta = backButton.getItemMeta();
            backButtonMeta.setDisplayName(ChatColor.YELLOW + "◀️ Назад");
            backButton.setItemMeta(backButtonMeta);
            inventory.setItem(35, backButton);
        }

        // Добавление кнопки "Вперед", если есть следующая страница
        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextButtonMeta = nextButton.getItemMeta();
            nextButtonMeta.setDisplayName(ChatColor.YELLOW + "Вперед ▶️");
            nextButton.setItemMeta(nextButtonMeta);
            inventory.setItem(27, nextButton);
        }

        ItemStack exitButton = new ItemStack(Material.BARRIER);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
        exitButton.setItemMeta(exitButtonMeta);
        inventory.setItem(31, exitButton);

        // Открытие инвентаря для игрока
        player.openInventory(inventory);
    }
    public void assignPlayerToCity(Player player, OfflinePlayer MainPlayer) {
        Army army = BookTownControl.GetArmyPlayerIn(player);
        MyHolder holder = new MyHolder();
        // Получаем список привязанных стран к армии
        List<UUID> connectedCountrys = army.GetConnectedCountrys();

        if (connectedCountrys.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Нет привязанных стран к армии.");
            player.closeInventory();
            return;
        }

        Inventory countryMenu = Bukkit.createInventory(holder, 27, "Пост игрока "+MainPlayer.getName());

        for (UUID countryId : connectedCountrys) {
            Town country = TownyUniverse.getInstance().getTown(countryId);
            if (country != null) {
                String countryName = country.getName();
                String mayor = country.getMayor().getPlayer().getDisplayName();
                String heritage = String.valueOf(country.getResidents().size());
                List<String> armyPlayers = new ArrayList<>();

                // Получаем список игроков из армии, присоединенных к городу
                for (Army.ArmyPlayer armyPlayer : army.getPlayers()) {
                    if (army.GetLink(armyPlayer) != null && army.GetLink(armyPlayer).equals(countryId)) {
                        armyPlayers.add(armyPlayer.getPlayer().getName());
                    }
                }

                List<String> countryDescription = new ArrayList<>();
                countryDescription.add(ChatColor.YELLOW + "Мэр: " + mayor);
                countryDescription.add(ChatColor.GOLD + "Жителей: " + heritage);
                countryDescription.add(ChatColor.DARK_GREEN + "Игроки из армии: ");
                for (String playerr : armyPlayers) {
                    countryDescription.add(ChatColor.DARK_GREEN + playerr);
                }
                ItemStack countryItem = new ItemStack(Material.PAPER);
                ItemMeta countryItemMeta = countryItem.getItemMeta();
                countryItemMeta.setDisplayName(ChatColor.YELLOW + countryName);
                countryItemMeta.setLore(countryDescription);
                countryItem.setItemMeta(countryItemMeta);
                countryMenu.addItem(countryItem);
            }
        }
        ItemStack countryItem = new ItemStack(Material.ARROW);
        ItemMeta countryItemMeta = countryItem.getItemMeta();
        countryItemMeta.setDisplayName(ChatColor.YELLOW + "Назад");
        countryItem.setItemMeta(countryItemMeta);

        countryMenu.setItem(26,countryItem);

        countryItem = new ItemStack(Material.OXEYE_DAISY);
        countryItemMeta = countryItem.getItemMeta();
        countryItemMeta.setDisplayName(ChatColor.YELLOW + "Освободить от службы");
        countryItem.setItemMeta(countryItemMeta);

        countryMenu.setItem(25,countryItem);

        player.openInventory(countryMenu);
    }
    public static void CountriesStatisticAndJoinMenu(Player player, int page) {
        List<Town> countries = new ArrayList<>(TownyUniverse.getInstance().getTowns());

        MyHolder holder = new MyHolder();
        int pageSize = 25; // Количество стран, отображаемых на одной странице
        int totalPages = (int) Math.ceil((double) countries.size() / pageSize); // Общее количество страниц

        // Проверка валидности номера страницы
        if(totalPages == 0) {
            player.sendMessage(ChatColor.RED + "Пока что в мире не создано ни одной страны.");
            return;
        }
        if (page < 1) {
            player.sendMessage(ChatColor.RED + "Некорректный номер страницы.");
            return;
        }
        else if (page > totalPages) {
            page = totalPages;
        }

        // Создание инвентаря для меню стран
        Inventory inventory = Bukkit.createInventory(holder, 36, "Страны (Страница " + (page) + "/" + totalPages + ")");

        // Вычисление индексов стран, отображаемых на текущей странице
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, countries.size());

        // Добавление стран на текущей странице в инвентарь
        for (int i = startIndex; i < endIndex; i++) {
            Town country = countries.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + country.getName());

            // Формирование описания страны
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Население: " + country.getResidents().size());
            lore.add(ChatColor.YELLOW + "Глава: " + country.getMayor());
            Town town = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTownOrNull();
            if(country.isOpen()) {
                lore.add(ChatColor.GREEN + "Свободный вход");
                if(town != null) {
                    lore.add(ChatColor.GREEN + "\nНажмите чтобы присоединиться");
                }
            }
            else {
                lore.add(ChatColor.GREEN + "Вход по подтверждению");
                if(town != null) {
                    lore.add(ChatColor.RED + "\nНажмите чтобы отправить заявку на вступление");
                }
            }



            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        // Добавление кнопки "Назад", если есть предыдущая страница
        if (page > 1) {
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backButtonMeta = backButton.getItemMeta();
            backButtonMeta.setDisplayName(ChatColor.YELLOW + "◀️ Назад");
            backButton.setItemMeta(backButtonMeta);
            inventory.setItem(35, backButton);
        }

        // Добавление кнопки "Вперед", если есть следующая страница
        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextButtonMeta = nextButton.getItemMeta();
            nextButtonMeta.setDisplayName(ChatColor.YELLOW + "Вперед ▶️");
            nextButton.setItemMeta(nextButtonMeta);
            inventory.setItem(27, nextButton);
        }

        ItemStack exitButton = new ItemStack(Material.BARRIER);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
        exitButton.setItemMeta(exitButtonMeta);
        inventory.setItem(31, exitButton);

        // Открытие инвентаря для игрока
        player.openInventory(inventory);
    }

    public static void openInventoryCountryControl(Player player) {
        MyHolder holder = new MyHolder();
        Town town = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTownOrNull();
        if(town == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED+"Вы не состоите в стране, вы можете выбрать страну в меню во вкладке "+ChatColor.YELLOW+"\"Другие страны\"");
            return;
        }

        Inventory inventory = Bukkit.createInventory(holder, 27, "Моя страна "+town.getName());
        int priority = 0;
        if (BookTownControl.townAddition.get(town.getUUID()).retso() != null && BookTownControl.townAddition.get(town.getUUID()).retso().equals(player.getUniqueId())) {
            priority = 1;
        }
        else if(town.getMayor().getPlayer().equals(player)) {
            priority = 2;
        }

        if(priority >= 0) {
            ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
            ItemMeta ironSwordMeta = ironSword.getItemMeta();
            ironSwordMeta.setDisplayName(ChatColor.RED+"Военные конфликты");
            ironSwordMeta.setLore(Arrays.asList(ChatColor.RED+"Ваша страна "+(BookTownControl.CheckForWarInTown(town) != null ? "" : "не ")+"участвует в военном конфликте"));
            ironSwordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            ironSword.setItemMeta(ironSwordMeta);
            inventory.setItem(4, ironSword);

            ItemStack barrier = new ItemStack(Material.STRUCTURE_VOID);
            ItemMeta barrierMeta = barrier.getItemMeta();
            barrierMeta.setDisplayName(ChatColor.RED+"Покинуть страну");
            //barrierMeta.setLore(Arrays.asList("Описание предмета"));
            barrier.setItemMeta(barrierMeta);
            inventory.setItem(14, barrier);
        }

        if(priority >= 1) {
            ItemStack woodenSword = new ItemStack(Material.WOODEN_SWORD);
            ItemMeta woodenSwordMeta = woodenSword.getItemMeta();
            woodenSwordMeta.setDisplayName(ChatColor.YELLOW+"Список участников");
            //woodenSwordMeta.setLore(Arrays.asList("Описание предмета"));
            woodenSwordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            woodenSword.setItemMeta(woodenSwordMeta);
            inventory.setItem(13, woodenSword);
            ItemStack birchDoor = new ItemStack(Material.BIRCH_DOOR);
            ItemMeta birchDoorMeta = birchDoor.getItemMeta();
            birchDoorMeta.setDisplayName(ChatColor.YELLOW+"Пригласить в страну");
            //birchDoorMeta.setLore(Arrays.asList("Описание предмета"));
            birchDoor.setItemMeta(birchDoorMeta);
            inventory.setItem(11, birchDoor);
            birchDoor = new ItemStack(Material.KNOWLEDGE_BOOK);
            birchDoorMeta = birchDoor.getItemMeta();
            birchDoorMeta.setDisplayName(ChatColor.YELLOW+"Рассмотреть контракты");
            //birchDoorMeta.setLore(Arrays.asList("Описание предмета"));
            birchDoor.setItemMeta(birchDoorMeta);
            inventory.setItem(12, birchDoor);
            if(town.isOpen()) {
                ItemStack redstoneTorch = new ItemStack(Material.REDSTONE_TORCH);
                ItemMeta redstoneTorchMeta = redstoneTorch.getItemMeta();
                redstoneTorchMeta.setDisplayName(ChatColor.GREEN+"Свободный вход включен");
                redstoneTorchMeta.setLore(Arrays.asList(ChatColor.YELLOW+"Нажмите для переключения"));
                redstoneTorch.setItemMeta(redstoneTorchMeta);
                inventory.setItem(20, redstoneTorch);
            } else {
                // Если армия закрыта, добавляем Lever
                ItemStack lever = new ItemStack(Material.LEVER);
                ItemMeta leverMeta = lever.getItemMeta();
                leverMeta.setDisplayName(ChatColor.RED+"Свободный вход выключен");
                leverMeta.setLore(Arrays.asList(ChatColor.YELLOW+"Нажмите для переключения"));
                lever.setItemMeta(leverMeta);
                inventory.setItem(20, lever);
            }
        }

        if(priority >= 2) {
            ItemStack pufferfish = new ItemStack(Material.BARRIER);
            ItemMeta pufferfishMeta = pufferfish.getItemMeta();
            pufferfishMeta.setDisplayName(ChatColor.RED+"Уничтожить страну");
            //pufferfishMeta.setLore(Arrays.asList("Описание предмета"));
            pufferfish.setItemMeta(pufferfishMeta);
            inventory.setItem(15, pufferfish);
            // Добавьте предметы для priority >= 2
            if(!BookTownControl.CheckForWar(town)) {
                pufferfish = new ItemStack(Material.NETHERITE_SWORD);
                pufferfishMeta = pufferfish.getItemMeta();
                pufferfishMeta.setDisplayName(ChatColor.RED+"Начать войну");
                pufferfishMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                pufferfish.setItemMeta(pufferfishMeta);
                inventory.setItem(21, pufferfish);
            }

            // Добавьте предметы для priority >= 2
        }

        ItemStack noPermissionItem = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta noPermissionMeta = noPermissionItem.getItemMeta();
        noPermissionMeta.setDisplayName("Недостаточно прав");
        noPermissionItem.setItemMeta(noPermissionMeta);

        for (int i = 11; i <= 15; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, noPermissionItem);
            }
        }

        holder.setInventory(inventory);
        player.openInventory(inventory);
    }

    public static void showGlobalWarsMenu(Player player, int page) {
        List<War> wars = new ArrayList<>(BookTownControl.Wars);
        Army army = BookTownControl.GetArmyPlayerIn(player);

        if (wars.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Пока что в мире нет конфликтов");
            player.closeInventory();
            return;
        }
        MyHolder holder = new MyHolder();
        int pageSize = 25; // Количество стран, отображаемых на одной странице
        int totalPages = (int) Math.ceil((double) wars.size() / pageSize); // Общее количество страниц

        // Проверка валидности номера страницы
        if (page < 1) {
            player.sendMessage(ChatColor.RED + "Некорректный номер страницы.");
            return;
        } else if (page > totalPages) {
            page = totalPages;
        }

        // Создание инвентаря для меню стран
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.DARK_BLUE + "Список войн (Страница " + (page) + "/" + totalPages + ")");

        // Вычисление индексов стран, отображаемых на текущей странице
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, wars.size());

        // Добавление стран на текущей странице в инвентарь
        for (int i = startIndex; i < endIndex; i++) {
            War war = wars.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            // Заголовок элемента списка
            String displayName = ChatColor.GOLD + "Война между " + ChatColor.YELLOW + TownyUniverse.getInstance().getTown(war.sides1.get(0)).getName() + ChatColor.GOLD + " и " + ChatColor.YELLOW + TownyUniverse.getInstance().getTown(war.sides2.get(0)).getName();
            meta.setDisplayName(displayName);

            // Формирование описания войны
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Статус: " + (war.isStarted ? ChatColor.RED + "Война начата" : ChatColor.GREEN + "Война не начата"));
            List<String> SIDes1 = new ArrayList<>();
            List<String> SIDes2 = new ArrayList<>();
            List<String> Warriors1 = new ArrayList<>();
            List<String> Warriors2 = new ArrayList<>();
            for(UUID u : war.sides1) {
                SIDes1.add(TownyUniverse.getInstance().getTown(u).getName());
            }
            for(UUID u : war.sides2) {
                SIDes2.add(TownyUniverse.getInstance().getTown(u).getName());
            }
            for(OfflinePlayer p : war.side1Warriors) {
                Warriors1.add(p.getName());
            }
            for(OfflinePlayer p : war.side2Warriors) {
                Warriors1.add(p.getName());
            }
            lore.add(ChatColor.YELLOW + "Участники стороны 1: " + ChatColor.WHITE + String.join(", ", SIDes1));
            lore.add(ChatColor.YELLOW + "Участники стороны 2: " + ChatColor.WHITE + String.join(", ", SIDes2));
            lore.add(ChatColor.YELLOW + "Воины стороны 1: " + ChatColor.WHITE + String.join(", ", Warriors1));
            lore.add(ChatColor.YELLOW + "Воины стороны 2: " + ChatColor.WHITE + String.join(", ", Warriors2));

            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        // Добавление кнопки "Назад", если есть предыдущая страница
        if (page > 1) {
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backButtonMeta = backButton.getItemMeta();
            backButtonMeta.setDisplayName(ChatColor.YELLOW + "◀️ Назад");
            backButton.setItemMeta(backButtonMeta);
            inventory.setItem(35, backButton);
        }

        // Добавление кнопки "Вперед", если есть следующая страница
        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextButtonMeta = nextButton.getItemMeta();
            nextButtonMeta.setDisplayName(ChatColor.YELLOW + "Вперед ▶️");
            nextButton.setItemMeta(nextButtonMeta);
            inventory.setItem(27, nextButton);
        }

        ItemStack exitButton = new ItemStack(Material.BARRIER);
        ItemMeta exitButtonMeta = exitButton.getItemMeta();
        exitButtonMeta.setDisplayName(ChatColor.YELLOW + "Выйти");
        exitButton.setItemMeta(exitButtonMeta);
        inventory.setItem(31, exitButton);

        // Открытие инвентаря для игрока
        player.openInventory(inventory);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();
        if(clickedInventory == null) return;
        InventoryHolder holder = event.getClickedInventory().getHolder();
        if (event.getClickedInventory().getType() != InventoryType.CHEST || clickedItem == null|| clickedItem.getType() == Material.AIR) {
            // Clicked inventory is null, handle the situation accordingly
            if (holder instanceof MyHolder) {
                event.setCancelled(true);
            }
            return;
        }

        if (holder instanceof MyHolder) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            String displayName = clickedItem.getItemMeta().getDisplayName();

                String inventoryTitle = event.getView().getTitle();
                if (inventoryTitle.equalsIgnoreCase("Главное меню")) {
                    if (displayName.contains("Армии")) {
                        openInventoryArmyCrOrJoin(player);
                        return;
                    }
                    else if(displayName.contains("Действующие войны")) {
                        showGlobalWarsMenu(player,1);
                        return;
                    }
                    else if(displayName.contains("Моя страна")) {
                        openInventoryCountryControl(player);
                        return;
                    }
                    else if(displayName.contains("Другие страны")) {
                        CountriesStatisticAndJoinMenu(player,1);
                        return;
                    }
                }
                else if(inventoryTitle.equalsIgnoreCase("Армии")) {
                    if (displayName.contains("Присоединиться к армии")) {
                        showArmiesMenu(player,1);
                    }
                    else if (displayName.contains("Создать армию")) {
                        player.closeInventory();
                        ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
                        int goldIngotCount = 0;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getType() == Material.GOLD_INGOT) {
                                goldIngotCount += item.getAmount();
                            }
                        } // Подсчитываем количество золотых слитков в инвентаре игрока

                        if (goldIngotCount >= 128) {
                            CompletableFuture<Void> waitForResponse = new CompletableFuture<>();
                            ChatListener chatListener = new ChatListener(player, waitForResponse);

                            // Отправляем сообщение и ожидаем ответа
                            player.sendMessage("Для отмены введите 'cancel'");
                            player.sendMessage("Введите название армии в чате:");

                            // Запускаем асинхронную задачу для ожидания ответа
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                Bukkit.getPluginManager().registerEvents(chatListener, plugin);
                            });

                            // Ожидаем ответа от игрока с ограничением в 60 секунд
                            waitForResponse.orTimeout(60, TimeUnit.SECONDS).thenAccept(response -> {
                                // После получения ответа выполняем действия
                                String armyName = chatListener.getResponse();
                                if (armyName == null || armyName.contains(" ")) {
                                    // Отмена
                                    if(armyName.contains(" ")) {
                                        player.sendMessage(ChatColor.RED + "Создание отменено, название не должно содержать пробелов.");
                                    }
                                    else {
                                        player.sendMessage(ChatColor.RED + "Создание отменено");
                                    }
                                    chatListener.unregisterChatListener(chatListener);

                                } else {
                                    int goldIngotCount2 = 0;
                                    for (ItemStack item : player.getInventory().getContents()) {
                                        if (item != null && item.getType() == Material.GOLD_INGOT) {
                                            goldIngotCount2 += item.getAmount();
                                        }
                                    }

                                    if (goldIngotCount2 >= 128) {
                                        ItemStack goldIngotStack = new ItemStack(Material.GOLD_INGOT, 64);
                                        ItemStack offhandItem = player.getInventory().getItemInOffHand();
                                        if (offhandItem.getType() == Material.GOLD_INGOT) {
                                            player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 64-offhandItem.getAmount()));
                                            offhandItem.setAmount(0);
                                        } else {
                                            player.getInventory().removeItem(goldIngotStack);
                                        }

                                        // Теперь забрали золото из второй руки (если было), забираем оставшееся золото
                                        player.getInventory().removeItem(goldIngotStack);
                                        Army newArmy = new Army(armyName);
                                        newArmy.addPlayer(player.getUniqueId(), 5);
                                        BookTownControl.Armys.add(newArmy);
                                        chatListener.unregisterChatListener(chatListener);
                                        player.sendMessage("Армия " + armyName + " создана");
                                    } else {
                                        chatListener.unregisterChatListener(chatListener);
                                        player.sendMessage(ChatColor.RED + "Для создания армии вам нужно иметь 2 стака золотых слитков.");
                                    }
                                }
                            }).exceptionally(e -> {
                                // Обработка исключения в случае превышения времени ожидания
                                chatListener.unregisterChatListener(chatListener);
                                player.sendMessage(ChatColor.RED + "Время ожидания истекло. Приглашение отменено.");
                                return null;
                            });
                        }
                        else {
                            player.sendMessage(ChatColor.RED+"Для создания армии вам нужно иметь 2 стака золотых слитков.");
                            return;
                        }
                    }
                }
                else if (inventoryTitle.contains("Моя армия")) {
                    if (displayName.contains("Пригласить в армию")) {
                        player.closeInventory();
                        player.sendMessage("Введите ник игрока, которого хотите пригласить\nCancel для отмены");

                        CompletableFuture<Void> waitForResponse = new CompletableFuture<>();
                        ChatListener chatListener = new ChatListener(player, waitForResponse);

                        // Регистрируем слушатель событий чата
                        Bukkit.getPluginManager().registerEvents(chatListener, plugin);

                        // Ожидаем ответа от игрока с ограничением в 60 секунд
                        waitForResponse.orTimeout(60, TimeUnit.SECONDS).thenAccept(response -> {
                            // После получения ответа выполняем действия
                            String invitedPlayerName = chatListener.getResponse();

                            if (invitedPlayerName == null || invitedPlayerName.equalsIgnoreCase("cancel") || invitedPlayerName.equalsIgnoreCase("c") || invitedPlayerName.equalsIgnoreCase("n") || invitedPlayerName.equalsIgnoreCase("no") || invitedPlayerName.equalsIgnoreCase("отмена") || invitedPlayerName.equalsIgnoreCase("н") || invitedPlayerName.equalsIgnoreCase("нет") || invitedPlayerName.equalsIgnoreCase("о")) {
                                // Отмена
                                player.sendMessage(ChatColor.RED + "Приглашение отменено.");
                            } else {
                                String command = "army invite " + invitedPlayerName;
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    player.performCommand(command);
                                });
                            }

                            // Разблокируем чат
                            HandlerList.unregisterAll(chatListener);
                        });
                    }
                    else if (displayName.contains("Список бойцов")) { //+
                        showArmyMembersMenu(player);
                    }
                    else if (displayName.contains("Распустить армию")) {//-
                        player.closeInventory();
                        player.performCommand("army disband false");
                    }
                    else if (displayName.contains("Присоединиться к стране")) {//-
                        player.performCommand("army connect");
                         //Connect to country
                    }
                    else if (displayName.contains("Покинуть армию")) {//-
                        player.closeInventory();
                        player.performCommand("army leave false");
                    }
                    else if(displayName.contains("Свободный вход")) {
                        Army army = BookTownControl.GetArmyPlayerIn(player);
                        if(army != null && army.getPlayer(player).getPriority() >= 4) {
                            army.setOpen(!army.isOpen());
                        }
                        openInventoryArmyControl(player);
                    }
                    else if(displayName.contains("Отозвать армию из страны")) {
                        showLocalCountriesMenu(player,1);
                    }
                    return;
                }
                else if(inventoryTitle.contains("Список армий")) {
                    if (displayName.equals(ChatColor.YELLOW + "◀️ Назад")) {
                        // Нажата кнопка "Назад"
                        int currentPage = Integer.parseInt(event.getView().getTitle().split(" ")[2].split("/")[0].replace("Страница ", ""));
                        showArmiesMenu(player, currentPage - 1);
                    }
                    else if (displayName.equals(ChatColor.YELLOW + "Вперед ▶️")) {
                        // Нажата кнопка "Вперед"
                        int currentPage = Integer.parseInt(event.getView().getTitle().split(" ")[2].split("/")[0].replace("Страница ", ""));
                        showArmiesMenu(player, currentPage + 1);
                    }
                    else if(displayName.equals(ChatColor.YELLOW + "Выйти")) {
                        openInventoryArmyCrOrJoin(player);
                    }
                    else {
                        String armyName = ChatColor.stripColor(displayName);

                        // Выполняем команду army join (название армии)
                        player.performCommand("army join " + armyName);
                    }
                    return;
                }
                else if (inventoryTitle.contains("Список бойцов")) {
                    if (displayName.equals(ChatColor.YELLOW + "Выйти")) {
                        openInventoryArmyControl(player);
                    } else {
                        Army army = BookTownControl.GetArmyPlayerIn(Bukkit.getOfflinePlayer(ChatColor.stripColor(displayName)));
                        // Получаем выбранного игрока
                        if(army != null) {
                            Army.ArmyPlayer selectedPlayer = army.getPlayer(Bukkit.getOfflinePlayer(ChatColor.stripColor(displayName)));

                            if (selectedPlayer != null && army.getPlayer(player).getPriority() >= 4) {
                                openPlayerControlMenu(player, selectedPlayer);
                            }
                        }
                        else {
                            player.closeInventory();
                        }

                    }
                    return;
                }
                else if (inventoryTitle.contains("Управление бойцом")) {
                    Army army = BookTownControl.GetArmyPlayerIn(player);

                    // Получаем выбранного игрока из названия предмета
                    String playerName = inventoryTitle.replace("Управление бойцом ", "");
                    OfflinePlayer selectedPlayer = Bukkit.getOfflinePlayer(playerName);

                    if (selectedPlayer != null) {
                        if (displayName.equals(ChatColor.YELLOW + "Поставить на службу в город")) {
                            // Действие для постановки игрока на службу в город
                            assignPlayerToCity(player,selectedPlayer);
                        }
                        else if (displayName.equals(ChatColor.YELLOW + "Удалить игрока")) {
                            if(selectedPlayer != player) {
                                army.removePlayer(selectedPlayer.getUniqueId());
                            }
                            else {
                                player.sendMessage(ChatColor.RED+"Вы не можете удалить из армии себя, используйте /army leave");
                                player.closeInventory();
                            }
                            // Действие для удаления игрока

                        }
                        else if (displayName.equals(ChatColor.YELLOW + "Изменить приоритет")) {
                            // Действие для изменения приоритета игрока
                            changePlayerPriority(selectedPlayer,player,army);
                        }
                        else if (displayName.equals(ChatColor.YELLOW + "Выйти")) {
                            showArmyMembersMenu(player);
                        }
                        else {
                            player.sendMessage("Неверное действие");
                        }
                    }
                    else if (displayName.equals(ChatColor.YELLOW + "Выйти")) {
                        showArmyMembersMenu(player);
                    }
                    else {
                        player.sendMessage("Ошибка при выборе игрока");
                    }
                    return;
                }
                else if (inventoryTitle.contains("Пост игрока")) {
                    String playerName = inventoryTitle.replace("Пост игрока ", "");
                    OfflinePlayer selectedPlayer = Bukkit.getOfflinePlayer(playerName);
                    ItemStack selectedItem = event.getCurrentItem();
                    if (selectedItem.getType() == Material.PAPER) {
                        ItemMeta selectedItemMeta = selectedItem.getItemMeta();
                        String countryName = ChatColor.stripColor(selectedItemMeta.getDisplayName());

                        // Находим страну по имени
                        Town country = TownyUniverse.getInstance().getTown(countryName);
                        if (country != null) {
                            // Привязываем страну к игроку
                            Army army = BookTownControl.GetArmyPlayerIn(selectedPlayer);
                            army.SetLink(selectedPlayer,country.getUUID());
                            player.sendMessage(ChatColor.GREEN + "Страна успешно привязана к "+playerName);
                            openInventoryArmyControl(player);
                        } else {
                            player.sendMessage(ChatColor.RED + "Страна не найдена.");
                        }
                    }
                    else if(selectedItem.getItemMeta().hasDisplayName() && selectedItem.getItemMeta().getDisplayName().contains("Назад")) {
                        showArmyMembersMenu(player);
                    }
                    else if(selectedItem.getItemMeta().hasDisplayName() && selectedItem.getItemMeta().getDisplayName().contains("Освободить от службы")) {
                        Army army = BookTownControl.GetArmyPlayerIn(player);
                        if(army.SetLink(selectedPlayer,null)) {
                            player.sendMessage("Игрок "+selectedPlayer.getName()+" освобождён от службы");
                        }
                        else {
                            player.sendMessage("Игрок "+selectedPlayer.getName()+" не может быть освобождён от службы из за войны");
                        }

                        showArmyMembersMenu(player);
                    }
                }
                else if (inventoryTitle.contains("Список стран")) {
                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    String itemName = ChatColor.stripColor(itemMeta.getDisplayName());

                    if (itemName.equals("Выйти")) {
                        openInventoryArmyControl(player);
                    } else {
                        // Получить страну из TownyUniverse по названию предмета
                        Town town = TownyUniverse.getInstance().getTown(itemName);
                        if (town != null) {
                            player.performCommand("army connect "+itemName);
                            player.closeInventory();
                            // Действия с полученной страной
                            // ...
                        }
                        else {
                            player.performCommand("army connect");
                        }
                    }
                }
                else if (inventoryTitle.contains("Страны")) {
                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    String itemName = ChatColor.stripColor(itemMeta.getDisplayName());

                    if (itemName.equals("Выйти")) {
                        openInventoryMAIN(player);
                    } else {
                        // Получить страну из TownyUniverse по названию предмета
                        Town town = TownyUniverse.getInstance().getTown(ChatColor.stripColor(itemName));
                        Town townIsPlayerIn = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTownOrNull();
                        if(townIsPlayerIn != null) {
                            return;
                        }
                        //                                                                                      ФИЩЩИФЫАЙЦУАЙЦАЙЦА
                        if (town != null) {
                            if(town.isOpen()) {
                                player.performCommand("town join "+ChatColor.stripColor(itemName));
                            }
                            else {
                                player.performCommand("country playerjoin "+ChatColor.stripColor(itemName));
                            }
                            player.closeInventory();
                            // Действия с полученной страной
                            // ...
                        }
                        else {
                            CountriesStatisticAndJoinMenu(player,1);
                        }
                    }
                }
                else if(inventoryTitle.contains("Сопряжённые страны")) {
                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    String itemName = ChatColor.stripColor(itemMeta.getDisplayName());
                    Army army = BookTownControl.GetArmyPlayerIn(player);
                    if (itemName.equals("Выйти")) {
                        openInventoryArmyControl(player);
                    } else {
                        // Получить страну из TownyUniverse по названию предмета
                        Town town = TownyUniverse.getInstance().getTown(itemName);
                        if (town != null) {
                            player.sendMessage("Армия отвязана от страны "+town.getName());
                            army.UnConnectCountry(town);
                            showLocalCountriesMenu(player,1);
                            // Действия с полученной страной
                        }
                        else {
                            showLocalCountriesMenu(player,1);
                        }
                    }

                }
                else if(inventoryTitle.contains("Список войн")) {
                    if (displayName.equals(ChatColor.YELLOW + "◀️ Назад")) {
                        // Нажата кнопка "Назад"
                        int currentPage = Integer.parseInt(event.getView().getTitle().split(" ")[2].split("/")[0].replace("Страница ", ""));
                        showGlobalWarsMenu(player, currentPage - 1);
                    }
                    else if (displayName.equals(ChatColor.YELLOW + "Вперед ▶️")) {
                        // Нажата кнопка "Вперед"
                        int currentPage = Integer.parseInt(event.getView().getTitle().split(" ")[2].split("/")[0].replace("Страница ", ""));
                        showGlobalWarsMenu(player, currentPage + 1);
                    }
                    else if(displayName.equals(ChatColor.YELLOW + "Выйти")) {
                        openInventoryMAIN(player);
                    }
                    else {
                        String[] townNames = ChatColor.stripColor(displayName.replace("Война между ","")).split(" и ");
                        String town1Name = townNames[0];
                        String town2Name = townNames[1];

                        List<War> wars = new ArrayList<>();
                        for (War war : BookTownControl.Wars) {
                            if (war.GetTownsByTwo(town1Name,town2Name)) {
                                CountryInventoryManager.showLocalWarsMenu(player,war);
                            }
                        }
                    }
                    return;
                }
                else if (inventoryTitle.contains("Моя страна")) {
                    Town town = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTownOrNull();
                    if(town == null) {
                        CountriesStatisticAndJoinMenu(player,1);
                    }
                    if (displayName.contains("Пригласить в страну")) {
                        player.closeInventory();
                        player.sendMessage("Введите ник игрока, которого хотите пригласить\nCancel для отмены");

                        CompletableFuture<Void> waitForResponse = new CompletableFuture<>();
                        ChatListener chatListener = new ChatListener(player, waitForResponse);

                        // Регистрируем слушатель событий чата
                        Bukkit.getPluginManager().registerEvents(chatListener, plugin);

                        // Ожидаем ответа от игрока с ограничением в 60 секунд
                        waitForResponse.orTimeout(60, TimeUnit.SECONDS).thenAccept(response -> {
                            // После получения ответа выполняем действия
                            String invitedPlayerName = chatListener.getResponse();

                            if (invitedPlayerName == null || invitedPlayerName.equalsIgnoreCase("cancel") || invitedPlayerName.equalsIgnoreCase("c") || invitedPlayerName.equalsIgnoreCase("n") || invitedPlayerName.equalsIgnoreCase("no") || invitedPlayerName.equalsIgnoreCase("отмена") || invitedPlayerName.equalsIgnoreCase("н") || invitedPlayerName.equalsIgnoreCase("нет") || invitedPlayerName.equalsIgnoreCase("о")) {
                                // Отмена
                                player.sendMessage(ChatColor.RED + "Приглашение отменено.");
                            } else {
                                String command = "town invite " + invitedPlayerName;
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    player.performCommand(command);
                                });
                            }

                            // Разблокируем чат
                            chatListener.unregisterChatListener(chatListener);
                        });
                    }

                    else if (displayName.contains("Список участников")) {
                        player.performCommand("town reslist");
                        player.closeInventory();
                    }
                    else if (displayName.contains("Уничтожить страну")) {//-
                        player.closeInventory();
                        player.performCommand("country remove "+town.getName()+" false");
                    }
                    else if (displayName.contains("Начать войну")) {//-
                        player.performCommand("country war");
                        //Connect to country
                    }
                    else if (displayName.contains("Покинуть страну")) {//-
                        player.closeInventory();
                        player.performCommand("town leave");
                    }
                    else if(displayName.contains("Свободный вход")) {
                        if(town.isOpen()) {
                            player.performCommand("country open");
                        }
                        else {
                            player.performCommand("town toggle open");
                        }
                        openInventoryCountryControl(player);
                    }
                    else if(displayName.contains("Рассмотреть контракты")) {
                        player.performCommand("amendlaws "+town.getName()+" contractbook");
                    }
                    /*else if(displayName.contains("Отозвать армию из страны")) {
                        showLocalCountriesMenu(player,1);
                    }*/
                    return;
                }
             // Отменить перемещение предмета
        }
    }


    public void changePlayerPriority(OfflinePlayer selectedPlayer, Player leadPlayer, Army army) {
        leadPlayer.closeInventory();
        leadPlayer.sendMessage("Введите ранк игрока от 1 до 5, если вы введёте 5, то ваш приоритет станет 4, и вы передадите лидерство\nОтмена для отмены");

        CompletableFuture<Void> waitForResponse = new CompletableFuture<>();
        ChatListener chatListener = new ChatListener(leadPlayer, waitForResponse);


        // Регистрируем слушатель событий чата
        Bukkit.getPluginManager().registerEvents(chatListener, plugin);

        // Ожидаем ответа от игрока с ограничением в 60 секунд
        waitForResponse.orTimeout(60, TimeUnit.SECONDS).thenAccept(response -> {
            // После получения ответа выполняем действия
            String message = chatListener.getResponse();

            if (message == null || message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel")) {
                // Отмена
                leadPlayer.sendMessage(ChatColor.RED + "Изменение приоритета отменено.");
            } else {
                try {
                    int priority = Integer.parseInt(message);
                    int leadPriority = army.getPlayer(leadPlayer).getPriority();
                    int youPriority = army.getPlayer(selectedPlayer).getPriority();

                    if (selectedPlayer == leadPlayer) {
                        leadPlayer.sendMessage(ChatColor.RED + "Вы не можете менять ранг себе.");
                    } else if (priority < 1 || priority > 5) {
                        leadPlayer.sendMessage(ChatColor.RED + "Неверный ранк. Ранк должен быть от 1 до 5.");
                    } else if (leadPriority != 5 && priority >= leadPriority) {
                        leadPlayer.sendMessage(ChatColor.RED + "Вы не можете выдать приоритет выше своего текущего приоритета.");
                    } else if (youPriority >= leadPriority && leadPriority != 5) {
                        leadPlayer.sendMessage(ChatColor.RED + "Вы не можете изменить приоритет игрока ранга выше вашего.");
                    } else {
                        // Логика для изменения приоритета игрока
                        // Например, вы можете использовать армейский API для изменения приоритета игрока
                        Army.ArmyPlayer armyPlayer = army.getPlayer(selectedPlayer);
                        army.addPlayer(selectedPlayer.getUniqueId(), priority);
                        leadPlayer.sendMessage(ChatColor.GREEN + "Приоритет успешно изменен на " + priority);
                    }
                } catch (NumberFormatException e) {
                    leadPlayer.sendMessage(ChatColor.RED + "Неверный формат ранка. Введите число от 1 до 5.");
                }
            }

            // Разблокируем чат
            chatListener.unregisterChatListener(chatListener);
        });
    }

    private static class MyHolder implements InventoryHolder {

        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public Inventory getInventory() {
            return inventory;
        }
    }
}