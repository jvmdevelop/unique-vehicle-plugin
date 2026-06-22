package com.jvmd.uniqueVehiclePlugin.assembler;

import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleDoor;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleFrame;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Vector3d;

public class VehicleAssembler {

    public Vehicle assemble(VehicleConfig config, Location location) {
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
            wheels[i] = new Wheel(wheelConfig.wheelType(), wheelDisplay, wheelConfig.part().offset());
        }

        Vector3d seatOffset = config.seatOffset();
        Location seatLoc = location.clone().add(seatOffset.x, seatOffset.y, seatOffset.z);
        Minecart seat = (Minecart) world.spawnEntity(seatLoc, EntityType.MINECART);
        seat.setDisplayBlockData(Material.AIR.createBlockData());
        seat.setVisibleByDefault(false);
        seat.setGravity(false);
        seat.setInvulnerable(true);
        seat.setSilent(true);

        return new Vehicle(frame, steeringWheel, doors, wheels, seat, location);
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
        });
    }
}
