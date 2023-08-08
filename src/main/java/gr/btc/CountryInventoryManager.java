package gr.btc;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CountryInventoryManager implements Listener {

    private Plugin plugin;

    public CountryInventoryManager(Plugin plugin) {
        this.plugin = plugin;
        //Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void showArmiesMenu(Player player, int page) {
        Town town = new Town("");
            for(Town t : TownyAPI.getInstance().getTowns()) {
                if (t.getMayor().getPlayer().equals(player)) {
                    town = t;
                    break;
                }
            }
            if(town == null) {
                player.sendMessage("Вы не являетесь президентом ни одной из стран");
                player.closeInventory();
                return;
            }
                List<Army> armies = new ArrayList<>(BookTownControl.Armys);
                Iterator<Army> iterator = armies.iterator();
                while (iterator.hasNext()) {
                    Army army = iterator.next();
                    if (army.IsCountryConnected(town)) {
                        iterator.remove();
                    }
                }
                if (armies.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Пока что здесь нет армий.");
                    player.closeInventory();
                    return;
                }
                CountryHolder holder = new CountryHolder();
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


    public static void showGlobalCountriesMenu(Player player, int page) {
        List<Town> countries = new ArrayList<>(TownyUniverse.getInstance().getTowns());
        if(TownyUniverse.getInstance().getResident(player.getUniqueId()).getTownOrNull() != null) {
            countries.remove(TownyUniverse.getInstance().getResident(player.getUniqueId()).getTownOrNull());
        }

        if (countries.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Нет доступных стран для начала войны");
            player.closeInventory();
            return;
        }
        CountryHolder holder = new CountryHolder();
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
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.DARK_BLUE + "Объявить войну (Страница " + (page) + "/" + totalPages + ")");

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
    public static void showLocalWarsMenu(Player player, War war) {
        CountryHolder holder = new CountryHolder();

        // Создание инвентаря для меню стран
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatColor.RED + TownyUniverse.getInstance().getTown(war.sides1.get(0)).getName() + " и " + TownyUniverse.getInstance().getTown(war.sides2.get(0)).getName());
        boolean guest = TownyUniverse.getInstance().getTown(war.sides1.get(0)).getMayor().getUUID().equals(player.getUniqueId()) || TownyUniverse.getInstance().getTown(war.sides2.get(0)).getMayor().getUUID().equals(player.getUniqueId());
        guest = !guest;
        if (!guest) {
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
            for (UUID u : war.sides1) {
                SIDes1.add(TownyUniverse.getInstance().getTown(u).getName());
            }
            for (UUID u : war.sides2) {
                SIDes2.add(TownyUniverse.getInstance().getTown(u).getName());
            }
            for (OfflinePlayer p : war.side1Warriors) {
                if (p != null && p.getName() != null) {
                    Warriors1.add(p.getName());
                }
            }
            for (OfflinePlayer p : war.side2Warriors) {
                if (p != null && p.getName() != null) {
                    Warriors2.add(p.getName());
                }
            }
            lore.add(ChatColor.YELLOW + "Участники стороны 1: " + ChatColor.WHITE + String.join(", ", SIDes1));
            lore.add(ChatColor.YELLOW + "Участники стороны 2: " + ChatColor.WHITE + String.join(", ", SIDes2));
            lore.add(ChatColor.YELLOW + "Воины стороны 1: " + ChatColor.WHITE + String.join(", ", Warriors1));
            lore.add(ChatColor.YELLOW + "Воины стороны 2: " + ChatColor.WHITE + String.join(", ", Warriors2));

            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(4, item);

            item = new ItemStack(Material.PAPER);
            meta = item.getItemMeta();

            // Заголовок элемента списка
            displayName = ChatColor.GOLD + "Заключить мирный договор";
            meta.setDisplayName(displayName);
            lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Сторона " + TownyUniverse.getInstance().getTown( war.sides1.get(0)) + " " + (war.side1TryToStop ? "Подписала документ о ненападении" : ""));
            lore.add(ChatColor.YELLOW + "Сторона " + TownyUniverse.getInstance().getTown( war.sides2.get(0)) + " " + (war.side2TryToStop ? "Подписала документ о ненападении" : ""));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(11, item);
        }
        else {
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
            for (UUID u : war.sides1) {
                SIDes1.add(TownyUniverse.getInstance().getTown(u).getName());
            }
            for (UUID u : war.sides2) {
                SIDes2.add(TownyUniverse.getInstance().getTown(u).getName());
            }
            for (OfflinePlayer p : war.side1Warriors) {
                if (p != null && p.getName() != null) {
                    Warriors1.add(p.getName());
                }
            }
            for (OfflinePlayer p : war.side2Warriors) {
                if (p != null && p.getName() != null) {
                    Warriors2.add(p.getName());
                }
            }
            lore.add(ChatColor.YELLOW + "Участники стороны 1: " + ChatColor.WHITE + String.join(", ", SIDes1));
            lore.add(ChatColor.YELLOW + "Участники стороны 2: " + ChatColor.WHITE + String.join(", ", SIDes2));
            lore.add(ChatColor.YELLOW + "Воины стороны 1: " + ChatColor.WHITE + String.join(", ", Warriors1));
            lore.add(ChatColor.YELLOW + "Воины стороны 2: " + ChatColor.WHITE + String.join(", ", Warriors2));

            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(4, item);
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
        if (clickedInventory == null || event.getClickedInventory().getType() != InventoryType.CHEST || clickedItem == null|| clickedItem.getType() == Material.AIR) {
            // Clicked inventory is null, handle the situation accordingly
            if (holder instanceof CountryHolder) {
                event.setCancelled(true);
            }
            return;
        }

        if (holder instanceof CountryHolder) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            String displayName = clickedItem.getItemMeta().getDisplayName();
            String inventoryTitle = event.getView().getTitle();
            ItemMeta itemMeta = clickedItem.getItemMeta();
            String itemName = ChatColor.stripColor(itemMeta.getDisplayName());


            if (inventoryTitle.contains("Список армий")) {
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
                    player.closeInventory();
                }
                else {
                    String armyName = ChatColor.stripColor(displayName);
                    // Выполняем команду army join (название армии)
                    player.performCommand("country join " + armyName);
                    player.closeInventory();
                }
                return;
            }
            else if (inventoryTitle.contains("Объявить войну")) {
                if (itemName.equals("Выйти")) {
                    player.closeInventory();
                } else {
                    // Получить страну из TownyUniverse по названию предмета
                    Town town = TownyUniverse.getInstance().getTown(itemName);
                    if (town != null) {
                        player.performCommand("country war "+itemName);
                        player.closeInventory();
                    }
                    else {
                        showGlobalCountriesMenu(player,1);
                    }
                }
            }
            else {
                if (itemName.contains("Выйти")) {
                    player.closeInventory();
                }
                else if(itemName.contains("Заключить мирный договор")) {
                    String[] townNames = ChatColor.stripColor(inventoryTitle).split(" и ");
                    String town1Name = townNames[0];
                    String town2Name = townNames[1];

                    List<War> wars = new ArrayList<>();
                    for (War war : BookTownControl.Wars) {
                        if (war.GetTownsByTwo(town1Name,town2Name)) {
                            if(TownyUniverse.getInstance().getTown(town1Name).getMayor().getUUID().equals(player.getUniqueId())) {
                                war.SwitchSide(true, false);
                            }
                            else if(TownyUniverse.getInstance().getTown(town2Name).getMayor().getUUID().equals(player.getUniqueId())) {
                                war.SwitchSide(false, true);
                            }

                            showLocalWarsMenu(player,war);
                        }
                    }

                }
            }
        }
    }
    private static class CountryHolder implements InventoryHolder {

        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public Inventory getInventory() {
            return inventory;
        }
    }
}
