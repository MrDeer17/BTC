package gr.btc;

import com.palmergames.adventure.platform.facet.Facet;
import com.palmergames.bukkit.towny.*;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import com.palmergames.util.StringMgmt;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.util.*;
import java.util.function.Consumer;

public class cmds implements CommandExecutor {
    private HashMap<UUID, Long> commandCooldowns = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerUUID = player.getUniqueId();

            // Проверяем, есть ли запись о времени выполнения последней команды для игрока
            if (commandCooldowns.containsKey(playerUUID)) {
                long lastExecutionTime = commandCooldowns.get(playerUUID);
                long currentTime = System.currentTimeMillis();
                long cooldownTime = 3000; // Задержка в 3 секунды (в миллисекундах)

                // Проверяем, прошло ли достаточно времени с момента последнего выполнения команды
                if (currentTime - lastExecutionTime < cooldownTime) {
                    long remainingTime = cooldownTime - (currentTime - lastExecutionTime);
                    String message = String.format("Пожалуйста, подождите %.1f секунд, прежде чем отправить команду.", remainingTime / 1000.0);
                    player.sendMessage(message);
                    return true;
                }
            }
        } //Задержка 3 секунды
        if (command.getName().equalsIgnoreCase("country")) {
            Player player = (Player) sender;
            Town town = null;
            if(args.length == 1) {
                if(args[0].equalsIgnoreCase("armySelect")) {
                    CountryInventoryManager.showArmiesMenu(player,1);
                }
                else if(args[0].equalsIgnoreCase("open")) {
                    Bukkit.dispatchCommand(player, "town toggle open");
                    Bukkit.dispatchCommand(player, "town set perm off");
                }
                else if (args[0].equalsIgnoreCase("war")) {
                    for (Town t : TownyAPI.getInstance().getTowns()) {
                        if (t.getMayor().getPlayer().equals(player)) {
                            town = t;
                            break;
                        }
                    }
                    if(BookTownControl.CheckForWar(town)) {
                        CountryInventoryManager.showLocalWarsMenu(player, BookTownControl.CheckForWarInTown(town));
                    }
                    else {
                        CountryInventoryManager.showGlobalCountriesMenu(player,1);
                    }

                    return true;
                }
            }
            else if (args.length == 2) {
                if(args[0].equalsIgnoreCase("remove")) {
                    boolean isstate = false;
                    for(Town t : TownyAPI.getInstance().getTowns()) {
                        if(t.getName().equals(args[1])) {
                            isstate = true;
                        }
                    }
                    if(!isstate) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Такой страны не существует"));
                        return true;
                    }
                    else {
                        sender.sendMessage("Используйте управление из книги, не пишите команды вручную");
                        return false;
                    }
                }
                else if(args[0].equalsIgnoreCase("unconnect")) {
                    for (Town t : TownyAPI.getInstance().getTowns()) {
                        if (t.getMayor().getPlayer().equals(player)) {
                            town = t;
                            break;
                        }
                    }
                    if(town != null) {
                        Army army = BookTownControl.getArmyByName(args[1]);
                        if(army != null) {
                            army.UnConnectCountry(town);
                            player.sendMessage("Армия "+args[1]+" теперь не защищает интересы страны "+town.getName());
                            player.closeInventory();
                        }
                        else {
                            player.sendMessage("Армия не найдена");
                        }
                    }

                    else {
                        player.sendMessage("Используйте управление из книги");
                    }
                }
                else if(args[0].equalsIgnoreCase("join")) {
                    for (Town t : TownyAPI.getInstance().getTowns()) {
                        if (t.getMayor().getPlayer().equals(player)) {
                            town = t;
                            break;
                        }
                    }
                    Army army = BookTownControl.getArmyByName(args[1]);
                    if (town != null && army != null) {
                        if(BookTownControl.townAddition.get(town.getUUID()).isContractBook()) {
                            BookTownControl.townAddition.get(town.getUUID()).inviteArmy(army);
                            Resident mayor = town.getMayor();
                            TextComponent acceptButton = new TextComponent(net.md_5.bungee.api.ChatColor.GREEN+"[Принять]");
                            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country connect "+army.getName()+" "+town.getName()));

                            // Отправить сообщение мэру с кнопкой "Принять"
                            mayor.getPlayer().sendMessage("Вы хотите, чтобы ваша армия присоединилась к стране " + town.getName());
                            mayor.getPlayer().spigot().sendMessage(acceptButton);
                            player.sendMessage("Предложение о присоединении армии отправлено командиру армии " + args[1]);
                        }
                        else {
                            player.sendMessage("Страна ещё не может подписывать контракты");
                        }
                    } else {
                        player.sendMessage("Армия не найдена");
                    }
                }
                else if(args[0].equalsIgnoreCase("coowner")) {

                    for (Town t : TownyAPI.getInstance().getTowns()) {
                        if (t.getMayor().getPlayer().equals(player)) {
                            town = t;
                            break;
                        }
                    }
                    if(town != null) {
                        if(args[1].equalsIgnoreCase("noone")) {
                            BookTownControl.townAddition.get(town.getUUID()).setco(null);
                            player.sendMessage("Теперь никто не совладелец страны");
                            return true;
                        }
                        BookTownControl.townAddition.get(town.getUUID()).setco(Bukkit.getPlayer(args[1]).getUniqueId());
                        player.sendMessage("Теперь "+args[1]+" совладелец страны");
                    }
                    else {
                        player.sendMessage("Проблема с получением страны");
                    }
                }
                else if(args[0].equalsIgnoreCase("playerjoin")) {
                    town = TownyUniverse.getInstance().getTown(args[1]);
                    if(town != null) {
                        if(town.getMayor().isOnline()) {
                            TextComponent acceptButton = new TextComponent(net.md_5.bungee.api.ChatColor.GREEN + "[Принять]");
                            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/town invite " + player.getName()));
                            town.getMayor().getPlayer().sendMessage("Вы хотите, чтобы игрок " + player.getName() + " был в вашей стране");
                            town.getMayor().getPlayer().spigot().sendMessage(acceptButton);
                            player.sendMessage("Запрос на вступление в город отправлен");
                        }
                        else {
                            player.sendMessage("Мэр сейчас не в сети");
                        }
                    }
                    else {
                        player.sendMessage("Проблема с получением города");
                    }
                }
                else if (args[0].equalsIgnoreCase("war")) {

                    for (Town t : TownyAPI.getInstance().getTowns()) {
                        if (t.getMayor().getUUID().equals(player.getUniqueId())) {
                            town = t;
                            break;
                        }
                    }
                    if(BookTownControl.CheckForWar(town)) {
                        CountryInventoryManager.showLocalWarsMenu(player,BookTownControl.CheckForWarInTown(town));
                    }
                    else {
                        Town town2 = TownyUniverse.getInstance().getTown(args[1]);
                        if (town != null && town2 != null) {
                            War war = BookTownControl.FireNewWar(town, town2);
                        } else {
                            player.sendMessage("Неверная страна");
                        }
                    }
                }
            }
            else if (args.length == 3) {
                if(args[2].equals("true")) {
                    boolean isdeleted = false;
                    UUID townU = TownyUniverse.getInstance().getTown(args[1]).getUUID();
                    try {
                        isdeleted = townDelete(player, args[1]);
                    } catch (TownyException e) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Во время удаления страны произошла ошибка"));
                        System.out.println(e.getMessage());
                    }
                    if(isdeleted) {
                        BookTownControl.townAddition.remove(townU);
                        BookTownControl.saveTownAdditionMap();
                        player.sendTitle(main.genGr("Страна "+args[1]+" удалена","#d95555","#dd7b7b"), ChatColor.RED+"", 10, 70, 20);
                        player.playSound(player.getLocation().add(0,50,0), Sound.BLOCK_BEACON_DEACTIVATE, 50f, 0.6f);
                        player.playSound(player.getLocation().add(0,50,0), Sound.ITEM_TRIDENT_THUNDER, 5f, 0.2f);
                        player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.05f, 0.6f);
                    }

                    //sender.sendMessage();
                    return true;
                }
                else if (args[0].equalsIgnoreCase("connect")) {
                    Army army = BookTownControl.getArmyByName(args[1]);
                    String townName = args[2];
                    town = TownyUniverse.getInstance().getTown(townName);

                    if (town != null && army != null) {
                        if(!isMayorCheck(player,town)) return false;

                        if (BookTownControl.townAddition.get(town.getUUID()).isArmyInvited(army)) {
                            army.ConnectCountry(town);
                            player.sendMessage("Теперь армия "+army.getName()+" охраняет вашу страну "+town.getName());
                        }
                        else {
                            player.sendMessage("Приглашение истекло");
                        }
                    } else {
                        player.sendMessage("Неверное название города или армия");
                    }
                }
                else {
                    TextComponent confirmComponent = new TextComponent("Вы уверены, что хотите удалить страну? ");
                    // Создание кнопки подтверждения
                    TextComponent confirmButton = new TextComponent("[Подтвердить]");
                    confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country remove "+args[1]+" true"));
                    confirmComponent.addExtra(confirmButton);
                    player.spigot().sendMessage(confirmComponent);
                }

            }
        }
        else if (command.getName().equalsIgnoreCase("amendlaws")) {
            Player player = (Player) sender;
            Town town = new Town("");
            boolean isstate = false;
            if(args.length == 1) {
                for(Town t : TownyAPI.getInstance().getTowns()) {
                    if(t.getName().equals(args[0])) {
                        town = t;
                        isstate = true;
                        break;
                    }
                }
                if(!isstate) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Такой страны не существует"));
                    return true;
                }
                if((BookTownControl.townAddition.get(town.getUUID()).getTownItemMap() != null && !BookTownControl.townAddition.get(town.getUUID()).getTownItemMap().isEmpty()) && isMayorCheck(player,town)) { //Содержит книгу
                    if(player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {
                        ItemStack newBook = new ItemStack(Material.WRITABLE_BOOK);
                        BookMeta bm = (BookMeta) newBook.getItemMeta();
                        for(String str : BookTownControl.townAddition.get(town.getUUID()).getTownItemMap()) {
                            bm.addPage(str);
                        }
                        bm.setDisplayName(main.genGr("Законы","#eb1a1a","#cc0aaa"));
                        newBook.setItemMeta(bm);
                        player.getInventory().setItemInOffHand(newBook);
                        BookTownControl.townAddition.get(town.getUUID()).setTownItemMap(null);
                        player.playSound(player.getLocation().add(0,50,0), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 50f, 0.9f);
                        BookTownControl.saveTownAdditionMap();
                    }
                    else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Освободите вторую руку"));
                        player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
                    }

                }
                else if((BookTownControl.townAddition.get(town.getUUID()).getTownItemMap() == null || BookTownControl.townAddition.get(town.getUUID()).getTownItemMap().isEmpty()) && isMayorCheck(player,town)) { //Не содержит книгу
                    if((player.getInventory().getItemInOffHand().getType().equals(Material.WRITABLE_BOOK) || player.getInventory().getItemInOffHand().getType().equals(Material.WRITTEN_BOOK)) && player.getInventory().getItemInOffHand().getItemMeta().hasDisplayName() && player.getInventory().getItemInOffHand().getItemMeta().getDisplayName().equalsIgnoreCase(main.genGr("Законы","#eb1a1a","#eb5a5a"))) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.GREEN+"Книга установлена"));
                        BookMeta bmbm = (BookMeta) player.getInventory().getItemInOffHand().getItemMeta();
                        BookTownControl.townAddition.get(town.getUUID()).setTownItemMap(bmbm.getPages());
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                        player.playSound(player.getLocation().add(0,50,0), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 50f, 0.9f);
                        BookTownControl.saveTownAdditionMap();
                        return true;
                    }
                    else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Книга не установлена"));
                        player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
                    }

                }
                else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"У вас нет прав на это действие, используйте /laws"));
                    player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
                }
            }
            if (args.length == 2) {
                if (args[1].contains("contractbook")) {
                    for (Town t : TownyAPI.getInstance().getTowns()) {
                        if (t.getName().equals(args[0])) {
                            town = t;
                            break;
                        }
                    }
                    if(town.equals(new Town(""))) {
                        return false;
                    }
                    if (BookTownControl.townAddition.get(town.getUUID()).isContractBook()) {
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta bookMeta = (BookMeta) book.getItemMeta();

                        bookMeta.setTitle("Армии страны");
                        bookMeta.setAuthor("null");

                        // Создаем список для хранения страниц
                        List<TextComponent> pages = new ArrayList<>();

                        // Добавление информации об армиях и охраняющих игроках
                        int currentPage = 0;
                        for (int i = 0; i < BookTownControl.Armys.size(); i++) {
                            Army army = BookTownControl.Armys.get(i);
                            if (army.IsCountryConnected(town)) {
                                String armyName = army.getName();
                                List<String> players = new ArrayList<>();

                                for (Army.ArmyPlayer ap : army.getPlayers()) {
                                    if (army.GetLink(ap) != null && army.GetLink(ap).equals(town.getUUID())) {
                                        players.add(ap.getPlayer().getName());
                                    }
                                }

                                StringBuilder pageContentBuilder = new StringBuilder(armyName);
                                if (!players.isEmpty()) {
                                    String finall = "Игроки охраняющие страну: ";
                                    for (String playerss : players) {
                                        finall += "\n" + playerss;
                                    }

                                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(finall).create());
                                    TextComponent pageComponent = new TextComponent(pageContentBuilder.toString());
                                    pageComponent.setHoverEvent(hoverEvent);
                                    ClickEvent clcEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "country unconnect "+armyName);
                                    HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите чтобы удалить армию с поста страны").create());
                                    TextComponent removeArmy = new TextComponent(ChatColor.RED+" [-]");
                                    removeArmy.setClickEvent(clcEvent);
                                    removeArmy.setHoverEvent(hoverEvent2);
                                    pageComponent.addExtra(removeArmy);

                                    if (i % 4 == 0) {
                                        currentPage++;
                                    }
                                    if (currentPage >= pages.size()) {
                                        pages.add(pageComponent);
                                    } else {
                                        pages.set(currentPage, new TextComponent(pages.get(currentPage) + "\n" + pageComponent));
                                    }
                                } else {
                                    String finall = "Пока что никто из этой армии\nне охраняет вашу страну.";
                                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(finall).create());
                                    TextComponent pageComponent = new TextComponent(String.valueOf(pageContentBuilder));
                                    pageComponent.setHoverEvent(hoverEvent);
                                    pageComponent.setHoverEvent(hoverEvent);
                                    ClickEvent clcEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country unconnect "+armyName);
                                    HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите чтобы удалить армию с поста страны").create());
                                    TextComponent removeArmy = new TextComponent(ChatColor.RED+" [-]");
                                    removeArmy.setClickEvent(clcEvent);
                                    removeArmy.setHoverEvent(hoverEvent2);
                                    pageComponent.addExtra(removeArmy);

                                    if (i % 4 == 0) {
                                        currentPage++;
                                    }
                                    if (currentPage >= pages.size()) {
                                        pages.add(pageComponent);
                                    } else {
                                        pages.set(currentPage, new TextComponent(pages.get(currentPage).getText() + "\n" + pageComponent.getText()));
                                    }
                                }
                            }
                        }
                        BaseComponent[] combinedPages = new TextComponent[pages.size()];
                        for (int i = 0; i < pages.size(); i++) {
                            combinedPages[i] = pages.get(i);
                        }

                        bookMeta.spigot().setPages(combinedPages);
                        ClickEvent clickEvent1 = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/country armySelect");
                        ClickEvent clickEvent2 = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openbookinmainhand");
                        HoverEvent hoverEvent1 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Предложить армии охранять вас").create());
                        HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Перейти обратно в книгу управления").create());

                        TextComponent textComponent1 = new TextComponent(ChatColor.GREEN + "    [+]   ");
                        TextComponent textComponent2 = new TextComponent(ChatColor.RED + " [⭯]");
                        textComponent1.setClickEvent(clickEvent1);
                        textComponent2.setClickEvent(clickEvent2);
                        textComponent1.setHoverEvent(hoverEvent1);
                        textComponent2.setHoverEvent(hoverEvent2);

                        BaseComponent lastPage;
                        if (combinedPages.length == 0) {
                            lastPage = new TextComponent(ChatColor.GRAY + "Пока что никакие армии вас не охраняют\n");
                        } else {
                            lastPage = combinedPages[combinedPages.length - 1];
                        }
                        lastPage.addExtra("\n\n");
                        lastPage.addExtra(textComponent1);
                        lastPage.addExtra(textComponent2);
                        //System.out.println("lastPage: " + lastPage.toPlainText());
                        int lastPageIndex = bookMeta.getPageCount();
                        bookMeta.spigot().setPage(lastPageIndex, lastPage);
                        book.setItemMeta(bookMeta);
                        player.openBook(book);
                    }
                    else {
                        ItemStack offHandItem = player.getInventory().getItemInOffHand();
                        if (offHandItem != null && (offHandItem.getType().equals(Material.WRITTEN_BOOK) || offHandItem.getType().equals(Material.WRITABLE_BOOK))) {
                            ItemMeta bookMeta = offHandItem.getItemMeta();
                            if (bookMeta.hasDisplayName() && ChatColor.stripColor(bookMeta.getDisplayName()).contains("Контракты")) {
                                player.getInventory().setItemInOffHand(null);
                                player.sendMessage(ChatColor.GREEN + "Теперь страна может заключать контракты с армиями");
                                BookTownControl.townAddition.get(town.getUUID()).setContractBook(true);
                                return true;
                            }
                        }
                        player.sendMessage(ChatColor.RED + "Книга \"Контракты\" не найдена");
                        return false;
                    }
                }
            }

        }
        else if (command.getName().equalsIgnoreCase("laws")) {
            Player player = (Player) sender;
            int chunkX = player.getLocation().getChunk().getX();
            int chunkZ = player.getLocation().getChunk().getZ();
            WorldCoord coordsCheck = new WorldCoord(player.getWorld(), chunkX,chunkZ);
            if(!(coordsCheck.isWilderness())) {
                if(!(BookTownControl.townAddition.get(coordsCheck.getTownOrNull().getUUID()).getTownItemMap().isEmpty())) {
                    ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta bm = (BookMeta) newBook.getItemMeta();
                    assert bm != null;
                    bm.setTitle("op");
                    bm.setAuthor("op");
                    for(String str : BookTownControl.townAddition.get(coordsCheck.getTownOrNull().getUUID()).getTownItemMap()) {
                        bm.addPage(str);
                    }
                    newBook.setItemMeta(bm);
                    player.openBook(newBook);
                }
                else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Законы в этой стране редактируются"));
                }

            }
            else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"В пустоши нет законов"));
            }
        }
        else if (command.getName().equalsIgnoreCase("tinvite")) {
            Player player = (Player) sender;
            ChatListener chatListener = new ChatListener(player);

            // Блокировка чата
            player.sendMessage(ChatColor.RED + "Введите ник игрока для приглашения:");

            // Регистрация слушателя чата
            Bukkit.getPluginManager().registerEvents(chatListener, BookTownControl.getPlugin(BookTownControl.class));

            // Отправка сообщения о возможности отмены
            player.sendMessage(ChatColor.RED + "Введите 'cancel' для отмены.");

            CommandListener commandListener = new CommandListener(player);
            Bukkit.getPluginManager().registerEvents(commandListener, BookTownControl.getPlugin(BookTownControl.class));
            commandListener.setBlockCommands(true);
            commandListener.setOnCommandExecuted((commandss) -> {
                player.sendMessage(ChatColor.RED + "Вы не можете использовать команды во время этой операции.");
            });
            // Ожидание ввода ника игрока
            chatListener.setChatEnabled(true);

            // Обработка введенного сообщения
            chatListener.setOnChatMessageReceived((message) -> {
                if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("c") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("no") || message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("н") || message.equalsIgnoreCase("нет") || message.equalsIgnoreCase("о")) {
                    // Отмена
                    //
                    player.sendMessage(ChatColor.RED + "Приглашение отменено.");
                    // Разблокировка чата
                    chatListener.setChatEnabled(false);
                    HandlerList.unregisterAll(chatListener);
                }
                else {
                    // Проверка ника игрока
                    if (Bukkit.getPlayerExact(message) == null) {
                        player.sendMessage(ChatColor.RED + "Неверный никнейм.");
                    }
                    else {
                        Bukkit.getScheduler().runTaskAsynchronously(BookTownControl.getPlugin(BookTownControl.class), () -> {
                            // Ваш код выполнения команды
                            player.performCommand("town invite "+Bukkit.getPlayerExact(message).getName());
                        });

                        //player.sendMessage(ChatColor.GREEN + "Приглашение игроку "+Bukkit.getPlayerExact(message).getName()+" отправлено ");
                        player.performCommand("town invite "+Bukkit.getPlayerExact(message).getName());
                        // Разблокировка чата

                        commandListener.setBlockCommands(false);
                        HandlerList.unregisterAll(commandListener);
                        chatListener.setChatEnabled(false);
                        HandlerList.unregisterAll(chatListener);
                    }
                }
            });
        }
        else if (command.getName().equalsIgnoreCase("menu")) {
            InventoryManager.openInventoryMAIN((Player) sender);
        }
        else if (command.getName().equalsIgnoreCase("army")) {
            if (args.length == 0) {
                InventoryManager.openInventoryArmyCrOrJoin((Player) sender);
            }
            else if(args.length == 1) {
                if (args[0].equalsIgnoreCase("connect")) {
                    InventoryManager.showGlobalCountriesMenu((Player) sender,1);
                }
            }
            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("disband")) {
                    Player player = (Player) sender;
                    if (args[1].equalsIgnoreCase("false")) {
                        // Вывести сообщение с подтверждением в чате
                        TextComponent confirmComponent = new TextComponent("Вы уверены, что хотите распустить армию? ");
                        // Создание кнопки подтверждения
                        TextComponent confirmButton = new TextComponent("[Подтвердить]");
                        confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/army disband true"));
                        confirmComponent.addExtra(confirmButton);
                        player.spigot().sendMessage(confirmComponent);
                    } else if (args[1].equalsIgnoreCase("true")) {
                        Army army = BookTownControl.GetArmyPlayerIn(player);
                        if (army != null) {
                            if(!priorityCheck(5,army,player)) return false;
                            BookTownControl.Armys.remove(army);
                            player.sendMessage("Армия расформирована");
                        }

                    } else {
                        player.sendMessage("Используйте управление из книги, не пишите команды вручную");
                    }
                }
                else if (args[0].equalsIgnoreCase("invite")) {
                    Player firstyPlayer = (Player) sender;
                    Army army = BookTownControl.GetArmyPlayerIn(firstyPlayer);
                    String playerName = args[1];
                    Player targetPlayer = Bukkit.getPlayer(playerName);
                    if(!priorityCheck(3,army,firstyPlayer)) return false;
                    if (targetPlayer != null) {
                        if(BookTownControl.GetArmyPlayerIn(targetPlayer) != null) {
                            firstyPlayer.sendMessage("Игрок уже состоит в армии");
                            return false;
                        }
                        // Создание сообщения с приглашением
                        army.invitePlayer(targetPlayer);
                        TextComponent inviteMessage = new TextComponent("Вы получили приглашение вступить в армию! ");

                        // Создание кнопки принятия приглашения
                        TextComponent acceptButton = new TextComponent("[Принять]");
                        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/army accept "+army.getName()));
                        acceptButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                        inviteMessage.addExtra(acceptButton);

                        // Создание кнопки отклонения приглашения
                        TextComponent declineButton = new TextComponent(" [Отклонить]");
                        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/army decline "+army.getName()));
                        declineButton.setColor(net.md_5.bungee.api.ChatColor.RED);
                        inviteMessage.addExtra(declineButton);

                        // Отправка приглашения целевому игроку
                        targetPlayer.spigot().sendMessage(inviteMessage);

                        // Отправка подтверждения отправителю
                        sender.sendMessage("Приглашение успешно отправлено игроку " + targetPlayer.getName());
                    }
                    else {
                        sender.sendMessage("Игрок " + playerName + " не найден");
                    }
                }
                else if (args[0].equalsIgnoreCase("accept")) {
                        String armyName = args[1];
                        Army army = BookTownControl.getArmyByName(armyName);

                        if (army != null) {
                            Player player = (Player) sender;
                            if (BookTownControl.GetArmyPlayerIn(player) != null) {
                                player.sendMessage("Вы уже состоите в армии");
                                return false;
                            }
                            if (army.isPlayerInvited(player)) {
                                army.addPlayer(player.getUniqueId(), 1);
                                sender.sendMessage("Вы успешно вступили в армию " + army.getName());
                            }
                            else if(army.isOpen()) {
                                sender.sendMessage("Используйте /army join <название армия>\nПриглашение истекло либо вас не приглашали в армию");
                            }
                            else {
                                sender.sendMessage("Вы не приглашены в армию " + army.getName());
                            }
                        } else {
                            sender.sendMessage("Армия с именем " + armyName + " не найдена");
                        }
                }
                else if (args[0].equalsIgnoreCase("decline")) {
                    Army army = BookTownControl.getArmyByName(args[1]);
                    Player player = (Player) sender;
                    if(army != null) {
                        if(army.isPlayerInvited(player)) {
                            sender.sendMessage("Приглашение отклонено");
                        }
                        else {
                            sender.sendMessage("Нет входящих приглашений");
                        }
                    }
                    else {
                        sender.sendMessage("Нет входящих приглашений");
                    }


                }
                else if(args[0].equalsIgnoreCase("join")) {
                    Army army = BookTownControl.getArmyByName(args[1]);
                    Player player = (Player) sender;
                    if(army != null) {
                        if (BookTownControl.GetArmyPlayerIn(player) != null) {
                            player.sendMessage("Вы уже состоите в армии");
                            return false;
                        }
                        if (army.isOpen()) {
                            army.addPlayer(player.getUniqueId(), 1);
                            sender.sendMessage("Вы успешно вступили в армию " + army.getName());

                        }
                        else {
                            sender.sendMessage("Вступить в эту армию можно только по приглашению");
                        }
                    }
                    else {
                        sender.sendMessage("Армия с таким именем не найдена");
                    }
                    player.closeInventory();
                }
                else if (args[0].equalsIgnoreCase("leave")) {
                        String confirm = args[1];
                        Player player = (Player) sender;
                        Army army = BookTownControl.GetArmyPlayerIn(player);

                        if (army != null) {
                            Army.ArmyPlayer armyPlayer = army.getPlayer(player);

                            if (armyPlayer != null) {
                                int priority = armyPlayer.getPriority();

                                if (priority >= 5) {
                                    sender.sendMessage("Используйте /army disband");
                                }
                                else {
                                    if (confirm.equalsIgnoreCase("true")) {
                                        army.removePlayer(player.getUniqueId());
                                        sender.sendMessage("Вы покинули армию " + army.getName());
                                    } else {
                                        // Вывести сообщение с подтверждением в чате
                                        TextComponent confirmComponent = new TextComponent("Вы уверены, что хотите покинуть армию? ");
                                        // Создание кнопки подтверждения
                                        TextComponent confirmButton = new TextComponent("[Подтвердить]");
                                        confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/army leave true"));
                                        confirmComponent.addExtra(confirmButton);
                                        player.spigot().sendMessage(confirmComponent);
                                    }
                                }
                            } else {
                                sender.sendMessage("Вы не являетесь членом армии");
                            }
                        } else {
                            sender.sendMessage("Вы не являетесь членом армии");
                        }
                }
                else if (args[0].equalsIgnoreCase("connect")) {
                    Player player = (Player) sender;
                    Army army = BookTownControl.GetArmyPlayerIn(player);
                    String townName = args[1];
                    Town town = TownyUniverse.getInstance().getTown(townName);

                    if (town != null && army != null) {
                        if(!priorityCheck(4,army,player)) return false;
                        if(BookTownControl.townAddition.get(town.getUUID()).isContractBook()) {
                            army.inviteCountry(town);
                            Resident mayor = town.getMayor();
                            TextComponent acceptButton = new TextComponent(net.md_5.bungee.api.ChatColor.GREEN+"[Принять]");
                            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/army connect "+army.getName()+" "+town.getName()));

                            // Отправить сообщение мэру с кнопкой "Принять"
                            mayor.getPlayer().sendMessage("Вы хотите, чтобы к вам присоединилась армия " + army.getName());
                            mayor.getPlayer().spigot().sendMessage(acceptButton);
                            player.sendMessage("Предложение о присоединении армии отправлено стране " + town.getName());
                        }
                        else {
                            player.sendMessage("Страна ещё не может подписывать контракты");
                        }
                    } else {
                        player.sendMessage("Страна не найдена");
                    }
                }

                return true;
            }
            else if(args.length == 3) {
                if (args[0].equalsIgnoreCase("connect")) {
                    Player player = (Player) sender;
                    Army army = BookTownControl.getArmyByName(args[1]);
                    String townName = args[2];
                    Town town = TownyUniverse.getInstance().getTown(townName);

                    if (town != null && army != null) {
                        if(!isMayorCheck(player,town)) return false;

                        if (army.isCountryInvited(town)) {
                            army.ConnectCountry(town);
                            player.sendMessage("Теперь армия "+army.getName()+" охраняет вашу страну "+town.getName());
                        }
                        else {
                            player.sendMessage("Приглашение истекло");
                        }
                    } else {
                        player.sendMessage("Неверное название города или армия");
                    }
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("openbookinmainhand")) {
            Player player = (Player) sender;
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType().equals(Material.WRITTEN_BOOK)) {
                player.openBook(itemInMainHand);
            }
        }
        return false;
    }

    public boolean townDelete(Player player, String CN) throws TownyException {
        TownyUniverse townyUniverse = TownyUniverse.getInstance();
        Town town = TownyUniverse.getInstance().getTown(CN);
        if ((!town.hasResident(player) || !townyUniverse.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_COMMAND_TOWN_DELETE.getNode()))) {
            if(player.isOp() || isMayorCheck(player,town)) {
                //TownyMessaging.sendMsg(player, Translatable.of("town_deleted_by_admin", new Object[]{town.getName()}));
                townyUniverse.getDataSource().removeTown(town);
                return true;
            }
            else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Во время удаления страны произошла ошибка"));
                return false;
            }
        } else {
            if (TownySettings.getTownRuinsEnabled()) {
                //TownyMessaging.sendErrorMsg(player, Translatable.of("msg_warning_town_ruined_if_deleted", new Object[]{TownySettings.getTownRuinsMaxDurationHours()}));
                if (TownySettings.getTownRuinsReclaimEnabled()) {
                    //TownyMessaging.sendErrorMsg(player, Translatable.of("msg_warning_town_ruined_if_deleted2", new Object[]{TownySettings.getTownRuinsMinDurationHours()}));
                }
            }
            if(player.isOp() || isMayorCheck(player,town)) {
                //TownyMessaging.sendMsg(player, Translatable.of("town_deleted_by_admin", new Object[]{town.getName()}));
                townyUniverse.getDataSource().removeTown(town);
                return true;
            }
            else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Во время удаления страны произошла ошибка"));
                return false;
            }
        }
    }
    boolean isMayorCheck(Player player, Town town) {
        Resident resik = new Resident(player.getName());
        if(Objects.equals(town.getMayor().getPlayer(), resik.getPlayer())) {

            return true;
        } else {
            return false;
        }


    }

    boolean priorityCheck(int reqPriority, Army army, Player player) {
            int priority = -1;
            Army.ArmyPlayer armyPlayer = army.getPlayer(player);
            if (armyPlayer != null) {
                priority = armyPlayer.getPriority();
            }
            if (priority < reqPriority) {
                return false;
            }
        return true;
    }
}
