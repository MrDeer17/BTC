package gr.btc;

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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

import java.util.Objects;
import java.util.function.Consumer;

public class cmds implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("countryremove")) {
            Player player = (Player) sender;
            boolean isstate = false;
            if(args.length >= 1) {
                for(Town t : TownyAPI.getInstance().getTowns()) {
                    if(t.getName().equals(args[0])) {
                        isstate = true;
                    }
                }
                if(!isstate) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Такой страны не существует"));
                    return true;
                }
            }
            if (args.length == 2 && args[1].equals("true")) {
                boolean isdeleted = false;
                try {
                    isdeleted = townDelete(player, args[0]);
                } catch (TownyException e) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Во время удаления страны произошла ошибка"));
                    System.out.println(e.getMessage());
                }
                if(isdeleted) {
                    Location playerLocation = player.getLocation();
                    World world = playerLocation.getWorld();
                    assert world != null;
                    player.sendTitle(main.genGr("Страна "+args[0]+" удалена","#d95555","#dd7b7b"), ChatColor.RED+"", 10, 70, 20);
                    player.playSound(player.getLocation().add(0,50,0), Sound.BLOCK_BEACON_DEACTIVATE, 50f, 0.6f);
                    player.playSound(player.getLocation().add(0,50,0), Sound.ITEM_TRIDENT_THUNDER, 5f, 0.2f);
                    player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.05f, 0.6f);
                }

                //sender.sendMessage();
                return true;
            }
            else if (args.length == 1) {
                TextComponent confirmComponent = new TextComponent("Вы уверены, что хотите удалить страну? ");
                // Создание кнопки подтверждения
                TextComponent confirmButton = new TextComponent("[Подтвердить]");
                confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/countryremove "+args[0]+" true"));
                confirmComponent.addExtra(confirmButton);
                player.spigot().sendMessage(confirmComponent);
            }
            else {
                sender.sendMessage("Используйте управление из книги, не пишите команды вручную");
                return false;
            }
        }
        else if (command.getName().equalsIgnoreCase("amendlaws")) {
            Player player = (Player) sender;
            Town town = new Town("");
            boolean isstate = false;
            if(args.length >= 1) {
                for(Town t : TownyAPI.getInstance().getTowns()) {
                    if(t.getName().equals(args[0])) {
                        town = t;
                        isstate = true;
                        continue;
                    }
                }
                if(!isstate) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Такой страны не существует"));
                    return true;
                }
                if(BookTownControl.townItemMap.containsKey(town.getUUID()) && isMayorCheck(player,town)) { //Содержит книгу
                    if(player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {
                        ItemStack newBook = new ItemStack(Material.WRITABLE_BOOK);
                        BookMeta bm = (BookMeta) newBook.getItemMeta();
                        for(String str : BookTownControl.townItemMap.get(town.getUUID())) {
                            bm.addPage(str);
                        }
                        bm.setDisplayName(main.genGr("Законы","#eb1a1a","#eb5a5a"));
                        newBook.setItemMeta(bm);
                        player.getInventory().setItemInOffHand(newBook);
                        BookTownControl.townItemMap.remove(town.getUUID());
                        player.playSound(player.getLocation().add(0,50,0), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 50f, 0.9f);
                        BookTownControl.saveTownItemMap();
                    }
                    else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.RED+"Освободите вторую руку"));
                        player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
                    }

                }
                else if(!(BookTownControl.townItemMap.containsKey(town.getUUID())) && isMayorCheck(player,town)) { //Не содержит книгу
                    if((player.getInventory().getItemInOffHand().getType() == Material.WRITABLE_BOOK || player.getInventory().getItemInOffHand().getType() == Material.WRITTEN_BOOK) && player.getInventory().getItemInOffHand().getItemMeta().hasDisplayName() && player.getInventory().getItemInOffHand().getItemMeta().getDisplayName().equalsIgnoreCase(main.genGr("Законы","#eb1a1a","#eb5a5a"))) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(ChatColor.GREEN+"Книга установлена"));
                        BookMeta bmbm = (BookMeta) player.getInventory().getItemInOffHand().getItemMeta();
                        BookTownControl.townItemMap.put(town.getUUID(),bmbm.getPages());
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                        player.playSound(player.getLocation().add(0,50,0), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 50f, 0.9f);
                        BookTownControl.saveTownItemMap();
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

        }
        else if (command.getName().equalsIgnoreCase("laws")) {
            Player player = (Player) sender;
            int chunkX = player.getLocation().getChunk().getX();
            int chunkZ = player.getLocation().getChunk().getZ();
            WorldCoord coordsCheck = new WorldCoord(player.getWorld(), chunkX,chunkZ);
            if(!(coordsCheck.isWilderness())) {
                if(BookTownControl.townItemMap.containsKey(coordsCheck.getTownOrNull().getUUID())) {
                    ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta bm = (BookMeta) newBook.getItemMeta();
                    assert bm != null;
                    bm.setTitle("op");
                    bm.setAuthor("op");
                    for(String str : BookTownControl.townItemMap.get(coordsCheck.getTownOrNull().getUUID())) {
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

            // Ожидание ввода ника игрока
            chatListener.setChatEnabled(true);

            // Обработка введенного сообщения
            chatListener.setOnChatMessageReceived((message) -> {
                if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("c") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("no") || message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("н") || message.equalsIgnoreCase("нет") || message.equalsIgnoreCase("о")) {
                    // Отмена
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

                        player.sendMessage(ChatColor.GREEN + "Приглашение игроку "+Bukkit.getPlayerExact(message).getName()+" отправлено ");
                        // Разблокировка чата
                        chatListener.setChatEnabled(false);
                        HandlerList.unregisterAll(chatListener);
                    }
                }
            });
        }
        else if (command.getName().equalsIgnoreCase("menu")) {
            InventoryManager.openInventoryMAIN((Player) sender);
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

    
}
