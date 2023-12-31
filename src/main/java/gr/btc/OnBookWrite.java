package gr.btc;

import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.InvalidNameException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.regen.PlotBlockData;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.tasks.TownClaim;
import com.palmergames.bukkit.towny.utils.MapUtil;
import com.palmergames.bukkit.util.BukkitTools;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OnBookWrite implements Listener {
    public static Map<UUID, Long> playerSelectionTime = new HashMap<>();
    public static Map<UUID, BukkitRunnable> playerSelectionTask = new HashMap<>();
    @EventHandler
    public void onBookWrite(PlayerEditBookEvent event) {
        if (!(event.getNewBookMeta().getDisplayName().equalsIgnoreCase(main.genGr("Книга создания страны","#FF0E6C","#C90AFF"))) || !event.isSigning()) {
            return;
        }

        Player player = event.getPlayer();
        List<String> pages = event.getNewBookMeta().getPages();
        Resident resik = TownyUniverse.getInstance().getResident(player.getName());
        ItemStack book = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack bookoff = event.getPlayer().getInventory().getItemInOffHand();
        for (Town t : TownyUniverse.getInstance().getTowns()) {
                if (t.getResidents().contains(resik)) {
                    if (Objects.equals(t.getMayor().getPlayer(), resik.getPlayer()) || BookTownControl.townAddition.get(t.getUUID()).retso().equals(player.getUniqueId())) {
                        if (book.getItemMeta() != null && book.getItemMeta().hasDisplayName() && book.getItemMeta().getDisplayName().equalsIgnoreCase(main.genGr("Книга создания страны", "#FF0E6C", "#C90AFF"))) {
                            Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), new Runnable() {
                                public void run() {
                                    player.getInventory().setItemInMainHand(null);
                                    player.updateInventory();
                                    GiveItemToFounder(t.getMayor().getPlayer(), t, true);
                                }
                            }, 1);

                            return; // Выходим из цикла после удаления книги
                        }
                        else if (bookoff.getItemMeta().hasDisplayName() && bookoff.getItemMeta().getDisplayName().equalsIgnoreCase(main.genGr("Книга создания страны", "#FF0E6C", "#C90AFF"))) {
                            Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), new Runnable() {
                                public void run() {
                                    player.getInventory().setItemInOffHand(null);
                                    player.updateInventory();
                                    GiveItemToFounder(t.getMayor().getPlayer(), t, true);
                                }
                            }, 1);

                            return; // Выходим из цикла после удаления книги
                        }
                    }
                    else {
                        player.sendMessage("Вы уже в стране, но у вас недостаточно прав");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        event.setCancelled(true);
        if (pages.size() < 4) {
            player.sendMessage(ChatColor.RED + "Книга должна содержать 4 страницы! У вас - "+pages.size());
            return;
        } else if(pages.size() > 5) {
            player.sendMessage(ChatColor.RED + "Книга должна содержать 4 страницы! У вас - "+pages.size());
            return;
        } //Книга должна содержать 4 страницы не больше не меньше
        //Проверка страниц

        String capitalLocation = pages.get(0); capitalLocation = capitalLocation.replace("Координаты страны: ", "");
        int x = 0;
        int y = 0;
        try {
            String[] coordinates = capitalLocation.split(" ");
            if (coordinates.length != 2) {
                player.sendMessage(ChatColor.RED + "Координаты указаны неверно!");
                return;
            }
            x = Integer.parseInt(coordinates[0]) >> 4; // Получаем номер чанка по координате X
            y = Integer.parseInt(coordinates[1]) >> 4; // Получаем номер чанка по координате Z
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Координаты указаны неверно!");
            return;
        }
        String founder = pages.get(1); founder = founder.replace("Президент: ", ""); Player fonder = Bukkit.getPlayer(founder);
        String playerList = pages.get(2); playerList = playerList.replace("Жители: ", "");
        String countryName = pages.get(3); countryName = countryName.replace("Название страны: ", "");

        if (capitalLocation.isEmpty() || founder.isEmpty() || countryName.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Не все данные указаны в книге!");
            return;
        } // Проверка, что все данные указаны
        if(fonder == null) {
            player.sendMessage(ChatColor.RED+"Игрок не на сервере либо у него некорректный ник");
            return;
        } //Если тот кто хочет основать страну не на сервере
        for(Town t : TownyAPI.getInstance().getTowns()) {
            if(t.getName().equals(countryName)) {
                player.sendMessage(ChatColor.RED+"Название страны уже занято, либо оно некорректно");
                return;
            }
        } //Некорректное название
        /*if(!player.isOp()) {
            String goa = getOnlineAdmins();
            WorldCoord coordsCheck = new WorldCoord(fonder.getWorld(),x,y);
            if (isChunkOccupied(coordsCheck, player.getWorld(), player)) {
                return;
            }
            if(goa.equals("")) {
                player.sendMessage("Сейчас на сервере нет админов, сообщите об этом в канал в дискорде");
            } else {
                player.sendMessage("Вся информация заполнена корректно, передайте эту книгу любому из них :\n"+goa);
            } //Отображение админов для того чтобы им написать
        } //Если игрок обычный
        else {*/
            if(!fonder.isOnline()) {
                player.sendMessage("Основатель вышел из сети");
                return;
            }
            Resident nr = new Resident(fonder.getName());
            WorldCoord coordsCheck = new WorldCoord(fonder.getWorld(),x,y);
            if (isChunkOccupied(coordsCheck, player.getWorld(), player)) {
                return;
            }
            WorldCoord coords = new WorldCoord(fonder.getWorld(),x,y);
            try {
                Town town = newTown(coords.getTownyWorld(),countryName,nr,coords.getCoord(),new Location(fonder.getWorld(),0,0,0),nr.getPlayer());
                String ps = playerList;
                Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), new Runnable() {
                    public void run() {
                        removeBookFromInventory(player);
                        GiveItemToFounder(fonder,town,false);
                        Towny.getPlugin().onEnable();
                        invitePlayers(player, Arrays.asList(ps.split(" ")));
                    }
                }, 1);

            } catch (TownyException e) {
                player.sendMessage("Ошибка "+e);
                throw new RuntimeException(e);
            } //countryName

        /*} //Если это админчик*/
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
            if(e.getResult() == null) {
                return;
            }
            AnvilInventory ai = e.getInventory();
            if(e.getResult().getType() == Material.WRITABLE_BOOK && oneof(e.getResult().getItemMeta().getDisplayName())) {
                ai.setRepairCost(10);
            }
            else if(e.getResult().getType() == Material.WRITABLE_BOOK && e.getResult().getItemMeta().getDisplayName().equalsIgnoreCase("законы")) {
                ai.setRepairCost(5);
            }
            else if(e.getResult().getType() == Material.WRITABLE_BOOK && e.getResult().getItemMeta().getDisplayName().equalsIgnoreCase("контракты")) {
                ai.setRepairCost(5);
            }
    }
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (item.getType() == Material.WRITABLE_BOOK && oneof(itemMeta.getDisplayName())) {
            BookMeta bMeta = (BookMeta) item.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE+"В книге должно быть 4 страницы:");
            lore.add(ChatColor.GRAY+"1: X и Y чанка");
            lore.add(ChatColor.WHITE+"2: Ваш ник");
            lore.add(ChatColor.GRAY+"3: Ваши жители через запятую");
            lore.add(ChatColor.WHITE+"4: Название вашей страны");
            lore.add("");
            lore.add(ChatColor.GREEN+"Гайд - /guide");
            lore.add("");
            lore.add(ChatColor.GRAY+"Ctrl+A - выделить всю страницу");
            lore.add("");
            lore.add(ChatColor.GRAY+"Чтобы открыть книгу и перепроверить данные вручную");
            lore.add(ChatColor.GRAY+"Нажмите Q (Выбросить предмет)");
            lore.add("");
            int minX = -2000;
            int maxX = 2000;
            int minY = -2000;
            int maxY = 2000;

            int randomX = new Random().nextInt(maxX - minX + 1) + minX;
            int randomY = new Random().nextInt(maxY - minY + 1) + minY;

            String prompt = "X Y координаты\n\nПример:\n " + randomX + " " + randomY + "\nВозьмётся чанк:\n " + randomX/8 + " " + randomY/8;

            if (bMeta.getPageCount() < 4) {
                int pagesToAdd = 4 - bMeta.getPageCount();
                for (int i = 0; i < pagesToAdd; i++) {
                    bMeta.addPage("");
                }
            }
            bMeta.setPage(1, prompt);
            bMeta.setPage(2,"Президент: "+e.getWhoClicked().getName());
            bMeta.setPage(3,"Ники жителей через пробел");
            bMeta.setPage(4,"Название вашей страны");

            bMeta.setLore(lore);
            bMeta.setDisplayName(main.genGr("Книга создания страны","#FF0E6C","#C90AFF"));
            p.playSound(p.getLocation().add(0,50,0), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 50f, 1f);
            p.playSound(p.getLocation().add(0,50,0), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 50f, 1.5f);
            p.playSound(p.getLocation().add(0,50,0), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 50f, 2f);
            item.setItemMeta(bMeta);
        }
        else if (item.getType() == Material.WRITABLE_BOOK && itemMeta.getDisplayName().equalsIgnoreCase("законы")) {
            if(!item.getItemMeta().hasLore() || item.getItemMeta().getLore().isEmpty()) {
                itemMeta.setDisplayName(main.genGr("Законы","#eb1a1a","#cc0aaa"));
                p.playSound(p.getLocation().add(0,50,0), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 50f, 0.9f);
                p.playSound(p.getLocation().add(0,50,0), Sound.BLOCK_BELL_USE, 50f, 0.9f);
                p.playSound(p.getLocation().add(0,50,0), Sound.ITEM_TRIDENT_THUNDER, 50f, 1.4f);
                item.setItemMeta(itemMeta);
            }
            else {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED+"Эта книга не подходит для создания книги законов");
                p.playSound(p.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
            }
        }
        else if (item.getType() == Material.WRITABLE_BOOK && itemMeta.getDisplayName().equalsIgnoreCase("контракты")) {
            if(!item.getItemMeta().hasLore() || item.getItemMeta().getLore().isEmpty()) {
                itemMeta.setDisplayName(main.genGr("Контракты","#cc4444","#ff0000"));
                p.playSound(p.getLocation().add(0,50,0), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 50f, 0.9f);
                p.playSound(p.getLocation().add(0,50,0), Sound.BLOCK_BELL_USE, 50f, 0.9f);
                p.playSound(p.getLocation().add(0,50,0), Sound.ITEM_TRIDENT_THUNDER, 50f, 1.4f);
                item.setItemMeta(itemMeta);
            }
            else {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED+"Эта книга не подходит для создания книги законов");
                p.playSound(p.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
            }
        }
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack newMainHandItem = event.getItemDrop().getItemStack();

        // Проверяем, что новый предмет в руке - книга с определенным названием
        if (newMainHandItem.getType().equals(Material.WRITABLE_BOOK)) {
            BookMeta bookMeta = (BookMeta) newMainHandItem.getItemMeta();
            //System.out.println("Swap");
            //Bukkit.getLogger().info(main.genGr("Книга создания страны","#FF0E6C","#C90AFF").toLowerCase()+"  " + bookMeta.getDisplayName().toLowerCase());
            if (bookMeta != null && bookMeta.hasDisplayName() && bookMeta.getDisplayName().toLowerCase().contains(main.genGr("Книга создания страны","#FF0E6C","#C90AFF").toLowerCase())) {
                // Открываем книгу
                event.setCancelled(true);
                if (newMainHandItem.getItemMeta().hasLore()) {
                    List<String> lore = newMainHandItem.getItemMeta().getLore();
                    if (lore.get(lore.size() - 2).contains("Открыто")) {
                        lore.remove(lore.size() - 2); // Удаляем последний элемент
                        player.sendMessage("Выбран автоматический режим заполнения книги");
                    }
                    else {
                        lore.add(lore.size() - 1,  ChatColor.GREEN+"Открыто для чтения");
                        player.sendMessage("Выбран ручной режим заполнения книги");
                    }
                    ItemMeta itemMeta = newMainHandItem.getItemMeta();
                    itemMeta.setLore(lore);
                    newMainHandItem.setItemMeta(itemMeta);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        // Проверка нажатия ПКМ и удержания клавиши Shift

        if (event.getAction().name().contains("RIGHT_CLICK") && player.isSneaking() && mainHandItem.getType() == Material.WRITTEN_BOOK) {
            //main.genGr("Книга создания страны","#FF0E6C","#C90AFF")) - название той книги которую нужно открывать в новом методе

            // Проверка наличия книги с определенным названием в основной руке
            Town town = new Town("");
            try {
                town = TownyAPI.getInstance().getResident(player.getName()).getTown();
            } catch (NotRegisteredException e) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"У вас нет прав на открытие новой территории в чужой стране"));
            }
            String bookname = "";
            bookname = main.genGr("Книга страны ","#4AFBFB","#2270F0")+main.genGr(town.getName(),"#BC0000","#FB7000");

            if (mainHandItem.getItemMeta().hasDisplayName() && mainHandItem.getItemMeta().getDisplayName().equalsIgnoreCase(bookname)) {
                // Выполнение команды относительно игрока
                if(town.getMayor().getPlayer().equals(player)) {
                    event.setCancelled(true);
                    player.performCommand("town claim");
                    setBookLore(mainHandItem,town,true); // LORE
                }
                else {
                    event.setCancelled(true);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"У вас нет прав на открытие новой территории"));
                }
            }
            else if(mainHandItem.getItemMeta().getDisplayName().toLowerCase().contains(main.genGr("Книга страны ","#4AFBFB","#2270F0").toLowerCase())) {
                event.setCancelled(true);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"У вас нет прав на открытие новой территории в чужой стране"));
            }
        }
        else if(event.getAction().name().contains("RIGHT_CLICK") && mainHandItem.getType() == Material.WRITTEN_BOOK && mainHandItem.getItemMeta().hasDisplayName() && mainHandItem.getItemMeta().getDisplayName().toLowerCase().contains(main.genGr("Книга страны ","#4AFBFB","#2270F0").toLowerCase())) {

            BookMeta bm = (BookMeta) mainHandItem.getItemMeta();
            int indexOfNewLine = bm.getPage(1).replace("Страна: ","").indexOf("\n");
            Town town = new Town("");
            for(Town tl : TownyUniverse.getInstance().getTowns()) {
                if(tl.getName().equalsIgnoreCase(bm.getPage(1).replace("Страна: ","").substring(0,indexOfNewLine).replaceAll("§[0-9a-fA-Fklmnor]", ""))) {
                    town = tl;
                    break;
                }
            }
            if(town.equals(new Town(""))) {
                player.sendMessage(ChatColor.RED+"Книга страны не актуальна, можете её выкинуть ☺");
                return;
            }
            event.setCancelled(true);
            if(TownyAPI.getInstance().getResident(player.getName()).hasTown()) {
                try {
                    town = TownyAPI.getInstance().getResident(player.getName()).getTown();
                } catch (NotRegisteredException e) {
                    throw new RuntimeException(e);
                } //Получаем город

                if(town.getMayor().getPlayer().equals(player)) { //Mayor
                    bm.spigot().setPages(Lists(town,2));
                    setBookLore(mainHandItem,town,true);
                } //Если игрок основатель
                else if(BookTownControl.townAddition.get(town.getUUID()).retso().equals(player.getUniqueId())) {
                    bm.spigot().setPages(Lists(town,1));
                    setBookLore(mainHandItem,town,true);
                }
                else {
                    bm.spigot().setPages(Lists(town,0));//NoMayor
                    setBookLore(mainHandItem,town,false);
                } //Если игрок не основатель
            }
            else {
                //No have town exception
                bm.spigot().setPages(Lists(town,0));
                setBookLore(mainHandItem,town,false);
            } //Если игрок ни в одном городе
            mainHandItem.setItemMeta(bm);
            player.openBook(mainHandItem);
        }
        else if(event.getAction().name().contains("RIGHT_CLICK") && mainHandItem.getType() == Material.WRITABLE_BOOK && mainHandItem.getItemMeta().hasDisplayName() && mainHandItem.getItemMeta().getDisplayName().toLowerCase().contains(main.genGr("Книга создания страны","#FF0E6C","#C90AFF").toLowerCase())) {
            //System.out.println("9u4ki");
            List<String> lore = mainHandItem.getItemMeta().getLore();
            if (lore.get(lore.size() - 2).contains("Открыто")) {
                return;
            } //Если открыто для чтения то не выполнять код
            Resident resik = TownyUniverse.getInstance().getResident(player.getName());
            ItemStack book = event.getPlayer().getInventory().getItemInMainHand();
            player.closeInventory();
            for (Town t : TownyUniverse.getInstance().getTowns()) {
                if (t.getResidents().contains(resik)) {
                    if (Objects.equals(t.getMayor().getPlayer(), resik.getPlayer()) || BookTownControl.townAddition.get(t.getUUID()).retso().equals(player.getUniqueId())) {
                        if (book.getItemMeta().hasDisplayName() && book.getItemMeta().getDisplayName().equalsIgnoreCase(main.genGr("Книга создания страны", "#FF0E6C", "#C90AFF"))) {
                            Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), new Runnable() {
                                public void run() {
                                    event.getPlayer().getInventory().setItemInMainHand(null);
                                    event.getPlayer().updateInventory();
                                    GiveItemToFounder(t.getMayor().getPlayer(), t, true);
                                }
                            }, 1);

                            return; // Выходим из цикла после удаления книги
                        }
                    }
                    else {
                        player.sendMessage("Вы уже в стране, но у вас недостаточно прав");
                        event.setCancelled(true);
                        return;
                    }
                }
            } //Вернуть книгу если надо
            BookMeta bMeta = (BookMeta) mainHandItem.getItemMeta();
            AFB afn = new AFB();
            afn.Start(player,bMeta,mainHandItem);
            player.getInventory().remove(mainHandItem);
        }
    }

    boolean oneof(String cur) {
        String[] maybit = {"CountryBook","Country Book","Книга создания страны", "КСС", "CB"};
        for(String str : maybit) {
            if(str.equalsIgnoreCase(cur)) {
                return true;
            }
        }
        return false;
    }
    String getOnlineAdmins() {
        String onlineAdmins = "";
        for (Player adm : Bukkit.getOnlinePlayers()) {
            if (adm.isOp()) {
                onlineAdmins += adm.getName() + ", ";
            }
        }
        return onlineAdmins;
    }
    BaseComponent[][] Lists(Town town, int isAdmin) {
        boolean exists = false;
        for(Town t : TownyUniverse.getInstance().getTowns()) {
            if(t.equals(town)) {
                exists = true;
                break;
            }
        }

        if (exists) {
            if(isAdmin == 2) {
                TownBlock tb = town.getHomeBlockOrNull();
                List<BaseComponent[]> pages = new ArrayList<>();

                TextComponent page1 = new TextComponent("Страна: " + ChatColor.GOLD + town.getName() + ChatColor.RESET + "\n");
                if (tb != null) {
                    TextComponent chunkInfo = new TextComponent("Корневой чанк: " + tb.getCoord().getX() + "," + tb.getCoord().getZ() + "\n");
                    page1.addExtra(chunkInfo);
                }
                TextComponent populationInfo = new TextComponent("Население: " + town.getResidents().size() + "\n");
                page1.addExtra(populationInfo);
                int Warriors = 0;
                for(Army army : BookTownControl.Armys) {
                    if(army.IsCountryConnected(town)) {
                        for(Army.ArmyPlayer ap : army.getPlayers()) {
                            if(Objects.equals(army.GetLink(ap), town.getUUID())) {
                                Warriors++;
                            }
                        }

                    }
                }
                DecimalFormat decimalFormat = new DecimalFormat("#");
                populationInfo = new TextComponent("Численность армии: " + Warriors + " \\ " + decimalFormat.format(Math.ceil(town.getResidents().size() / 3.0)) + "\n");
                page1.addExtra(populationInfo);
                TextComponent leaderInfo = new TextComponent("Лидер: " + town.getMayor().getPlayer().getDisplayName());
                page1.addExtra(leaderInfo);
                if(BookTownControl.townAddition.get(town.getUUID()).retso() != null) {
                    leaderInfo = new TextComponent("\nЗаместитель: " + BookTownControl.townAddition.get(town.getUUID()).retso());
                }
                else {
                    leaderInfo = new TextComponent("\nУстановить заместителя");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/country coowner (ник)").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                }
                page1.addExtra(leaderInfo);
                if(town.isOpen()) {
                    leaderInfo = new TextComponent("\nВход открыт");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите для переключения").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                    leaderInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country open"));
                }
                else {
                    leaderInfo = new TextComponent("\nВход закрыт");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите для переключения").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                    leaderInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town toggle open"));
                }
                page1.addExtra(leaderInfo);
                pages.add(new BaseComponent[]{page1});

                TextComponent page2 = new TextComponent("Горожане: \n");
                int counter = 1;
                boolean isOrigin = true;
                for (Resident r : town.getResidents()) {
                    if(!isOrigin) {
                        page2.addExtra(", ");
                    }
                    else {
                        isOrigin = false;
                    }
                    TextComponent playerName = new TextComponent("["+counter+"] ");
                    if(r.getUUID() != null) {
                        if (!(Bukkit.getOfflinePlayer(r.getUUID())).getName().equals(town.getMayor().getPlayer().getName())) {
                            playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Bukkit.getOfflinePlayer(r.getUUID()).getName() + "\nУдалить игрока").color(ChatColor.RED.asBungee()).create()));
                            playerName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town kick " + Bukkit.getOfflinePlayer(r.getUUID()).getName()));
                        } else {
                            playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Objects.requireNonNull(r.getPlayer()).getName() + "\nЭто вы").color(ChatColor.GREEN.asBungee()).create()));
                        }

                        counter++;
                        page2.addExtra(playerName);
                    }
                }

                page2.addExtra(".");

                TextComponent plusSign = new TextComponent("  [+]");
                plusSign.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Добавить нового игрока").color(ChatColor.DARK_GREEN.asBungee()).create()));
                plusSign.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tinvite"));
                page2.addExtra(plusSign);
                pages.add(new BaseComponent[]{page2});
                TextComponent line1 = new TextComponent(ChatColor.RED + "Уничтожить страну\n");
                line1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country remove " + town.getName()+" false"));

                TextComponent line2 = new TextComponent();
                TextComponent line7 = new TextComponent();
                if (BookTownControl.townAddition.get(town.getUUID()).getTownItemMap() == null || BookTownControl.townAddition.get(town.getUUID()).getTownItemMap().isEmpty()) {
                    line2.setText(ChatColor.RED + "Внести книгу \nзаконов из руки\n");
                } else {
                    line2.setText(ChatColor.GOLD + "Внести поправки\nв законы\n");
                }
                //main.genGr("Контракты","#cc4444","#ff0000")
                line2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amendlaws " + town.getName()));

                TextComponent line4 = new TextComponent(ChatColor.DARK_GREEN + "Добавить чанк\n (SHIFT+ПКМ)\n");
                line4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town claim"));

                TextComponent line5 = new TextComponent(ChatColor.RED + "Удалить чанк");
                line5.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town unclaim"));

                TextComponent line6 = new TextComponent();
                    line6 = new TextComponent(ChatColor.DARK_GREEN + "\nРассмотреть контракты\n");
                    line6.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amendlaws "+town.getName()+" contractbook"));
                if(BookTownControl.CheckForWar(town)) {
                    line7 = new TextComponent(ChatColor.DARK_RED + "Информация о войне");
                    line7.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country war"));
                }
                else {
                    line7 = new TextComponent(ChatColor.DARK_RED + "Начать войну");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Вы перейдёте в меню для выбора страны\nС которой можете начать войну").create());
                    line7.setHoverEvent(hoverEvent);
                    line7.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country war"));
                }
                pages.add(new BaseComponent[]{line1, line2, line4, line5,line6,line7});
                return pages.toArray(new BaseComponent[0][]);
            }
            else if(isAdmin == 1) {
                TownBlock tb = town.getHomeBlockOrNull();
                List<BaseComponent[]> pages = new ArrayList<>();

                TextComponent page1 = new TextComponent("Страна: " + ChatColor.GOLD + town.getName() + ChatColor.RESET + "\n");
                if (tb != null) {
                    TextComponent chunkInfo = new TextComponent("Корневой чанк: " + tb.getCoord().getX() + "," + tb.getCoord().getZ() + "\n");
                    page1.addExtra(chunkInfo);
                }
                TextComponent populationInfo = new TextComponent("Население: " + town.getResidents().size() + "\n");
                page1.addExtra(populationInfo);
                int Warriors = 0;
                for(Army army : BookTownControl.Armys) {
                    if(army.IsCountryConnected(town)) {
                        for(Army.ArmyPlayer ap : army.getPlayers()) {
                            if(Objects.equals(army.GetLink(ap), town.getUUID())) {
                                Warriors++;
                            }
                        }

                    }
                }
                DecimalFormat decimalFormat = new DecimalFormat("#");
                populationInfo = new TextComponent("Численность армии:\n " + Warriors + " \\ " + decimalFormat.format(Math.ceil(town.getResidents().size() / 3.0)) + "\n");
                page1.addExtra(populationInfo);
                TextComponent leaderInfo = new TextComponent("Лидер: " + town.getMayor().getPlayer().getDisplayName());
                page1.addExtra(leaderInfo);
                if(BookTownControl.townAddition.get(town.getUUID()).retso() != null) {
                    leaderInfo = new TextComponent("\nЗаместитель: " + BookTownControl.townAddition.get(town.getUUID()).retso());
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/country coowner noone - чтобы удалить поста заместителя").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                }
                else {
                    leaderInfo = new TextComponent("\n\nУстановить заместителя");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/country coowner (ник)").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                }
                page1.addExtra(leaderInfo);
                if(town.isOpen()) {
                    leaderInfo = new TextComponent("\nВход открыт");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите для переключения").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                    leaderInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country open"));
                }
                else {
                    leaderInfo = new TextComponent("\nВход закрыт");
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите для переключения").create());
                    leaderInfo.setHoverEvent(hoverEvent);
                    leaderInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town toggle open"));
                }
                page1.addExtra(leaderInfo);
                pages.add(new BaseComponent[]{page1});

                TextComponent page2 = new TextComponent("Горожане: \n");
                int counter = 1;
                boolean isOrigin = true;
                for (Resident r : town.getResidents()) {
                    if(!isOrigin) {
                        page2.addExtra(", ");
                    }
                    else {
                        isOrigin = false;
                    }
                    TextComponent playerName = new TextComponent("["+counter+"] ");
                    if(!(Objects.requireNonNull(r.getPlayer()).getName().equals(town.getMayor().getPlayer().getDisplayName()))) {
                        playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Objects.requireNonNull(r.getPlayer()).getName()+"\nУдалить игрока").color(ChatColor.RED.asBungee()).create()));
                        playerName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town kick " + r.getPlayer().getName()));
                    }
                    else {
                        playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Objects.requireNonNull(r.getPlayer()).getName()+"\nЭто вы").color(ChatColor.GREEN.asBungee()).create()));
                    }
                    counter++;
                    page2.addExtra(playerName);

                }

                page2.addExtra(".");

                TextComponent plusSign = new TextComponent("  [+]");
                plusSign.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Добавить нового игрока").color(ChatColor.DARK_GREEN.asBungee()).create()));
                plusSign.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tinvite"));
                page2.addExtra(plusSign);
                pages.add(new BaseComponent[]{page2});
                TextComponent line2 = new TextComponent();
                if (BookTownControl.townAddition.get(town.getUUID()).getTownItemMap() == null || BookTownControl.townAddition.get(town.getUUID()).getTownItemMap().isEmpty()) {
                    line2.setText(ChatColor.RED + "Внести книгу \nзаконов из руки\n");
                } else {
                    line2.setText(ChatColor.GOLD + "Внести поправки\nв законы\n");
                }
                //main.genGr("Контракты","#cc4444","#ff0000")
                line2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amendlaws " + town.getName()));

                TextComponent line4 = new TextComponent(ChatColor.DARK_GREEN + "Добавить чанк\n (SHIFT+ПКМ)\n");
                line4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town claim"));

                TextComponent line5 = new TextComponent(ChatColor.RED + "Удалить чанк\n");
                line5.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town unclaim"));

                TextComponent line6 = new TextComponent();
                    line6 = new TextComponent(ChatColor.DARK_GREEN + "\nРассмотреть контракты\n");
                    line6.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amendlaws "+town.getName()+" contractbook"));
                pages.add(new BaseComponent[]{line2, new TextComponent("\n"), line4, new TextComponent("\n"), line5,line6});
                return pages.toArray(new BaseComponent[0][]);
            }
            else {
                TownBlock tb = town.getHomeBlockOrNull();
                List<BaseComponent[]> pages = new ArrayList<>();

                TextComponent page1 = new TextComponent("Страна: " + ChatColor.GOLD + town.getName() + ChatColor.RESET + "\n");
                if (tb != null) {
                    TextComponent chunkInfo = new TextComponent("Корневой чанк: " + tb.getCoord().getX() + "," + tb.getCoord().getZ() + "\n");
                    page1.addExtra(chunkInfo);
                }
                TextComponent populationInfo = new TextComponent("Население: " + town.getResidents().size() + "\n");
                page1.addExtra(populationInfo);
                int Warriors = 0;
                for(Army army : BookTownControl.Armys) {
                    if(army.IsCountryConnected(town)) {
                        for(Army.ArmyPlayer ap : army.getPlayers()) {
                            if(Objects.equals(army.GetLink(ap), town.getUUID())) {
                                Warriors++;
                            }
                        }

                    }
                }
                DecimalFormat decimalFormat = new DecimalFormat("#");
                populationInfo = new TextComponent("Численность армии: " + Warriors + " \\ " + decimalFormat.format(Math.ceil(town.getResidents().size() / 3.0)) + "\n");
                page1.addExtra(populationInfo);
                TextComponent leaderInfo = new TextComponent("Лидер: " + town.getMayor().getPlayer().getDisplayName());
                page1.addExtra(leaderInfo);
                pages.add(new BaseComponent[]{page1});

                TextComponent page2 = new TextComponent("Горожане:\n");
                int counter = 1;
                for (Resident r : town.getResidents()) {
                    TextComponent playerName = new TextComponent("["+counter+"] ");
                    playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Objects.requireNonNull(r.getPlayer()).getName()+"\nУдалить игрока").color(ChatColor.RED.asBungee()).create()));
                    page2.addExtra(playerName);
                    counter++;
                }
                page2.setText(page2.getText().substring(0, page2.getText().length() - 2));
                page2.addExtra(".");
                pages.add(new BaseComponent[]{page2});
                TextComponent page3 = new TextComponent(ChatColor.RED + "У вас нет доступа к странице редактирования страны\n");
                pages.add(new BaseComponent[]{page3});
                return pages.toArray(new BaseComponent[0][]);
            }
        } else {
            List<BaseComponent[]> pages = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                pages.add(new BaseComponent[]{
                        new TextComponent(ChatColor.RED + "Информация в книге устарела")
                });
            }
            // Добавьте сюда остальные страницы книги, если нужно

            return pages.toArray(new BaseComponent[0][]);
        }



    }
    void GiveItemToFounder(Player founder, Town town, boolean comeback) {
        ItemStack signedBook = new ItemStack(Material.WRITTEN_BOOK);
        //BookMeta Mbm = (BookMeta) item.getItemMeta();
        BookMeta bookMeta = (BookMeta) signedBook.getItemMeta();
        assert bookMeta != null;
        bookMeta.setTitle("You can't see this");
        bookMeta.setAuthor(founder.getName());
        bookMeta.setDisplayName(main.genGr("Книга страны ","#4AFBFB","#2270F0")+main.genGr(town.getName(),"#BC0000","#FB7000") );
        bookMeta.spigot().setPages(Lists(town,2));
        signedBook.setItemMeta(bookMeta);
        setBookLore(signedBook,town,true); // LORE
        if(!comeback){
            if(founder.getInventory().firstEmpty() != -1) {
                founder.getInventory().addItem(signedBook);
                founder.sendTitle(ChatColor.GREEN + "Страна " + town.getName() + " создана", "", 10, 70, 20);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_BEACON_ACTIVATE, 50f, 0.7f);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 25f, 1.4f);
            }
            else {
                Location playerLocation = founder.getLocation();
                World world = playerLocation.getWorld();
                assert world != null;
                world.dropItem(playerLocation, signedBook);
                founder.sendTitle(ChatColor.GREEN + "Страна " + town.getName() + " создана", ChatColor.RED+"У ВАС НЕТ МЕСТА В ИНВЕНТАРЕ", 10, 70, 20);
                founder.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Предмет выброшен рядом с вами"));
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_BEACON_ACTIVATE, 50f, 0.4f);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 50f, 0.6f);
            }
        }
        else {
            if(founder.getInventory().firstEmpty() != -1) {
                founder.getInventory().addItem(signedBook);
                founder.sendTitle(ChatColor.GREEN + "Книга страны " + town.getName() + " возвращена", "", 10, 70, 20);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_BELL_RESONATE, 50f, 1.2f);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.ITEM_TRIDENT_THUNDER, 5f, 0.8f);
            }
            else {
                Location playerLocation = founder.getLocation();
                World world = playerLocation.getWorld();
                assert world != null;
                world.dropItem(playerLocation, signedBook);
                founder.sendTitle(ChatColor.GREEN + "Книга страны " + town.getName() + " возвращена", ChatColor.RED+"У ВАС НЕТ МЕСТА В ИНВЕНТАРЕ", 10, 70, 20);
                founder.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Предмет выброшен рядом с вами"));
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_BELL_RESONATE, 50f, 1.2f);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.ITEM_TRIDENT_THUNDER, 5f, 0.8f);
                founder.playSound(founder.getLocation().add(0,50,0), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100f, 0.6f);
            }
        }


    }

    public static Town newTown(TownyWorld world, String name, Resident resident, Coord key, Location spawn, Player player) throws TownyException { //NetherChuncks
        TownyUniverse.getInstance().newTown(name);
        Town town = TownyUniverse.getInstance().getTown(name);
        if (town == null) {
            throw new TownyException(String.format("Error fetching new town from name '%s'", name));
        } else {
            TownBlock townBlock = new TownBlock(key.getX(), key.getZ(), world);
            townBlock.setTown(town);
            TownPreClaimEvent preClaimEvent = new TownPreClaimEvent(town, townBlock, player, false, true);
            preClaimEvent.setCancelMessage(Translation.of("msg_claim_error", new Object[]{1, 1}));
            if (BukkitTools.isEventCancelled(preClaimEvent)) {
                TownyUniverse.getInstance().removeTownBlock(townBlock);
                TownyUniverse.getInstance().unregisterTown(town);
                town = null;
                townBlock = null;
                throw new TownyException(preClaimEvent.getCancelMessage());
            } else {
                town.setRegistered(System.currentTimeMillis());
                town.setMapColorHexCode(MapUtil.generateRandomTownColourAsHexCode());
                resident.setTown(town);
                town.setMayor(resident, false);
                town.setFounder(resident.getName());
                townBlock.setType(townBlock.getType());
                town.setSpawn(spawn);
                if (resident.isNPC()) {
                    town.setHasUpkeep(false);
                }

                if (world.isUsingPlotManagementRevert()) {
                    PlotBlockData plotChunk = TownyRegenAPI.getPlotChunk(townBlock);
                    if (plotChunk != null && TownyRegenAPI.getRegenQueueList().contains(townBlock.getWorldCoord())) {
                        TownyRegenAPI.removeFromActiveRegeneration(plotChunk);
                        TownyRegenAPI.removeFromRegenQueueList(townBlock.getWorldCoord());
                        TownyRegenAPI.addPlotChunkSnapshot(plotChunk);
                    } else {
                        TownyRegenAPI.handleNewSnapshot(townBlock);
                    }
                }

                if (TownyEconomyHandler.isActive()) {
                    TownyMessaging.sendDebugMsg("Creating new Town account: " + TownySettings.getTownAccountPrefix() + name);

                    try {
                        town.getAccount().setBalance(0.0, "Setting 0 balance for Town");
                    } catch (NullPointerException var10) {
                        throw new TownyException("The server economy plugin " + TownyEconomyHandler.getVersion() + " could not return the Town account!");
                    }
                }

                if (TownySettings.isTownTagSetAutomatically()) {
                    town.setTag(name.substring(0, Math.min(name.length(), TownySettings.getMaxTagLength())).replace("_", "").replace("-", ""));
                }

                BookTownControl.townAddition.put(town.getUUID(),new TownAddition());
                resident.save();
                townBlock.save();
                town.save();
                world.save();
                Towny.getPlugin().updateCache(townBlock.getWorldCoord());
                BukkitTools.fireEvent(new NewTownEvent(town));
                BookTownControl.townAddition.get(town.getUUID()).addChunckPriorityMap(new ChunkCoord(key.getX(),key.getZ(),world.getBukkitWorld().getName()),1);
                int playerChunkX = player.getLocation().getBlockX() >> 4;
                int playerChunkZ = player.getLocation().getBlockZ() >> 4;
                List<WorldCoord> townBlocks = new ArrayList<>();
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    for (int zOffset = -1; zOffset <= 1; zOffset++) {
                        int chunkX = playerChunkX + xOffset;
                        int chunkZ = playerChunkZ + zOffset;
                        WorldCoord townBlock2 = new WorldCoord("nether",chunkX, chunkZ);
                        townBlocks.add(townBlock2);
                    }
                }
// Запускаем асинхронную задачу TownClaim для заприватки чанков
                Towny.getPlugin().getScheduler().runAsync(new TownClaim(Towny.getPlugin(), player, town, townBlocks, false, true, false));
                return town;
            }
        }
    }

    public static void setBookLore(ItemStack book, Town town, boolean isMayor) {
        boolean exists = false;
        for(Town t : TownyUniverse.getInstance().getTowns()) {
            if(t.equals(town)) {
                exists = true;
                break;
            }
        }
        if(!exists) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED+"Страны больше не существует");
            ItemMeta bookMeta = book.getItemMeta();
            bookMeta.setLore(lore);
            book.setItemMeta(bookMeta);
            return;
        }
        if(isMayor) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY+"Чтобы отобразить актуальную информацию");
            lore.add(ChatColor.GRAY+"Перемести книгу во вторую руку (F), либо открой её");
            lore.add(ChatColor.GRAY+"");
            lore.add(ChatColor.WHITE+"Нераспределено: "+(BookTownControl.GetRemainsDefaultChunks(town)+BookTownControl.GetRemainsBasedChunks(town))+" чанков");
            lore.add(ChatColor.GREEN+"Из них "+BookTownControl.GetRemainsDefaultChunks(town)+" базовых");
            lore.add(ChatColor.YELLOW+"И "+BookTownControl.GetRemainsBasedChunks(town)+" обычных");
            ItemMeta bookMeta = book.getItemMeta();
            bookMeta.setLore(lore);
            book.setItemMeta(bookMeta);
        }
        else {
            ItemMeta bookMeta = book.getItemMeta();
            bookMeta.setLore(null);
            book.setItemMeta(bookMeta);
        }
    }

    public void invitePlayers(Player sender, List<String> playerList) {
        System.out.println(playerList.isEmpty());
        if(playerList.get(0).equalsIgnoreCase("")) {
            return;
        }
        int invitationsSent = 0;
        int invalidNames = 0;

        for (String playerName : playerList) {
            // Проверяем, что игрок с таким именем существует
            Player invitedPlayer = Bukkit.getPlayerExact(playerName);
            if (invitedPlayer == null) {
                invalidNames++;
                continue;
            }

            // Отправляем приглашение
            sender.performCommand("town invite " + sender.getName());
            invitationsSent++;
        }

        // Выводим сообщение о результатах
        sender.sendMessage("Все приглашения отправлены!");
        if (invalidNames > 0) {
            sender.sendMessage("Не удалось отправить приглашения " + invalidNames + " игрокам.");
        }
        sender.sendMessage("Всего отправлено приглашений: " + invitationsSent);
    }


    public static boolean isChunkOccupied(WorldCoord fonder, World world, Player player) {
        int x = fonder.getX();
        int y = fonder.getZ();
        WorldCoord coordsCheck = new WorldCoord(world, x - 10, y - 10);
        WorldCoord startCoord = new WorldCoord(world, x, y);
        System.out.println(startCoord.isWilderness());
        if(!startCoord.isWilderness()) {
            player.sendMessage(ChatColor.RED+"Чанк занят");
            return true;
        }
        for (int i = 0; i <= 20; i++) {
            for (int j = 0; j <= 20; j++) {
                if (!coordsCheck.isWilderness()) {
                    if(startCoord.isWilderness()) {
                        player.sendMessage(ChatColor.RED+"Чанк находится вблизи другого государства");
                    }
                    else {
                        player.sendMessage(ChatColor.RED+"Чанк занят");
                    }
                    return true;
                }
                coordsCheck = new WorldCoord(world, (x - 10) + i, (y - 10) + j);
            }
        }
        return false;
    }

    public void removeBookFromInventory(Player player) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Проверяем предмет в главной руке
        if (mainHandItem.getType() == Material.WRITABLE_BOOK && mainHandItem.hasItemMeta()) {
            BookMeta bookMeta = (BookMeta) mainHandItem.getItemMeta();

            if (bookMeta.hasDisplayName() && bookMeta.getDisplayName().toLowerCase().contains(main.genGr("Книга создания страны","#FF0E6C","#C90AFF").toLowerCase())) {
                player.getInventory().setItemInMainHand(null);
                player.updateInventory();
            }
        }

        // Если книга не найдена в главной руке, проверяем предмет во второстепенной руке
        if (offHandItem.getType() == Material.WRITABLE_BOOK && offHandItem.hasItemMeta()) {
            BookMeta bookMeta = (BookMeta) offHandItem.getItemMeta();
            if (bookMeta.hasDisplayName() && bookMeta.getDisplayName().toLowerCase().contains(main.genGr("Книга создания страны","#FF0E6C","#C90AFF").toLowerCase())) {
                player.getInventory().setItemInOffHand(null);
                player.updateInventory();
            }
        }

    }
}
