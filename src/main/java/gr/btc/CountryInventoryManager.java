package gr.btc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class CountryInventoryManager implements Listener {

    private Plugin plugin;

    public CountryInventoryManager(Plugin plugin) {
        this.plugin = plugin;
        //Bukkit.getPluginManager().registerEvents(this, plugin);
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



            if (inventoryTitle.equalsIgnoreCase("Связать армию со страной")) {
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
