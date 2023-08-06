package gr.btc;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class InventoryManager implements Listener {

    private Plugin plugin;

    public InventoryManager(Plugin plugin) {
        this.plugin = plugin;
        //Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openInventoryMAIN(Player player) {
        MyHolder holder = new MyHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, "Главное меню");

        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta crossbowMeta = crossbow.getItemMeta();
        crossbowMeta.setDisplayName("Армии");
        crossbowMeta.addEnchant(Enchantment.DURABILITY, 1, true); // Скрытое зачарование
        crossbow.setItemMeta(crossbowMeta);

        // Расположите CROSSBOW в центре
        inventory.setItem(13, crossbow);

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
        chainMeta.setDisplayName("Присоединиться к армии");
        chainMeta.setLore(Arrays.asList("Описание предмета"));
        chain.setItemMeta(chainMeta);

        ItemStack enderEye = new ItemStack(Material.ENDER_EYE);
        ItemMeta enderEyeMeta = enderEye.getItemMeta();
        enderEyeMeta.setDisplayName("Создать армию");
        enderEyeMeta.setLore(Arrays.asList("Описание предмета"));
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
                skullMeta.setDisplayName(ChatColor.YELLOW + member.getPlayer().getDisplayName());

                // Создаем список для отображения дополнительной информации об игроке
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + "Ранг: " + member.getPriority());
                if(army.GetLink(member) == null) {
                    lore.add(ChatColor.GREEN + "Ничего не охраняет");
                }
                else {
                    lore.add(ChatColor.GOLD + "Охраняет: " + TownyUniverse.getInstance().getTown(army.GetLink(member)).getName());
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
        Inventory inventory = Bukkit.createInventory(holder, 27, "Управление бойцом "+selectedPlayer.getPlayer().getDisplayName());

        // Добавляем предметы в инвентарь для каждого действия
        ItemStack assignToCityItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta assignToCityMeta = assignToCityItem.getItemMeta();
        assignToCityMeta.setDisplayName(ChatColor.YELLOW + "Поставить на службу в город");
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
        changePriorityItem.setItemMeta(changePriorityMeta);
        inventory.setItem(14, changePriorityItem);

        // Открываем инвентарь игроку
        player.openInventory(inventory);
    }
    public static void openInventoryArmyControl(Player player) {
        MyHolder holder = new MyHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, "Военные конфликты");
        Army army = BookTownControl.GetArmyPlayerIn(player);
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
            ironSwordMeta.setDisplayName("Военные конфликты");
            ironSwordMeta.setLore(Arrays.asList("Ваша армия участвует в "+1+" военных конфликтах"));
            ironSword.setItemMeta(ironSwordMeta);
            inventory.setItem(4, ironSword);

            ItemStack barrier = new ItemStack(Material.STRUCTURE_VOID);
            ItemMeta barrierMeta = barrier.getItemMeta();
            barrierMeta.setDisplayName("Покинуть армию");
            barrierMeta.setLore(Arrays.asList("Описание предмета"));
            barrier.setItemMeta(barrierMeta);
            inventory.setItem(14, barrier);
        }

        if(priority >= 1) {
            ItemStack woodenSword = new ItemStack(Material.WOODEN_SWORD);
            ItemMeta woodenSwordMeta = woodenSword.getItemMeta();
            woodenSwordMeta.setDisplayName("Список бойцов");
            woodenSwordMeta.setLore(Arrays.asList("Описание предмета"));
            woodenSword.setItemMeta(woodenSwordMeta);
            inventory.setItem(13, woodenSword);
        }

        if(priority >= 2) {
            // Добавьте предметы для priority >= 2
        }

        if(priority >= 3) {
            ItemStack birchDoor = new ItemStack(Material.BIRCH_DOOR);
            ItemMeta birchDoorMeta = birchDoor.getItemMeta();
            birchDoorMeta.setDisplayName("Пригласить в армию");
            birchDoorMeta.setLore(Arrays.asList("Описание предмета"));
            birchDoor.setItemMeta(birchDoorMeta);
            inventory.setItem(11, birchDoor);
            if(army.isOpen()) {
                ItemStack redstoneTorch = new ItemStack(Material.REDSTONE_TORCH);
                ItemMeta redstoneTorchMeta = redstoneTorch.getItemMeta();
                redstoneTorchMeta.setDisplayName("Свободный вход включен");
                redstoneTorchMeta.setLore(Arrays.asList("Нажмите для переключения"));
                redstoneTorch.setItemMeta(redstoneTorchMeta);
                inventory.setItem(20, redstoneTorch);
            } else {
                // Если армия закрыта, добавляем Lever
                ItemStack lever = new ItemStack(Material.LEVER);
                ItemMeta leverMeta = lever.getItemMeta();
                leverMeta.setDisplayName("Свободный вход выключен");
                leverMeta.setLore(Arrays.asList("Нажмите для переключения"));
                lever.setItemMeta(leverMeta);
                inventory.setItem(20, lever);
            }
        }

        if(priority >= 4) {
            ItemStack ponder = new ItemStack(Material.GOAT_HORN);
            ItemMeta ponderMeta = ponder.getItemMeta();
            ponderMeta.setDisplayName("Присоединиться к стране");
            ponderMeta.setLore(Arrays.asList("Описание предмета"));
            ponder.setItemMeta(ponderMeta);
            inventory.setItem(12, ponder);
            ponder = new ItemStack(Material.TURTLE_HELMET);
            ponderMeta = ponder.getItemMeta();
            ponderMeta.setDisplayName("Отозвать армию из страны");
            ponderMeta.setLore(Arrays.asList("Описание предмета"));
            ponder.setItemMeta(ponderMeta);
            inventory.setItem(21, ponder);

        }

        if(priority >= 5) {
            ItemStack pufferfish = new ItemStack(Material.BARRIER);
            ItemMeta pufferfishMeta = pufferfish.getItemMeta();
            pufferfishMeta.setDisplayName("Распустить армию");
            pufferfishMeta.setLore(Arrays.asList("Описание предмета"));
            pufferfish.setItemMeta(pufferfishMeta);
            inventory.setItem(15, pufferfish);
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
    public void assignPlayerToCity(Player player, Player MainPlayer) {
        Army army = BookTownControl.GetArmyPlayerIn(player);
        MyHolder holder = new MyHolder();
        // Получаем список привязанных стран к армии
        List<UUID> connectedCountrys = army.GetConnectedCountrys();

        if (connectedCountrys.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Нет привязанных стран к армии.");
            player.closeInventory();
            return;
        }

        Inventory countryMenu = Bukkit.createInventory(holder, 27, "Пост игрока "+MainPlayer.getDisplayName());

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
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();
        if(clickedInventory == null) return;
        InventoryHolder holder = event.getClickedInventory().getHolder();
        if (clickedInventory == null || event.getClickedInventory().getType() != InventoryType.CHEST || clickedItem == null|| clickedItem.getType() == Material.AIR) {
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
                    if (displayName.equals("Армии")) {
                        openInventoryArmyCrOrJoin(player);
                        return;
                    }
                }
                else if(inventoryTitle.equalsIgnoreCase("Армии")) {
                    if (displayName.equals("Присоединиться к армии")) {
                        showArmiesMenu(player,1);
                    }
                    else if (displayName.equals("Создать армию")) {
                        player.closeInventory();
                        ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
                        int goldIngotCount = 0;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getType() == Material.GOLD_INGOT) {
                                goldIngotCount += item.getAmount();
                            }
                        } // Подсчитываем количество золотых слитков в инвентаре игрока

                        if (goldIngotCount >= 128) {
                            ItemStack takenGoldIngot = new ItemStack(Material.GOLD_INGOT, 128);
                            player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 64));
                            player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, 64));
                            player.sendMessage("Для отмены введите cancel");
                            player.sendMessage("Введите название армии в чате:");
                            // Регистрируем слушателя чата
                            ChatListener chatListener = new ChatListener(player);
                            Bukkit.getPluginManager().registerEvents(chatListener, plugin);
                            CommandListener commandListener = new CommandListener(player);
                            Bukkit.getPluginManager().registerEvents(commandListener, plugin);
                            commandListener.setBlockCommands(true);
                            commandListener.setOnCommandExecuted((command) -> {
                                player.sendMessage(ChatColor.RED + "Вы не можете использовать команды во время этой операции.");
                            });
                            chatListener.setChatEnabled(true);
                            chatListener.setOnChatMessageReceived((message) -> {
                                // Получаем название армии из сообщения

                                String armyName = message;
                                if (armyName.equalsIgnoreCase("cancel") || armyName.equalsIgnoreCase("c") || armyName.equalsIgnoreCase("n") || armyName.equalsIgnoreCase("no") || armyName.equalsIgnoreCase("отмена") || armyName.equalsIgnoreCase("н") || armyName.equalsIgnoreCase("нет") || armyName.equalsIgnoreCase("о")) {
                                    // Отмена
                                    player.sendMessage(ChatColor.RED + "Создание отменено, золото выброшено рядом с вами");
                                    World world = player.getWorld();
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            // Drop the item synchronously
                                            world.dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT,64));
                                            world.dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT,64));
                                        }
                                    }.runTask(plugin);

                                }
                                else {
                                    Army newArmy = new Army(armyName);
                                    newArmy.addPlayer(player.getUniqueId(),5);
                                    BookTownControl.Armys.add(newArmy);
                                    player.sendMessage("Армия "+armyName+" создана\n/army - меню армии");
                                }
                                // Разблокируем чат и удаляем слушателя чата
                                chatListener.setChatEnabled(false);
                                HandlerList.unregisterAll(chatListener);
                                commandListener.setBlockCommands(false);
                                HandlerList.unregisterAll(commandListener);
                            });
                            return;
                        }
                        else {
                            player.sendMessage("Для создания армии вам нужно иметь 2 стака золотых слитков.");
                            return;
                        }
                    }
                }
                else if (inventoryTitle.equalsIgnoreCase("Военные конфликты")) {
                    if (displayName.equals("Пригласить в армию")) {
                            // Регистрируем слушатель событий чата
                        player.closeInventory();
                        player.sendMessage("Введите ник игрока которого хотите пригласить\nCancel для отмены");
                            ChatListener chatListener = new ChatListener(player);
                            chatListener.setChatEnabled(true);
                            CommandListener commandListener = new CommandListener(player);
                            Bukkit.getPluginManager().registerEvents(commandListener, plugin);
                            commandListener.setBlockCommands(true);
                            commandListener.setOnCommandExecuted((command) -> {
                                player.sendMessage(ChatColor.RED + "Вы не можете использовать команды во время этой операции.");
                            });

                            chatListener.setOnChatMessageReceived((message) -> {
                                // Получаем ник игрока из сообщения
                                String invitedPlayerName = message;

                                // Выполняем команду "army add (ник игрока)"
                                if (invitedPlayerName.equalsIgnoreCase("cancel") || invitedPlayerName.equalsIgnoreCase("c") || invitedPlayerName.equalsIgnoreCase("n") || invitedPlayerName.equalsIgnoreCase("no") || invitedPlayerName.equalsIgnoreCase("отмена") || invitedPlayerName.equalsIgnoreCase("н") || invitedPlayerName.equalsIgnoreCase("нет") || invitedPlayerName.equalsIgnoreCase("о")) {
                                    // Отмена
                                    player.sendMessage(ChatColor.RED + "Приглашение отменено.");
                                }
                                else {
                                    String command = "army invite " + invitedPlayerName;
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        player.performCommand(command);
                                    });
                                }
                                // Разблокируем чат и удаляем слушателя событий чата
                                chatListener.setChatEnabled(false);
                                HandlerList.unregisterAll(chatListener);
                                commandListener.setBlockCommands(false);
                                HandlerList.unregisterAll(commandListener);
                            });

                            // Регистрируем слушатель событий чата
                            Bukkit.getPluginManager().registerEvents(chatListener, plugin);
                    }
                    else if (displayName.equals("Список бойцов")) { //+
                        showArmyMembersMenu(player);
                    }
                    else if (displayName.equals("Распустить армию")) {//-
                        player.closeInventory();
                        player.performCommand("army disband false");
                    }
                    else if (displayName.equals("Присоединиться к стране")) {//-
                        player.performCommand("army connect");
                         //Connect to country
                    }
                    else if (displayName.equals("Покинуть армию")) {//-
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
                        Army army = BookTownControl.GetArmyPlayerIn(Bukkit.getPlayer(ChatColor.stripColor(displayName)));
                        // Получаем выбранного игрока
                        if(army != null) {
                            Army.ArmyPlayer selectedPlayer = army.getPlayer(Bukkit.getPlayer(ChatColor.stripColor(displayName)));

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
                    Player selectedPlayer = Bukkit.getPlayer(playerName);

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
                        } else {
                            player.sendMessage("Неверное действие");
                        }
                    } else {
                        player.sendMessage("Ошибка при выборе игрока");
                    }
                    return;
                }
                else if (inventoryTitle.contains("Пост игрока")) {
                    String playerName = inventoryTitle.replace("Пост игрока ", "");
                    Player selectedPlayer = Bukkit.getPlayer(playerName);
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
                        army.SetLink(selectedPlayer,null);
                        player.sendMessage("Игрок "+selectedPlayer.getDisplayName()+" освобождён от службы");
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
                            // Действия с полученной страной
                            // ...
                        }
                        else {
                            player.performCommand("army connect");
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

             // Отменить перемещение предмета
        }
    }


    public void changePlayerPriority(Player selectedPlayer,Player leadPlayer , Army army) {
        leadPlayer.closeInventory();
        leadPlayer.sendMessage("Введите ранк игрока от 1 до 5, если вы введёте 5, то ваш приоритет станет 4, и вы передадите лидерство\nОтмена для отмены");

        ChatListener chatListener = new ChatListener(leadPlayer);
        chatListener.setChatEnabled(true);

        chatListener.setOnChatMessageReceived((message) -> {
            // Проверяем, была ли команда отмены
            if (message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel")) {
                // Отмена
                leadPlayer.sendMessage(ChatColor.RED + "Изменение приоритета отменено.");
            } else {
                try {
                    int priority = Integer.parseInt(message);
                    int leadPriority = army.getPlayer(leadPlayer).getPriority();
                    int YouPriority = army.getPlayer(selectedPlayer).getPriority();
                    if (priority >= leadPriority && leadPriority != 5) {
                        leadPlayer.sendMessage(ChatColor.RED + "Вы не можете выдать приоритет выше своего текущего приоритета.");
                        return;
                    }
                    else if(YouPriority >= leadPriority && leadPriority != 5) {
                        leadPlayer.sendMessage(ChatColor.RED + "Вы не можете изменить приоритет игрока ранга выше вашего.");
                    }
                    if (selectedPlayer == leadPlayer) {
                        leadPlayer.sendMessage(ChatColor.RED + "Вы не можете менять ранг себе.");
                        return;
                    }
                    if (priority >= 1 && priority <= 4) {
                        // Проверяем, что лидер не пытается изменить приоритет самому себе
                        // Проверяем, что лидер не пытается выдать приоритет выше своего текущего приоритета


                        // Логика для изменения приоритета игрока
                        // Например, вы можете использовать армейский API для изменения приоритета игрока
                        Army.ArmyPlayer armyPlayer = army.getPlayer(selectedPlayer);
                        army.addPlayer(selectedPlayer.getUniqueId(), priority);
                        leadPlayer.sendMessage(ChatColor.GREEN + "Приоритет успешно изменен на " + priority);
                    }
                    else if (priority == 5) {
                        // Проверяем, что лидер не пытается передать лидерство самому себе

                        // Логика для передачи лидерства
                        Army.ArmyPlayer armyPlayer = army.getPlayer(selectedPlayer);
                        army.addPlayer(leadPlayer.getUniqueId(), 4);
                        army.addPlayer(selectedPlayer.getUniqueId(), 5);
                        leadPlayer.sendMessage(ChatColor.GREEN + "Лидерство передано игроку " + selectedPlayer.getDisplayName());
                    } else {
                        leadPlayer.sendMessage(ChatColor.RED + "Неверный ранк. Ранк должен быть от 1 до 5.");
                    }
                } catch (NumberFormatException e) {
                    leadPlayer.sendMessage(ChatColor.RED + "Неверный формат ранка. Введите число от 1 до 5.");
                }
            }

            // Разблокируем чат и удаляем слушателя событий чата
            chatListener.setChatEnabled(false);
            HandlerList.unregisterAll(chatListener);
        });

        // Регистрируем слушатель событий чата
        Bukkit.getPluginManager().registerEvents(chatListener, plugin);
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