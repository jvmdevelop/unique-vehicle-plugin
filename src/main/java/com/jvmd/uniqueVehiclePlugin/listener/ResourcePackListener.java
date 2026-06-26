package com.jvmd.uniqueVehiclePlugin.listener;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.util.UUID;

public class ResourcePackListener implements Listener {

    private final ResourcePackInfo packInfo;

    public ResourcePackListener(String url, String sha1) {
        this.packInfo = ResourcePackInfo.resourcePackInfo()
                .id(UUID.nameUUIDFromBytes(sha1.getBytes()))
                .uri(URI.create(url))
                .hash(sha1)
                .build();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                .packs(packInfo)
                .required(false)
                .build());
    }
}
