package com.jvmd.uniqueVehiclePlugin.gui;

import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleDatabase;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleEditorGui implements Listener {

    private static final String TITLE = "Vehicle Editor";
    private static final int SIZE = 36;

    private static final String[] PART_KEYS = {
        "frame", "steering_wheel", "door_0", "door_1",
        "wheel_0", "wheel_1", "wheel_2", "wheel_3", "seat"
    };
    private static final String[] PART_NAMES = {
        "Frame", "Steering Wheel", "Door Left", "Door Right",
        "Wheel FL", "Wheel FR", "Wheel BL", "Wheel BR", "Seat"
    };
    private static final Material[] PART_MATERIALS = {
        Material.IRON_BLOCK, Material.COMPARATOR, Material.OAK_DOOR, Material.OAK_DOOR,
        Material.MINECART, Material.MINECART, Material.MINECART, Material.MINECART, Material.SADDLE
    };

    private final Map<UUID, EditSession> sessions = new ConcurrentHashMap<>();
    private final VehicleDatabase database;

    public VehicleEditorGui(VehicleDatabase database) {
        this.database = database;
    }

    public void open(Player player, Vehicle vehicle) {
        EditSession session = new EditSession(vehicle);
        sessions.put(player.getUniqueId(), session);

        Inventory inv = Bukkit.createInventory(null, SIZE,
                Component.text(TITLE).decoration(TextDecoration.ITALIC, false));
        refresh(inv, session);
        player.openInventory(inv);
    }

    private void refresh(Inventory inv, EditSession session) {
        inv.clear();
        int sel = session.getSelectedPartIndex();
        String partKey = PART_KEYS[sel];
        PartCustomization part = session.getVehicle().getCustomization().getPart(partKey);
        float globalScale = session.getVehicle().getCustomization().getGlobalScale();

        // Row 0: Part selection
        for (int i = 0; i < 9; i++) {
            ItemStack item = new ItemStack(PART_MATERIALS[i]);
            ItemMeta meta = item.getItemMeta();
            boolean selected = i == sel;
            meta.displayName(Component.text(PART_NAMES[i],
                    selected ? NamedTextColor.GREEN : NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            if (selected) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // Row 1: Offset X Y Z adjustment
        inv.setItem(9,  makeButton(Material.RED_STAINED_GLASS_PANE,   "X ← (Shift: -0.5)", NamedTextColor.RED));
        inv.setItem(10, makeValue("X: " + fmt(part.getDeltaX()), NamedTextColor.WHITE, "Left-click: ±0.0625  |  Shift: ±0.5"));
        inv.setItem(11, makeButton(Material.GREEN_STAINED_GLASS_PANE,  "X → (Shift: +0.5)", NamedTextColor.GREEN));
        inv.setItem(12, makeButton(Material.RED_STAINED_GLASS_PANE,   "Y ← (Shift: -0.5)", NamedTextColor.RED));
        inv.setItem(13, makeValue("Y: " + fmt(part.getDeltaY()), NamedTextColor.WHITE, "Left-click: ±0.0625  |  Shift: ±0.5"));
        inv.setItem(14, makeButton(Material.GREEN_STAINED_GLASS_PANE,  "Y → (Shift: +0.5)", NamedTextColor.GREEN));
        inv.setItem(15, makeButton(Material.RED_STAINED_GLASS_PANE,   "Z ← (Shift: -0.5)", NamedTextColor.RED));
        inv.setItem(16, makeValue("Z: " + fmt(part.getDeltaZ()), NamedTextColor.WHITE, "Left-click: ±0.0625  |  Shift: ±0.5"));
        inv.setItem(17, makeButton(Material.GREEN_STAINED_GLASS_PANE,  "Z → (Shift: +0.5)", NamedTextColor.GREEN));

        // Row 2: Read-only pivot info from config
        Vector3d pivot = getConfigPivot(session.getVehicle(), sel);
        if (pivot != null) {
            inv.setItem(18, makeInfo("Pivot X: " + fmt(pivot.x), NamedTextColor.AQUA, "Configured in config.yml"));
            inv.setItem(19, makeInfo("Pivot Y: " + fmt(pivot.y), NamedTextColor.AQUA, "Configured in config.yml"));
            inv.setItem(20, makeInfo("Pivot Z: " + fmt(pivot.z), NamedTextColor.AQUA, "Configured in config.yml"));
            for (int i = 21; i <= 26; i++)
                inv.setItem(i, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));
        } else {
            for (int i = 18; i <= 26; i++)
                inv.setItem(i, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));
        }

        // Row 3: Scale + controls
        inv.setItem(27, makeButton(Material.RED_STAINED_GLASS_PANE,  "Scale ← (Shift: -0.5)", NamedTextColor.RED));
        inv.setItem(28, makeValue("Scale: " + String.format("%.2f", globalScale), NamedTextColor.YELLOW, "Range: 0.1 - 5.0  |  Shift: ±0.5"));
        inv.setItem(29, makeButton(Material.GREEN_STAINED_GLASS_PANE, "Scale → (Shift: +0.5)", NamedTextColor.GREEN));
        for (int i = 30; i <= 32; i++)
            inv.setItem(i, makeButton(Material.BLACK_STAINED_GLASS_PANE, " ", NamedTextColor.BLACK));
        inv.setItem(33, makeButton(Material.YELLOW_CONCRETE, "Reset Part",  NamedTextColor.YELLOW));
        inv.setItem(34, makeButton(Material.RED_CONCRETE,    "Cancel",      NamedTextColor.RED));
        inv.setItem(35, makeButton(Material.LIME_CONCRETE,   "Save",        NamedTextColor.GREEN));
    }

    private Vector3d getConfigPivot(Vehicle vehicle, int partIndex) {
        VehicleConfig cfg = vehicle.getConfig();
        return switch (partIndex) {
            case 1 -> cfg.steeringWheel().pivot();
            case 2 -> cfg.doors().size() > 0 ? cfg.doors().get(0).part().pivot() : null;
            case 3 -> cfg.doors().size() > 1 ? cfg.doors().get(1).part().pivot() : null;
            case 4 -> cfg.wheels().size() > 0 ? cfg.wheels().get(0).part().pivot() : null;
            case 5 -> cfg.wheels().size() > 1 ? cfg.wheels().get(1).part().pivot() : null;
            case 6 -> cfg.wheels().size() > 2 ? cfg.wheels().get(2).part().pivot() : null;
            case 7 -> cfg.wheels().size() > 3 ? cfg.wheels().get(3).part().pivot() : null;
            default -> null;
        };
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        EditSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        Component title = event.getView().title();
        if (!title.equals(Component.text(TITLE).decoration(TextDecoration.ITALIC, false))) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= SIZE) return;

        boolean shift = event.isShiftClick();
        handleClick(player, session, slot, shift);
        refresh(player.getOpenInventory().getTopInventory(), session);
    }

    private void handleClick(Player player, EditSession session, int slot, boolean shift) {
        if (slot <= 8) {
            session.setSelectedPartIndex(slot);
            return;
        }

        String partKey = PART_KEYS[session.getSelectedPartIndex()];
        PartCustomization part = session.getVehicle().getCustomization().getPart(partKey);
        double step = shift ? 0.5 : 0.0625;

        switch (slot) {
            case 9  -> part.setDeltaX(part.getDeltaX() - step);
            case 11 -> part.setDeltaX(part.getDeltaX() + step);
            case 12 -> part.setDeltaY(part.getDeltaY() - step);
            case 14 -> part.setDeltaY(part.getDeltaY() + step);
            case 15 -> part.setDeltaZ(part.getDeltaZ() - step);
            case 17 -> part.setDeltaZ(part.getDeltaZ() + step);
            case 27 -> adjustScale(session, shift ? -0.5f : -0.1f, player);
            case 29 -> adjustScale(session, shift ? 0.5f  :  0.1f, player);
            case 33 -> {
                session.getVehicle().getCustomization().resetPart(partKey);
                player.sendActionBar(Component.text("Part reset", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
            case 34 -> {
                sessions.remove(player.getUniqueId());
                session.revert();
                player.closeInventory();
                player.sendMessage(Component.text("Edit cancelled.", NamedTextColor.RED));
            }
            case 35 -> {
                sessions.remove(player.getUniqueId());
                database.save(session.getVehicle().getCustomization());
                player.closeInventory();
                player.sendMessage(Component.text("Vehicle customization saved!", NamedTextColor.GREEN));
            }
        }
    }

    private void adjustScale(EditSession session, float delta, Player player) {
        float current = session.getVehicle().getCustomization().getGlobalScale();
        float next = Math.max(0.1f, Math.min(5.0f, current + delta));
        session.getVehicle().applyGlobalScale(next);
        player.sendActionBar(Component.text("Scale: " + String.format("%.2f", next), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        EditSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;
        session.revert();
        player.sendMessage(Component.text("Edit cancelled.", NamedTextColor.RED));
    }

    private ItemStack makeButton(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeValue(String name, NamedTextColor color, String lore) {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeInfo(String name, NamedTextColor color, String lore) {
        ItemStack item = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    private String fmt(double value) {
        return String.format("%.4f", value);
    }
}
