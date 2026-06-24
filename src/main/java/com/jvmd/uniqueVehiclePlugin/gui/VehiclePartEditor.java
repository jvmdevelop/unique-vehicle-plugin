package com.jvmd.uniqueVehiclePlugin.gui;

import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleDatabase;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleFrame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3d;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VehiclePartEditor implements Listener {

    /** Blocks per tick for position/pivot movement */
    private static final double STEP = 1.0 / 64.0;
    /** Degrees per tick for rotation */
    private static final double ROT_STEP = 2.8125;
    /** Ticks between each applied movement */
    private static final int MOVE_INTERVAL = 2;
    /** Degrees per orbit click */
    private static final double ORBIT_STEP = 15.0;
    /** Blocks per elevation click */
    private static final double ELEV_STEP = 0.5;
    /** Orbit radius (blocks from vehicle center) */
    private static final double ORBIT_RADIUS = 4.5;

    // Hotbar slots for orbit sticks
    private static final int SLOT_LEFT  = 3; // orbit left + elevation up
    private static final int SLOT_RIGHT = 5; // orbit right + elevation down

    private enum EditorMode { POSITION, PIVOT }

    private final Map<UUID, PartEditState> sessions = new ConcurrentHashMap<>();
    private final VehicleDatabase database;
    private final Plugin plugin;

    public VehiclePartEditor(VehicleDatabase database, Plugin plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public void tick() {
        for (var it = sessions.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            PartEditState state = entry.getValue();
            Player player = state.player;

            if (!player.isOnline() || !player.isValid()) {
                cleanupState(state, false);
                it.remove();
                continue;
            }

            // Keep stand frozen at its orbit position
            state.stand.teleport(state.standLoc);

            Input input = player.getCurrentInput();

            // Sprint+Space (new press) → toggle mode
            boolean sprintSpace = input.isSprint() && input.isJump();
            if (sprintSpace && !state.prevSprintSpace) {
                toggleMode(state);
            }
            state.prevSprintSpace = sprintSpace;

            // Apply part movement on interval
            if (--state.cooldown <= 0) {
                state.cooldown = MOVE_INTERVAL;
                processInput(state, input);
            }

            // Pivot highlight particles (every 3 ticks, shown whenever pivot exists)
            if (++state.particleTick >= 3) {
                state.particleTick = 0;
                spawnPivotParticles(state);
            }

            showActionBar(state);
        }
    }

    // ── Session management ────────────────────────────────────────────────────

    public void startSession(Player player, Vehicle vehicle, String partKey) {
        startSessionWithMode(player, vehicle, partKey, EditorMode.POSITION);
    }

    public void startSessionInPivotMode(Player player, Vehicle vehicle, String partKey) {
        startSessionWithMode(player, vehicle, partKey, EditorMode.PIVOT);
    }

    private void startSessionWithMode(Player player, Vehicle vehicle, String partKey, EditorMode initialMode) {
        endSession(player.getUniqueId());

        // Initial orbit: behind the vehicle (yaw + 180°)
        double initialAngle = vehicle.getLocation().getYaw() + 180.0;

        PartEditState state = new PartEditState(player, vehicle, partKey, initialAngle);
        state.mode = initialMode;

        // Spawn stand at computed orbit position
        Location standLoc = computeOrbitLoc(vehicle.getLocation(), initialAngle, 0.0);
        ArmorStand stand = standLoc.getWorld().spawn(standLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setCollidable(false);
            as.setInvulnerable(true);
        });
        stand.addPassenger(player);

        state.stand    = stand;
        state.standLoc = standLoc.clone();
        sessions.put(player.getUniqueId(), state);

        if (initialMode == EditorMode.PIVOT) {
            freezeAnimations(vehicle);
        }

        // Save & replace inventory with orbit sticks
        state.savedInventory = player.getInventory().getContents().clone();
        player.getInventory().clear();
        player.getInventory().setItem(SLOT_LEFT,  makeStick("← Orbit  /  ↑ Elevation", NamedTextColor.AQUA,
                List.of("Right-click: orbit left", "Left-click:  orbit up")));
        player.getInventory().setItem(SLOT_RIGHT, makeStick("→ Orbit  /  ↓ Elevation", NamedTextColor.GREEN,
                List.of("Right-click: orbit right", "Left-click:  orbit down")));

        sendModeTitle(player, partKey, initialMode, true);
    }

    public void endSession(UUID playerId) {
        PartEditState state = sessions.remove(playerId);
        if (state != null) {
            state.stand.eject();
            Bukkit.getScheduler().runTaskLater(plugin, () -> state.stand.remove(), 1L);
            restoreInventory(state);
        }
    }

    private void cleanupState(PartEditState state, boolean save) {
        state.stand.eject();
        state.stand.remove();
        restoreInventory(state);
        if (save) database.save(state.vehicle.getCustomization());
    }

    private void restoreInventory(PartEditState state) {
        if (state.savedInventory != null && state.player.isOnline()) {
            state.player.getInventory().setContents(state.savedInventory);
            state.savedInventory = null;
        }
    }

    // ── Orbit ─────────────────────────────────────────────────────────────────

    /** Orbit left/right by step degrees. */
    private void orbitHorizontal(PartEditState state, double step) {
        state.orbitAngle += step;
        refreshOrbit(state);
    }

    /** Move elevation up/down by step blocks. Clamped to [-2, 6]. */
    private void orbitElevation(PartEditState state, double step) {
        state.orbitElevation = Math.max(-2.0, Math.min(6.0, state.orbitElevation + step));
        refreshOrbit(state);
    }

    private void refreshOrbit(PartEditState state) {
        Location newLoc = computeOrbitLoc(state.vehicle.getLocation(), state.orbitAngle, state.orbitElevation);
        state.standLoc.setX(newLoc.getX());
        state.standLoc.setY(newLoc.getY());
        state.standLoc.setZ(newLoc.getZ());
        state.standLoc.setYaw(newLoc.getYaw());
    }

    /** Compute world location for the orbit stand given angle (degrees) and elevation offset. */
    private static Location computeOrbitLoc(Location vehicleLoc, double angleDeg, double elevation) {
        double rad = Math.toRadians(angleDeg);
        double bx = Math.sin(rad) * ORBIT_RADIUS;
        double bz = -Math.cos(rad) * ORBIT_RADIUS;

        double sx = vehicleLoc.getX() + bx;
        double sy = vehicleLoc.getY() + elevation;
        double sz = vehicleLoc.getZ() + bz;

        // Yaw so the stand faces the vehicle
        double dx = vehicleLoc.getX() - sx;
        double dz = vehicleLoc.getZ() - sz;
        float facingYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        return new Location(vehicleLoc.getWorld(), sx, sy, sz, facingYaw, 0f);
    }

    // ── Input (WASD) ──────────────────────────────────────────────────────────

    private void toggleMode(PartEditState state) {
        state.mode = (state.mode == EditorMode.POSITION) ? EditorMode.PIVOT : EditorMode.POSITION;
        if (state.mode == EditorMode.PIVOT) {
            freezeAnimations(state.vehicle);
        }
        sendModeTitle(state.player, state.partKey, state.mode, false);
    }

    /**
     * Freezes wheel spin angles to 0 so that {@code translation = pivot - R(pivot) = 0}
     * when R is effectively identity. Without this, accumulated spin from driving causes
     * pivot changes to visually shift the mesh (because R ≠ identity → pivot - R(pivot) ≠ 0).
     */
    private static void freezeAnimations(Vehicle vehicle) {
        vehicle.setSpeed(0);
        for (var wheel : vehicle.getWheels()) {
            wheel.setSpinAngle(0);
        }
    }

    private void processInput(PartEditState state, Input input) {
        PartCustomization part = state.vehicle.getCustomization().getPart(state.partKey);

        boolean space  = input.isJump();
        boolean sprint = input.isSprint();

        if (sprint && space) return; // mode-toggle combo, skip movement

        if (space) {
            // Space + A/D = rotation Y (works in both modes)
            if (input.isLeft())  part.setRotationYaw(part.getRotationYaw() - ROT_STEP);
            if (input.isRight()) part.setRotationYaw(part.getRotationYaw() + ROT_STEP);
        } else if (state.mode == EditorMode.PIVOT) {
            if (sprint) {
                if (input.isForward())  part.setPivotDeltaY(part.getPivotDeltaY() + STEP);
                if (input.isBackward()) part.setPivotDeltaY(part.getPivotDeltaY() - STEP);
            } else {
                if (input.isForward())  part.setPivotDeltaZ(part.getPivotDeltaZ() + STEP);
                if (input.isBackward()) part.setPivotDeltaZ(part.getPivotDeltaZ() - STEP);
                if (input.isLeft())     part.setPivotDeltaX(part.getPivotDeltaX() + STEP);
                if (input.isRight())    part.setPivotDeltaX(part.getPivotDeltaX() - STEP);
            }
        } else { // POSITION
            if (sprint) {
                if (input.isForward())  part.setDeltaY(part.getDeltaY() + STEP);
                if (input.isBackward()) part.setDeltaY(part.getDeltaY() - STEP);
            } else {
                if (input.isForward())  part.setDeltaZ(part.getDeltaZ() + STEP);
                if (input.isBackward()) part.setDeltaZ(part.getDeltaZ() - STEP);
                if (input.isLeft())     part.setDeltaX(part.getDeltaX() + STEP);
                if (input.isRight())    part.setDeltaX(part.getDeltaX() - STEP);
            }
        }
    }

    // ── Events ────────────────────────────────────────────────────────────────

    /** Right-click with orbit sticks → horizontal orbit. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PartEditState state = sessions.get(player.getUniqueId());
        if (state == null) return;

        event.setCancelled(true);

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot == SLOT_LEFT)       orbitHorizontal(state, -ORBIT_STEP);
        else if (slot == SLOT_RIGHT) orbitHorizontal(state,  ORBIT_STEP);
    }

    /**
     * Left-click (arm-swing) with orbit sticks → vertical orbit.
     * PlayerInteractEvent(LEFT_CLICK_AIR) is not fired when the player is mounted,
     * so we use PlayerAnimationEvent instead.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onArmSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

        Player player = event.getPlayer();
        PartEditState state = sessions.get(player.getUniqueId());
        if (state == null) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot == SLOT_LEFT)       orbitElevation(state,  ELEV_STEP);
        else if (slot == SLOT_RIGHT) orbitElevation(state, -ELEV_STEP);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PartEditState state = sessions.get(player.getUniqueId());
        if (state == null) return;
        if (!event.getDismounted().getUniqueId().equals(state.stand.getUniqueId())) return;

        sessions.remove(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> state.stand.remove(), 1L);

        restoreInventory(state);
        database.save(state.vehicle.getCustomization());

        PartCustomization part = state.vehicle.getCustomization().getPart(state.partKey);
        player.sendMessage(
                Component.text("Saved ", NamedTextColor.GREEN)
                        .append(Component.text("[" + state.partKey + "]", NamedTextColor.GOLD))
                        .append(Component.text(
                                String.format("  pos(%.3f, %.3f, %.3f)  pivot(%.3f, %.3f, %.3f)  rot:%.1f°",
                                        part.getDeltaX(), part.getDeltaY(), part.getDeltaZ(),
                                        part.getPivotDeltaX(), part.getPivotDeltaY(), part.getPivotDeltaZ(),
                                        part.getRotationYaw()),
                                NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        endSession(event.getPlayer().getUniqueId());
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void showActionBar(PartEditState state) {
        PartCustomization part = state.vehicle.getCustomization().getPart(state.partKey);
        Component bar;

        if (state.mode == EditorMode.POSITION) {
            bar = Component.text("[", NamedTextColor.DARK_GRAY)
                    .append(Component.text(state.partKey, NamedTextColor.GOLD))
                    .append(Component.text(" POS]  ", NamedTextColor.DARK_GRAY))
                    .append(label("X")).append(value(fmt(part.getDeltaX())))
                    .append(label("Y")).append(value(fmt(part.getDeltaY())))
                    .append(label("Z")).append(value(fmt(part.getDeltaZ()) + "  "))
                    .append(label("Rot")).append(Component.text(String.format("%.1f°", part.getRotationYaw()), NamedTextColor.AQUA));
        } else {
            bar = Component.text("[", NamedTextColor.DARK_GRAY)
                    .append(Component.text(state.partKey, NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(" PIVOT]  ", NamedTextColor.DARK_GRAY))
                    .append(label("pX")).append(pivotVal(fmt(part.getPivotDeltaX())))
                    .append(label("pY")).append(pivotVal(fmt(part.getPivotDeltaY())))
                    .append(label("pZ")).append(pivotVal(fmt(part.getPivotDeltaZ()) + "  "))
                    .append(label("Rot")).append(Component.text(String.format("%.1f°", part.getRotationYaw()), NamedTextColor.AQUA));
        }

        state.player.sendActionBar(bar.decoration(TextDecoration.ITALIC, false));
    }

    private static Component label(String s) {
        return Component.text(s + ":", NamedTextColor.GRAY);
    }
    private static Component value(String s) {
        return Component.text(s + " ", NamedTextColor.YELLOW);
    }
    private static Component pivotVal(String s) {
        return Component.text(s + " ", NamedTextColor.LIGHT_PURPLE);
    }
    private static String fmt(double v) { return String.format("%.3f", v); }

    private void sendModeTitle(Player player, String partKey, EditorMode mode, boolean isStart) {
        Component title = isStart
                ? Component.text(partKey, NamedTextColor.GOLD)
                : Component.text(mode == EditorMode.POSITION ? "MODE: POSITION" : "MODE: PIVOT",
                        mode == EditorMode.POSITION ? NamedTextColor.YELLOW : NamedTextColor.LIGHT_PURPLE);

        Component sub = mode == EditorMode.POSITION
                ? Component.text("WASD:pos  Ctrl+WS:Y  Space+AD:rot  Sprint+Space:pivot  Sneak:save", NamedTextColor.GRAY)
                : Component.text("WASD:pivot XZ  Ctrl+WS:pivot Y  Space+AD:rot  Sprint+Space:pos  Sneak:save", NamedTextColor.LIGHT_PURPLE);

        player.showTitle(Title.title(
                title.decoration(TextDecoration.ITALIC, false),
                sub.decoration(TextDecoration.ITALIC, false),
                Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(3), Duration.ofMillis(400))
        ));
    }

    // ── Pivot particles ───────────────────────────────────────────────────────

    /** Spawns END_ROD particles at the pivot world position, visible only to the editing player. */
    private void spawnPivotParticles(PartEditState state) {
        ItemDisplay partEntity = resolvePartEntity(state.vehicle, state.partKey);
        if (partEntity == null || partEntity.isDead()) return;

        Vector3d configPivot = resolveConfigPivot(state.vehicle, state.partKey);
        if (configPivot == null) return;

        PartCustomization c = state.vehicle.getCustomization().getPart(state.partKey);
        double px = configPivot.x + c.getPivotDeltaX();
        double py = configPivot.y + c.getPivotDeltaY();
        double pz = configPivot.z + c.getPivotDeltaZ();

        // Rotate from part-local space to world space using vehicle yaw
        double rad = Math.toRadians(state.vehicle.getLocation().getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double wdx = px * cos - pz * sin;
        double wdz = px * sin + pz * cos;

        Location pivotLoc = partEntity.getLocation().clone().add(wdx, py, wdz);

        // Only the editing player sees these particles
        state.player.spawnParticle(Particle.END_ROD, pivotLoc, 2, 0.015, 0.015, 0.015, 0);
    }

    /** Returns the ItemDisplay entity for a part key. */
    private static ItemDisplay resolvePartEntity(Vehicle vehicle, String partKey) {
        return switch (partKey.toLowerCase()) {
            case "frame"         -> vehicle.getFrame().getItemDisplay();
            case "steering_wheel"-> vehicle.getSteeringWheel();
            case "door_0"        -> vehicle.getDoors().length > 0 ? vehicle.getDoors()[0].getDoorModel() : null;
            case "door_1"        -> vehicle.getDoors().length > 1 ? vehicle.getDoors()[1].getDoorModel() : null;
            case "wheel_0"       -> vehicle.getWheels().length > 0 ? vehicle.getWheels()[0].getModel() : null;
            case "wheel_1"       -> vehicle.getWheels().length > 1 ? vehicle.getWheels()[1].getModel() : null;
            case "wheel_2"       -> vehicle.getWheels().length > 2 ? vehicle.getWheels()[2].getModel() : null;
            case "wheel_3"       -> vehicle.getWheels().length > 3 ? vehicle.getWheels()[3].getModel() : null;
            default              -> null;
        };
    }

    /** Returns the config pivot for a part key, or null if the part has no pivot. */
    private static Vector3d resolveConfigPivot(Vehicle vehicle, String partKey) {
        return switch (partKey.toLowerCase()) {
            case "steering_wheel" -> vehicle.getConfig().steeringWheel().pivot();
            case "door_0"         -> vehicle.getConfig().doors().size() > 0 ? vehicle.getConfig().doors().get(0).part().pivot() : null;
            case "door_1"         -> vehicle.getConfig().doors().size() > 1 ? vehicle.getConfig().doors().get(1).part().pivot() : null;
            case "wheel_0"        -> vehicle.getConfig().wheels().size() > 0 ? vehicle.getConfig().wheels().get(0).part().pivot() : null;
            case "wheel_1"        -> vehicle.getConfig().wheels().size() > 1 ? vehicle.getConfig().wheels().get(1).part().pivot() : null;
            case "wheel_2"        -> vehicle.getConfig().wheels().size() > 2 ? vehicle.getConfig().wheels().get(2).part().pivot() : null;
            case "wheel_3"        -> vehicle.getConfig().wheels().size() > 3 ? vehicle.getConfig().wheels().get(3).part().pivot() : null;
            default               -> null; // frame and seat have no pivot
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ItemStack makeStick(String name, NamedTextColor color, List<String> loreLines) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(loreLines.stream()
                .map(l -> Component.text(l, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .toList());
        stick.setItemMeta(meta);
        return stick;
    }

    public boolean hasSession(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private static class PartEditState {
        final Player player;
        final Vehicle vehicle;
        final String partKey;

        ArmorStand stand;
        Location standLoc;
        ItemStack[] savedInventory;

        EditorMode mode = EditorMode.POSITION;
        boolean prevSprintSpace = false;
        int cooldown = 0;
        int particleTick = 0;

        double orbitAngle;
        double orbitElevation = 0.0;

        PartEditState(Player player, Vehicle vehicle, String partKey, double initialOrbitAngle) {
            this.player = player;
            this.vehicle = vehicle;
            this.partKey = partKey;
            this.orbitAngle = initialOrbitAngle;
        }
    }
}
