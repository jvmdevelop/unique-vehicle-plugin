package com.jvmd.uniqueVehiclePlugin.gui;

import com.jvmd.uniqueVehiclePlugin.customization.VehicleDatabase;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleEditorGui implements Listener {

    private static final String TITLE = "Vehicle Editor";
    private static final int SIZE = 36;

    // Row 0 — parts (position / rotation)
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

    // Row 1 — pivots (slots 10-16; slot 9 = label, slot 17 = empty)
    // Only parts that have a pivot point
    private static final String[] PIVOT_KEYS = {
        "steering_wheel", "door_0", "door_1",
        "wheel_0", "wheel_1", "wheel_2", "wheel_3"
    };
    private static final String[] PIVOT_NAMES = {
        "Pivot: Steering Wheel", "Pivot: Door Left", "Pivot: Door Right",
        "Pivot: Wheel FL", "Pivot: Wheel FR", "Pivot: Wheel BL", "Pivot: Wheel BR"
    };

    private final Map<UUID, GuiSession> sessions = new ConcurrentHashMap<>();
    private final VehicleDatabase database;
    private final VehiclePartEditor partEditor;
    private final Plugin plugin;

    public VehicleEditorGui(VehicleDatabase database, VehiclePartEditor partEditor, Plugin plugin) {
        this.database = database;
        this.partEditor = partEditor;
        this.plugin = plugin;
    }

    public void open(Player player, Vehicle vehicle) {
        sessions.put(player.getUniqueId(), new GuiSession(vehicle));
        Inventory inv = Bukkit.createInventory(null, SIZE,
                Component.text(TITLE).decoration(TextDecoration.ITALIC, false));
        refresh(inv, sessions.get(player.getUniqueId()));
        player.openInventory(inv);
    }

    private void refresh(Inventory inv, GuiSession session) {
        inv.clear();

        // ── Row 0: Part buttons (position/rotation editing) ──────────────────
        for (int i = 0; i < 9; i++) {
            ItemStack item = new ItemStack(PART_MATERIALS[i]);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(PART_NAMES[i], NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to edit position / rotation", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // ── Row 1: Pivot buttons ──────────────────────────────────────────────
        // Slot 9 = section label
        inv.setItem(9, makeLabel("— Pivots —", NamedTextColor.LIGHT_PURPLE,
                "Click to edit the pivot (rotation center) of a part"));

        // Slots 10-16: one button per pivot-capable part
        for (int i = 0; i < PIVOT_KEYS.length; i++) {
            ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(PIVOT_NAMES[i], NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Click to edit pivot point", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Pivot highlighted with particles", NamedTextColor.DARK_PURPLE)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
            inv.setItem(10 + i, item); // slots 10-16
        }

        inv.setItem(17, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));

        // ── Row 2: Scale + actions ────────────────────────────────────────────
        inv.setItem(18, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));
        inv.setItem(19, makeButton(Material.RED_STAINED_GLASS_PANE,   "Scale −", NamedTextColor.RED));
        inv.setItem(20, makeValue("Scale: " + String.format("%.2f",
                session.vehicle.getCustomization().getGlobalScale()),
                NamedTextColor.YELLOW, "Click ±0.1  |  Shift ±0.5"));
        inv.setItem(21, makeButton(Material.GREEN_STAINED_GLASS_PANE,  "Scale +", NamedTextColor.GREEN));
        inv.setItem(22, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));
        inv.setItem(23, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));
        inv.setItem(24, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));
        inv.setItem(25, makeButton(Material.YELLOW_CONCRETE,  "Reset All Offsets", NamedTextColor.YELLOW));
        inv.setItem(26, makeButton(Material.RED_CONCRETE,     "Close",             NamedTextColor.RED));

        // ── Row 3: Controls hint ──────────────────────────────────────────────
        for (int i = 27; i <= 35; i++)
            inv.setItem(i, makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.GRAY));

        ItemStack hintItem = new ItemStack(Material.PAPER);
        ItemMeta hintMeta = hintItem.getItemMeta();
        hintMeta.displayName(Component.text("Controls", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        hintMeta.lore(List.of(
                Component.text("WASD → move XZ",                                     NamedTextColor.GRAY)  .decoration(TextDecoration.ITALIC, false),
                Component.text("Ctrl + W/S → move Y",                               NamedTextColor.GRAY)  .decoration(TextDecoration.ITALIC, false),
                Component.text("Space + A/D → rotate part",                         NamedTextColor.GRAY)  .decoration(TextDecoration.ITALIC, false),
                Component.text("Sprint + Space → toggle PIVOT / POSITION mode",     NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false),
                Component.text("  pivot mode: WASD = pivot XZ, Ctrl+WS = pivot Y", NamedTextColor.DARK_PURPLE) .decoration(TextDecoration.ITALIC, false),
                Component.text("Stick ← / → → orbit left/right (right-click)",     NamedTextColor.AQUA)  .decoration(TextDecoration.ITALIC, false),
                Component.text("Stick ← / → → orbit up/down (left-click)",         NamedTextColor.AQUA)  .decoration(TextDecoration.ITALIC, false),
                Component.text("Sneak → dismount & save",                           NamedTextColor.GRAY)  .decoration(TextDecoration.ITALIC, false)
        ));
        hintItem.setItemMeta(hintMeta);
        inv.setItem(31, hintItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GuiSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        Component title = event.getView().title();
        if (!title.equals(Component.text(TITLE).decoration(TextDecoration.ITALIC, false))) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= SIZE) return;

        boolean shift = event.isShiftClick();
        Vehicle vehicle = session.vehicle;

        // Row 0 (0-8): part → POSITION mode editor
        if (slot < 9) {
            String partKey = PART_KEYS[slot];
            sessions.remove(player.getUniqueId());
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    partEditor.startSession(player, vehicle, partKey), 1L);
            return;
        }

        // Row 1 (10-16): pivot → PIVOT mode editor
        if (slot >= 10 && slot <= 16) {
            int idx = slot - 10;
            String partKey = PIVOT_KEYS[idx];
            sessions.remove(player.getUniqueId());
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    partEditor.startSessionInPivotMode(player, vehicle, partKey), 1L);
            return;
        }

        // Row 2: scale / reset / close
        switch (slot) {
            case 19 -> { adjustScale(session, player, shift ? -0.5f : -0.1f); refresh(player.getOpenInventory().getTopInventory(), session); }
            case 21 -> { adjustScale(session, player, shift ?  0.5f :  0.1f); refresh(player.getOpenInventory().getTopInventory(), session); }
            case 25 -> { resetAllOffsets(session, player); refresh(player.getOpenInventory().getTopInventory(), session); }
            case 26 -> { sessions.remove(player.getUniqueId()); player.closeInventory(); }
        }
    }

    private void adjustScale(GuiSession session, Player player, float delta) {
        float next = Math.max(0.1f, Math.min(5.0f, session.vehicle.getCustomization().getGlobalScale() + delta));
        session.vehicle.applyGlobalScale(next);
        database.save(session.vehicle.getCustomization());
        player.sendActionBar(Component.text("Scale: " + String.format("%.2f", next), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
    }

    private void resetAllOffsets(GuiSession session, Player player) {
        for (String key : PART_KEYS)
            session.vehicle.getCustomization().resetPart(key);
        database.save(session.vehicle.getCustomization());
        player.sendActionBar(Component.text("All offsets reset.", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        GuiSession session = sessions.remove(player.getUniqueId());
        if (session != null) database.save(session.vehicle.getCustomization());
    }

    // ── Item helpers ──────────────────────────────────────────────────────────

    private ItemStack makeButton(Material mat, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeLabel(String name, NamedTextColor color, String lore) {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
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

    private static class GuiSession {
        final Vehicle vehicle;
        GuiSession(Vehicle vehicle) { this.vehicle = vehicle; }
    }
}
