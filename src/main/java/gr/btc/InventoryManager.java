package gr.btc;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class InventoryManager implements Listener {

    private Plugin plugin;

    public InventoryManager(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
    public static void openInventoryArmyControl(Player player) {
        MyHolder holder = new MyHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, "Военные конфликты");

        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
        ItemMeta ironSwordMeta = ironSword.getItemMeta();
        ironSwordMeta.setDisplayName("Военные конфликты");
        ironSwordMeta.setLore(Arrays.asList("Ваша армия участвует в "+1+" военных конфликтах"));
        ironSword.setItemMeta(ironSwordMeta);

        ItemStack birchDoor = new ItemStack(Material.BIRCH_DOOR);
        ItemMeta birchDoorMeta = birchDoor.getItemMeta();
        birchDoorMeta.setDisplayName("Пригласить в армию");
        birchDoorMeta.setLore(Arrays.asList("Описание предмета"));
        birchDoor.setItemMeta(birchDoorMeta);

        ItemStack ponder = new ItemStack(Material.GOAT_HORN);
        ItemMeta ponderMeta = ponder.getItemMeta();
        ponderMeta.setDisplayName("Присоединиться к стране");
        ponderMeta.setLore(Arrays.asList("Описание предмета"));
        ponder.setItemMeta(ponderMeta);

        ItemStack woodenSword = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta woodenSwordMeta = woodenSword.getItemMeta();
        woodenSwordMeta.setDisplayName("Список бойцов");
        woodenSwordMeta.setLore(Arrays.asList("Описание предмета"));
        woodenSword.setItemMeta(woodenSwordMeta);

        ItemStack pufferfish = new ItemStack(Material.PUFFERFISH);
        ItemMeta pufferfishMeta = pufferfish.getItemMeta();
        pufferfishMeta.setDisplayName("Распустить армию");
        pufferfishMeta.setLore(Arrays.asList("Описание предмета"));
        pufferfish.setItemMeta(pufferfishMeta);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName("Покинуть армию");
        barrierMeta.setLore(Arrays.asList("Описание предмета"));
        barrier.setItemMeta(barrierMeta);

        // Расположите предметы в меню
        inventory.setItem(10, birchDoor);
        inventory.setItem(11, ponder);
        inventory.setItem(12, woodenSword);
        inventory.setItem(14, pufferfish);
        inventory.setItem(15, barrier);
        inventory.setItem(13, ironSword);

        holder.setInventory(inventory);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getClickedInventory().getHolder();
        if (holder instanceof MyHolder) {
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                String inventoryTitle = event.getView().getTitle();
                if (inventoryTitle.equalsIgnoreCase("Главное меню")) {
                    if (clickedItem.getItemMeta().getDisplayName().equals("Армии")) {
                        openInventoryArmyCrOrJoin(player);
                    }
                }
                else if(inventoryTitle.equalsIgnoreCase("Армии")) {
                    if (clickedItem.getItemMeta().getDisplayName().equals("Присоединиться к армии")) {
                        player.sendMessage("");
                    }
                    else if (clickedItem.getItemMeta().getDisplayName().equals("Создать армию")) {
                        player.sendMessage("");
                    }
                }

                else if (inventoryTitle.equalsIgnoreCase("Военные конфликты")) {
                    if (clickedItem.getItemMeta().getDisplayName().equals("Пригласить в армию")) {
                        player.sendMessage("Вы пригласили игрока в армию!");
                    } else if (clickedItem.getItemMeta().getDisplayName().equals("Список бойцов")) {
                        player.sendMessage("Список бойцов вашей армии:");
                    } else if (clickedItem.getItemMeta().getDisplayName().equals("Распустить армию")) {
                        player.sendMessage("Вы распустили свою армию!");
                    } else if (clickedItem.getItemMeta().getDisplayName().equals("Присоединиться к стране")) {
                        player.sendMessage("Вы присоединились к стране!");
                    } else if (clickedItem.getItemMeta().getDisplayName().equals("Покинуть армию")) {
                        player.sendMessage("Вы покинули армию!");
                    }
                }
            }

            event.setCancelled(true); // Отменить перемещение предмета
        }
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