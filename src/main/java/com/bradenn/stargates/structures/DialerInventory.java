package com.bradenn.stargates.structures;

import com.bradenn.stargates.inventory.DestinationItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DialerInventory implements InventoryHolder {

    private final UUID stargateUUID;
    private final Stargate stargate;
    private final Inventory inventory;

    public DialerInventory(UUID stargateUUID) throws Exception {
        Stargate stargate = Stargate.fromUUID(stargateUUID);
        if (Objects.isNull(stargate)) throw new Exception("Invalid Stargate");

        this.stargate = stargate;

        String title = String.format("%s : %s", stargate.getName(), stargate.getAddress());
        this.inventory = generateInventory(title);
        this.stargateUUID = stargateUUID;

        Stargate.getAll().forEach(this::addDestination);
    }

    public Stargate getStargate() {
        return stargate;
    }

    private Inventory generateInventory(String title) {
        return Bukkit.createInventory(this, InventoryType.PLAYER, title);
    }

    private void addDestination(Stargate stargate) {
        if (stargate.getUUID().equals(this.stargate.getUUID())) return;

        inventory.addItem(new DestinationItem(stargate));
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    public UUID getStargateUUID() {
        return stargateUUID;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
