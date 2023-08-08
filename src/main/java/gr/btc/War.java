package gr.btc;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.*;

public class War implements Serializable {
    Date StartDate;
    boolean isStarted = false;
    List<UUID> sides1 = new ArrayList<>();
    List<UUID> sides2 = new ArrayList<>();
    List<OfflinePlayer> side1Warriors = new ArrayList<>();
    List<OfflinePlayer> side2Warriors = new ArrayList<>();

    boolean side1TryToStop = false;
    boolean side2TryToStop = false;


    public War(UUID side1, UUID side2) {
        this.sides1.add(side1);
        this.sides2.add(side2);
        Town town1 = TownyUniverse.getInstance().getTown(side1);
        Town town2 = TownyUniverse.getInstance().getTown(side2);
        for (Army army : BookTownControl.Armys) {
            if (army.IsCountryConnected(town1)) {
                for (Army.ArmyPlayer ap : army.getPlayers()) {
                    if (Objects.equals(army.GetLink(ap), town1.getUUID())) {
                        side1Warriors.add(ap.getPlayer());

                    }
                }
            }
        }
        for (Army army : BookTownControl.Armys) {
            if (army.IsCountryConnected(town2)) {
                for (Army.ArmyPlayer ap : army.getPlayers()) {
                    if (Objects.equals(army.GetLink(ap), town2.getUUID())) {
                        side2Warriors.add(ap.getPlayer());
                    }
                }
            }
        }
        int side1WarriorsCount = 0;
        int side2WarriorsCount = 0;
        if(!side1Warriors.isEmpty()) {
            side1WarriorsCount = side1Warriors.size();
        }
        if(!side2Warriors.isEmpty()) {
            side2WarriorsCount = side2Warriors.size();
        }

        if (side1WarriorsCount == side2WarriorsCount) {
            this.StartDate = new Date();
            this.isStarted = true;
            Bukkit.broadcastMessage(ChatColor.RED + "Началась война между " + town1.getName() + " и " + town2.getName());
        } else {
            int daysToReinforce = 4;
            this.StartDate = new Date(System.currentTimeMillis() + (daysToReinforce * 24 * 60 * 60 * 1000));
            this.isStarted = false;
            int warriorsDifference = Math.abs(side1WarriorsCount - side2WarriorsCount);



            if (side1WarriorsCount > side2WarriorsCount) {
                if (side1WarriorsCount < 2) {
                    side1WarriorsCount = 2 - warriorsDifference;
                }
                town2.getMayor().getPlayer().sendMessage("У вас есть " + daysToReinforce + " дня на укрепление армии. Вам не хватает " + warriorsDifference + " бойцов.");
            }
            else if(side2WarriorsCount > side1WarriorsCount) {
                if (side2WarriorsCount < 2) {
                    side2WarriorsCount = 2 - warriorsDifference;
                }
                town1.getMayor().getPlayer().sendMessage("У вас есть " + daysToReinforce + " дня на укрепление армии. Вам не хватает " + warriorsDifference + " бойцов.");
            }
        }
    }

    public List<Army> GetArmy() {
        List<Army> armies = new ArrayList<>();
        for(UUID town : sides1) {
            for (Army army : BookTownControl.Armys) {
                if (army.IsCountryConnected(TownyUniverse.getInstance().getTown(town))) {
                    armies.add(army);
                }
            }
        }
        for(UUID town : sides2) {
            for (Army army : BookTownControl.Armys) {
                if (army.IsCountryConnected(TownyUniverse.getInstance().getTown(town))) {
                    armies.add(army);
                }
            }
        }
        return armies;
    }

    public List<Town> GetTowns() {
        List<Town> towns = new ArrayList<>();
        for(UUID town : sides1) {
            towns.add(TownyUniverse.getInstance().getTown(town));
        }
        for(UUID town : sides2) {
            towns.add(TownyUniverse.getInstance().getTown(town));
        }
        return towns;
    }

    public void SwitchSide(boolean side1, boolean side2) {
        if(side1) {
            side1TryToStop = !side1TryToStop;
        }
        if(side2) {
            side2TryToStop = !side2TryToStop;
        }
    }
    public boolean GetTownsByTwo(String st1, String st2) {
        boolean type1 = false;
        boolean type2 = false;
        if(TownyUniverse.getInstance().getTown(sides1.get(0)).getName().contains(st1)) {
            type1 = true;
        }
        if(TownyUniverse.getInstance().getTown(sides2.get(0)).getName().contains(st2)) {
            type2 = true;
        }
        return type1 && type2;
    }
    public boolean TryToEndWar() {
        //REFIRE
        if(!this.isStarted) {
            if (side1Warriors.size() == side2Warriors.size() && (side1Warriors.size() >= 2 && side2Warriors.size() >= 2)) {
                this.StartDate = new Date();
                this.isStarted = true;
                Bukkit.broadcastMessage(ChatColor.RED + "Началась война между " + TownyUniverse.getInstance().getTown(sides1.get(0)).getName() + " и " + TownyUniverse.getInstance().getTown(sides2.get(0)).getName());
            }
            return false;
        }
        else {
            //END
            Date currentDate = new Date();
            if (this.StartDate.getTime() + (2 * 24 * 60 * 60 * 1000) <= currentDate.getTime() || (side1TryToStop && side2TryToStop)) {
                Town town1 = TownyUniverse.getInstance().getTown(sides1.get(0));
                Town town2 = TownyUniverse.getInstance().getTown(sides2.get(0));
                Bukkit.broadcastMessage(ChatColor.GRAY + "Война между " + town1.getName() + " и " + town2.getName() + " окончена.");
                BookTownControl.Wars.remove(War.this);
                return true;
            } else {
                return false;
            }
        }
    }

    public Town ReturnTownByPlayer(Player player) {
        List<OfflinePlayer> Warriors = new ArrayList<>(this.side1Warriors);
        Warriors.addAll(this.side2Warriors);
        for(OfflinePlayer p : Warriors) {
            if(p.getUniqueId().equals(player.getUniqueId())) {
                if(side1Warriors.contains(p.getUniqueId())) {

                    return TownyUniverse.getInstance().getTown(sides1.get(0));
                }
                if(side2Warriors.contains(p.getUniqueId())) {
                    return TownyUniverse.getInstance().getTown(sides2.get(0));
                }
            }
        }
    }
}
