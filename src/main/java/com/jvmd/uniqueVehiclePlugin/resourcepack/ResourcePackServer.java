package com.jvmd.uniqueVehiclePlugin.resourcepack;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.logging.Logger;

public class ResourcePackServer {

    private final HttpServer server;
    private final String url;
    private final String sha1;

    public ResourcePackServer(Plugin plugin, File zipFile, int port) throws IOException {
        this.sha1 = computeSha1(zipFile);
        this.url = "http://localhost:" + port + "/" + zipFile.getName();

        byte[] data = Files.readAllBytes(zipFile.toPath());

        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/" + zipFile.getName(), exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        });
        server.setExecutor(null);
        server.start();

        Logger log = plugin.getLogger();
        log.info("Resource pack server started on port " + port);
        log.info("URL: " + url);
        log.info("SHA1: " + sha1);
    }

    public String getUrl() { return url; }

    public String getSha1() { return sha1; }

    public void stop() {
        server.stop(0);
    }

    private static String computeSha1(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = Files.readAllBytes(file.toPath());
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA1", e);
        }
    }
}
