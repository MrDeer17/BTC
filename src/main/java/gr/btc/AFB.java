package gr.btc;

import com.palmergames.adventure.platform.viaversion.ViaFacet;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AFB {

    public void Start(Player player, BookMeta bMeta, ItemStack mhi) {

        List<String> lore = mhi.getItemMeta().getLore();
        BookTownControl.PlayerFillingBook.put(player.getUniqueId(),new AFBSerialized(bMeta,mhi));
        BookTownControl.savePFB();
        if(bMeta.getPage(1).contains("X Y координаты")) {
            if (OnBookWrite.playerSelectionTime.containsKey(player.getUniqueId())) {
                Location playerLocation = player.getLocation();
                int chunkX = playerLocation.getBlockX() >> 4; // Деление на 16
                int chunkZ = playerLocation.getBlockZ() >> 4; // Деление на 16

                WorldCoord coordsCheck = new WorldCoord(player.getWorld(), chunkX-10, chunkZ-10);
                if (OnBookWrite.isChunkOccupied(coordsCheck, player.getWorld(), player)) {
                    return;
                }
                player.sendMessage("Чанк выбран");
                int roundedX = (int) Math.round(player.getLocation().getX());
                int roundedZ = (int) Math.round(player.getLocation().getZ());
                bMeta.setPage(1, roundedX + " " + roundedZ);
                OnBookWrite.playerSelectionTime.remove(player.getUniqueId());
                OnBookWrite.playerSelectionTask.get(player.getUniqueId()).cancel();
                Start(player,bMeta,mhi);
            }
            else {
                // Если игрок только начинает выбор, стартуем таймер и показываем эффекты
                player.sendMessage(ChatColor.GREEN+"Выберите начальный чанк, чтобы установить страну.\nЗажмите SHIFT если хотите выбрать текущий чанк и не отпускайте\nПока в чате не напишет что чанк был выбран.");
                OnBookWrite.playerSelectionTime.put(player.getUniqueId(), System.currentTimeMillis());
                // Запускаем асинхронную задачу для отображения эффектов
                BukkitRunnable runnable = new BukkitRunnable() {
                    boolean chunkSelected = false;
                    @Override
                    public void run() {
                        if (player.isSneaking() && !chunkSelected) {
                            Start(player, bMeta, mhi);
                            chunkSelected = true;
                            this.cancel();
                        }
                        showSelectionEffects(player);
                        long selectionStartTime = OnBookWrite.playerSelectionTime.get(player.getUniqueId());
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - selectionStartTime;
                        if (!(elapsedTime < 60000)) {
                            player.sendMessage("Вы не успели выбрать чанк");
                            OnBookWrite.playerSelectionTime.remove(player.getUniqueId());
                            OnBookWrite.playerSelectionTask.get(player.getUniqueId()).cancel();
                            Bukkit.getScheduler().runTask(BookTownControl.getPlugin(BookTownControl.class), () -> {
                                player.getWorld().dropItem(player.getLocation(), mhi);
                            });
                            this.cancel();
                        }
                    }
                };
                OnBookWrite.playerSelectionTask.put(player.getUniqueId(),runnable);
                runnable.runTaskTimerAsynchronously(BookTownControl.getPlugin(BookTownControl.class), 0L, 20L);
            }
        }
        else if (bMeta.getPage(3).contains("Ники жителей через пробел")) {
            askForResponse(player, "Введите ники жителей через пробел:"+ ChatColor.GRAY+"\nЕсли таковых нет напишите: \"skip\" или \"пропустить\"\nЕсли ник содержит слово skip, и оно не длиннее 6 символов, то придётся пригласить человека вручную\n", bMeta, 3, mhi);
        }
        else if (bMeta.getPage(4).contains("Название вашей страны")) {
            askForResponse(player, "Введите название вашей страны:", bMeta, 4,mhi);
        }
        else {
            lore.add(lore.size() - 1,  ChatColor.GREEN+"Открыто для чтения");
            bMeta.setLore(lore);
            mhi.setItemMeta(bMeta);
            Bukkit.getScheduler().runTask(BookTownControl.getPlugin(BookTownControl.class), () -> {
                player.getWorld().dropItem(player.getLocation(), mhi);
            });
            player.sendMessage("Книга заполнена, откройте книгу ещё раз, перепроверьте информацию и подпишите её любым названием.");
            BookTownControl.PlayerFillingBook.remove(player.getUniqueId());
            BookTownControl.savePFB();
        }
    }

    private void showSelectionEffects(Player player) {
        int chunkX = player.getLocation().getBlockX() >> 4; // Координата чанка по X, где находится игрок
        int chunkZ = player.getLocation().getBlockZ() >> 4; // Координата чанка по Z, где находится игрок
        Particle p = Particle.SOUL_FIRE_FLAME;

        for (int i = chunkX - 10; i <= chunkX + 10; i++) {
            for (int j = chunkZ - 10; j <= chunkZ + 10; j++) {
                WorldCoord coordsCheck = new WorldCoord(player.getWorld().getName(),player.getWorld().getUID() , i << 4, j << 4);

                if (!coordsCheck.isWilderness()) {
                    p = Particle.VILLAGER_ANGRY;
                }
            }
        } //Villager Angry if bad

        int minY = 0; // Минимальная высота, на которой будут отображаться эффекты
        int maxY = 300; // Максимальная высота, на которой будут отображаться эффекты

        // Отображаем эффекты на границах чанка
        for (int y = minY; y <= maxY; y += 7) {
            for (int x = chunkX * 16; x < (chunkX + 1) * 16; x++) {
                // Верхняя граница чанка
                player.spawnParticle(p, new Location(player.getWorld(), x, y, chunkZ * 16), 1, 0,0,0, 0);
                // Нижняя граница чанка
                player.spawnParticle(p, new Location(player.getWorld(), x, y, (chunkZ * 16)+16), 1, 0,0,0, 0);
            }
            for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++) {
                // Левая граница чанка
                player.spawnParticle(p, new Location(player.getWorld(), chunkX * 16, y, z), 1, 0,0,0, 0);
                // Правая граница чанка
                player.spawnParticle(p, new Location(player.getWorld(), (chunkX + 1) * 16, y, z), 1, 0,0,0, 0);
            }
        }
    }
    private void askForResponse(Player player, String question, BookMeta bMeta, int pageIndex, ItemStack mhi) {
        CompletableFuture<Void> responseFuture = new CompletableFuture<>();
        player.sendMessage(question);
        if (pageIndex == 3) {
            TextComponent confirmButton = new TextComponent(ChatColor.YELLOW+"Нажмите на эту строку чтобы пропустить этот этап");
            confirmButton.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "Пропустить"));
            player.spigot().sendMessage(confirmButton);
        }
        ChatListener chatListener = new ChatListener(player, responseFuture);
        // Отправляем сообщение и ожидаем ответа
        Bukkit.getScheduler().runTaskAsynchronously(BookTownControl.getPlugin(BookTownControl.class), () -> {
            Bukkit.getPluginManager().registerEvents(chatListener, BookTownControl.getPlugin(BookTownControl.class));
        });

        responseFuture.orTimeout(60, TimeUnit.SECONDS).thenAccept(voidResult -> {
            String response = chatListener.getResponse(); // Получаем ответ только после успешного завершения ожидания
            if (pageIndex == 3 && (response.toLowerCase().contains("skip") || response.toLowerCase().contains("пропус"))) {
                response = "";
            }
            if(pageIndex == 4) {
                if(response.contains(" ")) {
                    player.sendMessage(ChatColor.RED+"Название страны не должно содержать пробелов, вместо них используйте "+ChatColor.GREEN+"_");
                    chatListener.unregisterChatListener(chatListener);
                    Start(player,bMeta,mhi);
                    return;
                }
            }
            bMeta.setPage(pageIndex, response);
            //player.getInventory().getItemInMainHand().setItemMeta(bMeta);
            player.sendMessage("Ответ принят");
            Start(player,bMeta,mhi);
            chatListener.unregisterChatListener(chatListener);
        }).exceptionally(ex -> {
            if (ex instanceof TimeoutException) {
                player.sendMessage("Превышено время ожидания ответа (60 секунд).");
                chatListener.unregisterChatListener(chatListener);
                Start(player,bMeta,mhi);
            }
            else {
                chatListener.unregisterChatListener(chatListener);
                player.sendMessage("Произошла ошибка при ожидании ответа.");
                ex.printStackTrace();
                Start(player,bMeta,mhi);
            }
            //Start(player,bMeta,mhi);
            return null;
        });
    }

}
