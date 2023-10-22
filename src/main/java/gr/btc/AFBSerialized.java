package gr.btc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
import java.util.*;

public class AFBSerialized implements Serializable {

    private List<String> lore;
    private String DN;
    private List<String> pages;

    public AFBSerialized(BookMeta bm, ItemStack is) {
        lore = is.getItemMeta().getLore();
        DN = is.getItemMeta().getDisplayName();
        pages = bm.getPages();
    }

    public void Start(Player player) {
        ItemStack is = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta itemMeta = (BookMeta) is.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setAuthor("");
        itemMeta.setTitle("");
        itemMeta.setDisplayName(DN);
        itemMeta.setPages(pages);
        is.setItemMeta(itemMeta);
        AFB afb = new AFB();
        afb.Start(player,itemMeta,is);
    }
}
