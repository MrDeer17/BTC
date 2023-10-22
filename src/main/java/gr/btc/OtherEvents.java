package gr.btc;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OtherEvents implements Listener {
    private final Map<Player, Long> lastEventTimeMap = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(BookTownControl.PlayerFillingBook.containsKey(event.getPlayer().getUniqueId())) {
            BookTownControl.PlayerFillingBook.get(event.getPlayer().getUniqueId()).Start(event.getPlayer());
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (BookTownControl.IsPlayerInWar(player) != null) {
            War war = BookTownControl.CheckForWarInTown(BookTownControl.IsPlayerInWar(player));

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(player.getName());
            List<String> lore = new ArrayList<>();
            String lose = "";
            String win = "";
            if(war.side1Warriors.contains(player)) {
                lose = TownyUniverse.getInstance().getTown(war.sides1.get(0)).getName();
                win = TownyUniverse.getInstance().getTown(war.sides2.get(0)).getName();
            }
            else if(war.side2Warriors.contains(player)) {
                lose = TownyUniverse.getInstance().getTown(war.sides2.get(0)).getName();
                win = TownyUniverse.getInstance().getTown(war.sides1.get(0)).getName();
            }

            lore.add(ChatColor.GREEN + "ПКМ на чанк " + ChatColor.YELLOW + lose + ChatColor.GREEN + " чтобы захватить");
            lore.add(ChatColor.GREEN + "или на чанк своей страны чтобы отбить чанк обратно");
            lore.add("");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd HH:mm");
            Date expirationDate = new Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000); // Текущее время + 12 часов
            String expirationDateString = dateFormat.format(expirationDate);
            lore.add(ChatColor.GRAY + "Годен до: " + expirationDateString);
            lore.add(ChatColor.GRAY + "Только страна " +win+" получит новый чанк");
            meta.setLore(lore);
            head.setItemMeta(meta);
//BookTownControl.
            player.getWorld().dropItem(player.getLocation(), head);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)  {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Coord key = null;
            Block clickedBlock = event.getBlockPlaced();
            if (clickedBlock != null) {
                World world = player.getWorld();
                int x = clickedBlock.getX();
                int z = clickedBlock.getZ();

                key = Coord.parseCoord(x,z);
                // Ваш код для работы с объектом WorldCoord
            }
        else {
            return;
        }
        System.out.println(key);
        System.out.println(new TownyWorld(player.getWorld().getName()));


        if (item != null && item.getType() == Material.PLAYER_HEAD && item.hasItemMeta() && item.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
                List<String> lore = meta.getLore();

                if (lore != null && lore.size() >= 5) {
                    event.setCancelled(true);
                    TownBlock townBlock = null;
                    try {
                        townBlock = TownyUniverse.getInstance().getTownBlock(new WorldCoord(new TownyWorld(player.getWorld().getName()).getName(),key.getX(),key.getZ()));
                    } catch (NotRegisteredException e) {
                        player.sendMessage(ChatColor.RED +"Неверно выбранный чанк");
                    }
                    String descriptionLine = ChatColor.stripColor(lore.get(0)); // Удаляем форматирование из первой строки описания
                    String cityName = descriptionLine.replace("ПКМ на чанк ","").replace(" чтобы захватить","");
                    Town LoseTown = TownyUniverse.getInstance().getTown(cityName);
                    descriptionLine = ChatColor.stripColor(lore.get(4)); // Удаляем форматирование из первой строки описания
                    cityName = descriptionLine.replace("Только страна ","").replace(" получит новый чанк","");
                    Town WinTown = TownyUniverse.getInstance().getTown(cityName);
                    String expirationDateString = lore.get(3).substring(11);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd HH:mm");

                    try {
                        Date expirationDate = dateFormat.parse(expirationDateString);

                        if (expirationDate.before(new Date())) {
                            player.sendMessage(ChatColor.RED + "Срок годности предмета истек!");

                            item.setAmount(0);
                            return;
                        }
                        else {

                            if (townBlock.getTownOrNull() != null
                                    && townBlock.getTownOrNull().equals(LoseTown)
                                    && (BookTownControl.townAddition.get(LoseTown.getUUID()).getChunckPriorityMap().get(new ChunkCoord(townBlock.getWorldCoord().getX(),townBlock.getWorldCoord().getZ(),townBlock.getWorldCoord().getWorldName())) == null
                                    || BookTownControl.townAddition.get(LoseTown.getUUID()).getChunckPriorityMap().get(new ChunkCoord(townBlock.getWorldCoord().getX(),townBlock.getWorldCoord().getZ(),townBlock.getWorldCoord().getWorldName())) <= 0))
                            {
                                townBlock.setTown(WinTown);
                                player.sendMessage(ChatColor.DARK_RED +"Чанк захвачен");
                            }
                            else {
                                player.sendMessage(ChatColor.RED +"Неверно выбранный чанк");
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }


        // Ваш код для работы с объектом Town
    }
    @EventHandler
    public void onPlayerEntersTownBorder(PlayerChangePlotEvent event) {
        Player player = event.getPlayer();
        WorldCoord getto = event.getTo();
        String outMessage = "";

        if(getto.isWilderness()) {
            if(!event.getFrom().isWilderness()) {
                outMessage += ChatColor.RED+"Пустошь";
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(outMessage));
            }
            return;
        }
        else {

            try {
                if(Objects.equals(getto.getTownOrNull().getHomeBlock(), getto.getTownBlockOrNull())) {
                    outMessage += ChatColor.GREEN+getto.getTownOrNull().getName();
                    outMessage += GetIsPriority(getto);

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(outMessage));
                }
                else {
                    outMessage += ChatColor.WHITE+getto.getTownOrNull().getName();
                    outMessage += GetIsPriority(getto);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent(outMessage));
                }

            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }

        if (!(System.currentTimeMillis() - lastEventTimeMap.getOrDefault(player, 0L) >= 120000)) {
            return; // It has not been 2 minutes since the last event, so return and do nothing
        } else {
            lastEventTimeMap.put(player, System.currentTimeMillis()); // Update the last event time to the current time
        }

        if(!(getto.getTownOrNull().equals(event.getFrom().getTownOrNull()))) {
            String welcomeMessage = ChatColor.GREEN+"Добро пожаловать в страну "+getto.getTownOrNull().getName();
            String lawsMessage = ChatColor.YELLOW+"Нажмите сюда чтобы открыть законы этой страны";

            TextComponent message = new TextComponent(welcomeMessage);
            TextComponent lawsLink = new TextComponent(lawsMessage);
            lawsLink.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/laws"));

            message.addExtra("\n");
            message.addExtra(lawsLink);

            event.getPlayer().spigot().sendMessage(message);
        }

        // Ваш код обработки события здесь
    }
    @EventHandler
    public void onTownClaim(TownClaimEvent event) {
        Player player = event.getResident().getPlayer();
        int BASOVIECHANKI = BookTownControl.GetRemainsDefaultChunks(event.getTown());
        final Sound[] randomSound = {null};


        if(BASOVIECHANKI <= 0) { //Открыть не базовый чанк
            if(checkInvalidChunksForLP(player,event)) {
                TownyUniverse.getInstance().removeTownBlock(event.getTownBlock());
                player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
                player.sendMessage(ChatColor.RED+"Некорректное перекрытие чанков, отмена привата\nБазовые чанки не должны ограничивать обычные чанки со всех сторон");
                return;
            }
            else {

            }
            for (int i = 0; i < 10; i++) {
                int randomDelay = 3; // Delay in milliseconds
                Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), () -> {
                    float randomPitch = (float) (0.8 + Math.random() * 0.4); // Random pitch between 0.6 and 1.4
                    int randomSoundIndex = (int) (Math.random() * 5);
                    switch (randomSoundIndex) {
                        case 0:
                            randomSound[0] = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                            break;
                        case 1:
                            randomSound[0] = Sound.UI_STONECUTTER_TAKE_RESULT;
                            break;
                        case 2:
                            randomSound[0] = Sound.ITEM_TRIDENT_THROW;
                            break;
                        case 3:
                            randomSound[0] = Sound.ITEM_SHOVEL_FLATTEN;
                            break;
                        case 4:
                            randomSound[0] = Sound.BLOCK_BAMBOO_WOOD_PLACE;
                            break;
                    }
                    player.playSound(player.getLocation().add(0, 50, 0), randomSound[0], 50f, randomPitch);
                }, randomDelay * i);
            }
        }
        else { //Открыть базовый чанк
            WorldCoord wc = new WorldCoord(event.getTownBlock().getWorldCoord());

            if (!checkInvalidChunks(player, event)) {
                TownyUniverse.getInstance().removeTownBlock(event.getTownBlock());
                player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ALLAY_ITEM_THROWN, 50f, 0.6f);
                player.sendMessage(ChatColor.RED+"Некорректное перекрытие чанков, отмена привата\nБазовые чанки не должны ограничивать обычные чанки со всех сторон");
                return;
            } else {
                BookTownControl.townAddition.get(wc.getTownOrNull().getUUID()).addChunckPriorityMap(new ChunkCoord(wc.getX(),wc.getZ(),wc.getWorldName()) ,1);
            }

            for (int i = 0; i < 20; i++) {
                int randomDelay = 2; // Delay in milliseconds
                Bukkit.getScheduler().runTaskLater(BookTownControl.getPlugin(BookTownControl.class), () -> {
                    int randomSoundIndex = (int) (Math.random() * 5);
                    switch (randomSoundIndex) {
                        case 0:
                            randomSound[0] = Sound.ITEM_SHIELD_BLOCK;
                            break;
                        case 1:
                            randomSound[0] = Sound.BLOCK_CHAIN_FALL;
                            break;
                        case 2:
                            randomSound[0] = Sound.ITEM_SPYGLASS_USE;
                            break;
                        case 3:
                            randomSound[0] = Sound.ITEM_SHOVEL_FLATTEN;
                            break;
                        case 4:
                            randomSound[0] = Sound.BLOCK_BAMBOO_WOOD_PLACE;
                            break;
                    }
                    float randomPitch = (float) (0.8 + Math.random() * 0.4); // Random pitch between 0.6 and 1.4
                    player.playSound(player.getLocation().add(0, 50, 0), randomSound[0], 50f, randomPitch);
                }, randomDelay * i);
            }
        }
    }
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (mainHandItem.getType() == Material.WRITTEN_BOOK && mainHandItem.getItemMeta().hasDisplayName() &&
                mainHandItem.getItemMeta().getDisplayName().toLowerCase().contains(main.genGr("Книга страны ","#4AFBFB","#2270F0").toLowerCase())) {

            BookMeta bm = (BookMeta) mainHandItem.getItemMeta();
            int indexOfNewLine = bm.getPage(1).replace("Страна: ","").indexOf("\n");
            Town town = new Town("");
            for (Town tl : TownyUniverse.getInstance().getTowns()) {
                if (tl.getName().equalsIgnoreCase(bm.getPage(1).replace("Страна: ","").substring(0,indexOfNewLine).replaceAll("§[0-9a-fA-Fklmnor]", ""))) {
                    town = tl;
                    continue;
                }
            }
            if (town != null && Objects.equals(town.getMayor().getPlayer(), player)) {
                OnBookWrite.setBookLore(mainHandItem, town, true);
                event.setCancelled(true);
                player.playSound(player.getLocation().add(0,50,0), Sound.ENTITY_ITEM_PICKUP, 50f, 0.8f);
            } else {
                OnBookWrite.setBookLore(mainHandItem, town, false);
            }

        }

        // Code to handle the swap hand items event

    }
    String GetIsPriority(WorldCoord getto) {
        String outMessage = "";
        Town town = getto.getTownOrNull();
        if(!(BookTownControl.townAddition.containsKey(town.getUUID())) || !BookTownControl.townAddition.get(town.getUUID()).getChunckPriorityMap().containsKey(new ChunkCoord(getto.getX(),getto.getZ(),getto.getWorldName()))) {
            outMessage += " "+ChatColor.DARK_GRAY+"⚔"; //If FightChunck
        }
        else {
            outMessage += " "+ChatColor.GOLD+"\uD83D\uDEE1"; //If SafeChunck
        }
        return outMessage;
    }

    private boolean checkInvalidChunks(Player player, TownClaimEvent event) {
        int x = event.getTownBlock().getX();
        int y = event.getTownBlock().getZ();
        WorldCoord coordsCheck = new WorldCoord(player.getWorld(), x - 2, y - 2);
        WorldCoord coordsCheckRam = null;

        // Создаем двумерный массив chunkPriorityArray размером 5x5
        int[][] chunkPriorityArray = new int[5][5];

        for (int i = 0; i <= 4; i++) {
            for (int j = 0; j <= 4; j++) {
                if (coordsCheck.isWilderness()) {
                    chunkPriorityArray[i][j] = -1; // Если чанк wilderness, приоритет -1
                } else if (BookTownControl.townAddition.get(event.getTown().getUUID()).getChunckPriorityMap().containsKey((new ChunkCoord(coordsCheck.getX(),coordsCheck.getZ(),coordsCheck.getWorldName())))) {
                    chunkPriorityArray[i][j] = 1; // Если чанк есть в ChunckPriorityMap, приоритет 1
                } else {
                    chunkPriorityArray[i][j] = 0; // Если чанка нет в ChunckPriorityMap, приоритет 0
                }

                coordsCheckRam = new WorldCoord(coordsCheck);
                coordsCheck = new WorldCoord(coordsCheckRam.getWorldName(), coordsCheckRam.getX()+1, coordsCheckRam.getZ());
            }
            coordsCheckRam = new WorldCoord(coordsCheck);
            coordsCheck = new WorldCoord(coordsCheckRam.getWorldName(), coordsCheckRam.getX()-5, coordsCheckRam.getZ()+1);
        }

        int centerX = chunkPriorityArray.length / 2;
        int centerY = chunkPriorityArray[0].length / 2;
        chunkPriorityArray[centerX][centerY] = 1;

        /*printChunkPriorityArray(chunkPriorityArray);*/

        // Пробегаемся от центра в четырех направлениях
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if ((i == j) || (i + 2 == j) || (i - 2 == j)) {
                    continue; // Пропускаем диагональные элементы
                }

                int neighborX = centerX + i;
                int neighborY = centerY + j;

                // Проверяем, чтобы соседние элементы находились в пределах массива и имели приоритет 0
                if ((neighborX >= 0 && neighborX < chunkPriorityArray.length) &&
                        (neighborY >= 0 && neighborY < chunkPriorityArray[0].length) &&
                        (chunkPriorityArray[neighborX][neighborY] == 0)) {

                    boolean allNeighborsAreOne = true;

                    // Проверяем соседние элементы
                    for (int k = -1; k <= 1; k++) {
                        for (int l = -1; l <= 1; l++) {
                            if ((k == l) ||
                                    (k + 2 == l) ||
                                    (k - 2 == l)) {
                                continue; // Пропускаем диагональные и текущий элемент
                            }

                            int neighborNeighborX = neighborX + k;
                            int neighborNeighborY = neighborY + l;

                            // Проверяем, чтобы соседние соседних элементов находились в пределах массива и имели приоритет 1
                            if ((neighborNeighborX >= 0 && neighborNeighborX < chunkPriorityArray.length) &&
                                    (neighborNeighborY >= 0 && neighborNeighborY < chunkPriorityArray[0].length) &&
                                    (chunkPriorityArray[neighborNeighborX][neighborNeighborY] != 1)) {
                                allNeighborsAreOne = false;
                                break;
                            }
                        }

                        if (!allNeighborsAreOne) {
                            break;
                        }
                    }

                    // Если все соседние элементы равны 1, выводим число 1 в консоль
                    if (!allNeighborsAreOne) {
                        player.sendMessage(ChatColor.RED+"Некорректное перекрытие чанков, отмена привата\nБазовые чанки не должны ограничивать обычные чанки со всех сторон");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkInvalidChunksForLP(Player player, TownClaimEvent event) {
        int x = event.getTownBlock().getX();
        int y = event.getTownBlock().getZ();
        WorldCoord coordsCheck = new WorldCoord(player.getWorld(), x-2, y-2);
        WorldCoord coordsCheckRam = null;

        // Create a 5x5 two-dimensional array called chunkPriorityArray
        int[][] chunkPriorityArray = new int[5][5];

        for (int i = 0; i <= 4; i++) {
            for (int j = 0; j <= 4; j++) {
                if (coordsCheck.isWilderness()) {
                    chunkPriorityArray[i][j] = -1; // If the chunk is wilderness, set priority to -1
                } else if (BookTownControl.townAddition.get(event.getTown().getUUID()).getChunckPriorityMap().containsKey((new ChunkCoord(coordsCheck.getX(),coordsCheck.getZ(),coordsCheck.getWorldName())))) {
                    chunkPriorityArray[i][j] = 1; // If the chunk is in ChunckPriorityMap, set priority to 1
                } else {
                    chunkPriorityArray[i][j] = 0; // If the chunk is not in ChunckPriorityMap, set priority to 0
                }

                coordsCheckRam = new WorldCoord(coordsCheck);
                coordsCheck = new WorldCoord(coordsCheckRam.getWorldName(), coordsCheckRam.getX() + 1, coordsCheckRam.getZ());
            }
            coordsCheckRam = new WorldCoord(coordsCheck);
            coordsCheck = new WorldCoord(coordsCheckRam.getWorldName(), coordsCheckRam.getX() - 5, coordsCheckRam.getZ() + 1);
        }

        //printChunkPriorityArray(chunkPriorityArray);

        // Check if the center element and its adjacent elements in all four directions are equal to 1
        boolean allOnes = true;
        int centerRow = chunkPriorityArray.length / 2;
        int centerColumn = chunkPriorityArray[0].length / 2;


            if (chunkPriorityArray[centerRow - 1][centerColumn] != 1) {
                allOnes = false;
            }
            else if (chunkPriorityArray[centerRow + 1][centerColumn] != 1) {
                allOnes = false;
            }
            else if (chunkPriorityArray[centerRow][centerColumn - 1] != 1) {
                allOnes = false;
            }
            else if (chunkPriorityArray[centerRow][centerColumn + 1] != 1) {
                allOnes = false;
            }


        return allOnes;
    }
}