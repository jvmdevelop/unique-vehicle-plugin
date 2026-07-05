package com.jvmd.uniqueVehiclePlugin.assembler;

import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleFrame;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class VehicleAssembler {

    public Vehicle assemble(VehicleConfig config, Location location) {
        location = location.clone();
        location.setPitch(0);
        World world = location.getWorld();

        VehicleFrame frame = buildFrame(config.frame(), location, world);
        ItemDisplay steeringWheel = spawnDisplay(config.steeringWheel(), location, world);

        VehicleDoor[] doors = new VehicleDoor[config.doors().size()];
        for (int i = 0; i < config.doors().size(); i++) {
            VehicleConfig.DoorPartConfig doorConfig = config.doors().get(i);
            ItemDisplay doorDisplay = spawnDisplay(doorConfig.part(), location, world);
            doors[i] = new VehicleDoor(doorConfig.doorType(), doorDisplay, doorConfig.part().offset());
        }

        Wheel[] wheels = new Wheel[config.wheels().size()];
        for (int i = 0; i < config.wheels().size(); i++) {
            VehicleConfig.WheelPartConfig wheelConfig = config.wheels().get(i);
            ItemDisplay wheelDisplay = spawnDisplay(wheelConfig.part(), location, world);
            wheels[i] = new Wheel(wheelConfig.wheelType(), wheelConfig.front(), wheelDisplay, wheelConfig.part().offset());
        }

        Vector3d seatOffset = config.seatOffset();
        Location seatLoc = location.clone().add(seatOffset.x, seatOffset.y, seatOffset.z);

        org.bukkit.entity.ArmorStand seat = world.spawn(seatLoc, org.bukkit.entity.ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setSmall(true);
            as.setMarker(true);
            as.setCollidable(false);
            as.setPersistent(false);
            as.setCanPickupItems(false);
        });

        Interaction hitbox = world.spawn(location, Interaction.class, entity -> {
            entity.setInteractionWidth(2.0f);
            entity.setInteractionHeight(2.0f);
            entity.setResponsive(true);
        });

        return new Vehicle(config, frame, steeringWheel, doors, wheels, seat, hitbox, location);
    }

    private VehicleFrame buildFrame(PartConfig config, Location location, World world) {
        ItemDisplay display = spawnDisplay(config, location, world);
        return new VehicleFrame(display, config.offset());
    }

    private ItemDisplay spawnDisplay(PartConfig config, Location baseLocation, World world) {
        Location spawnLoc = baseLocation.clone().add(config.offset().x, config.offset().y, config.offset().z);

        return world.spawn(spawnLoc, ItemDisplay.class, entity -> {
            ItemStack item = new ItemStack(config.material());
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(config.customModelData());
            item.setItemMeta(meta);
            entity.setItemStack(item);
            entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            entity.setTeleportDuration(1);
            entity.setInterpolationDuration(2);
            entity.setTransformation(new Transformation(new Vector3f(0, 0, 0), new Quaternionf(), new Vector3f(0, 0, 0), new Quaternionf()));
        });
    }
}
