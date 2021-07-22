package com.gmail.ed3v8vl0.MessageLibrary;

import com.rabbitmq.client.ConnectionFactory;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class RabbitManager {
    private static final Logger logger = LoggerFactory.getLogger(RabbitManager.class);
    private final HashMap<String, ConnectionHandle> connectionMap = new HashMap<>();
    private final ConnectionFactory connectionFactory;

    public RabbitManager(String host, int port, String username, String password) {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(host);
        this.connectionFactory.setPort(port);
        this.connectionFactory.setUsername(username);
        this.connectionFactory.setPassword(password);
    }

    /**
     * Connect to RabbitMQ and return a ConnectionHandle.
     * Returns a ConnectionHandle if already connected.
     */
    public ConnectionHandle getConnection(Plugin plugin) {
        String pluginName = plugin.getName();

        if (!this.connectionMap.containsKey(pluginName)) {
            try {
                this.connectionMap.put(pluginName, new ConnectionHandle(pluginName, connectionFactory));
                RabbitManager.logger.info("[{}] RabbitMQ Connection Succeed.", pluginName);
            } catch (IOException | TimeoutException e) {
                RabbitManager.logger.error("[{}] RabbitMQ Connection Error.", pluginName, e);
            }
        }

        return this.connectionMap.get(pluginName);
    }

    protected void close(Plugin plugin) {
        String pluginName = plugin.getName();

        if (this.connectionMap.containsKey(pluginName)) {
            ConnectionHandle connectionHandle = this.connectionMap.get(pluginName);

            try {
                connectionHandle.close();
                this.connectionMap.remove(pluginName);
                RabbitManager.logger.info("[{}] RabbitMQ Connection Closed.", pluginName);
            } catch (IOException e) {
                RabbitManager.logger.error("[{}] ConnectionHandle : {}", pluginName, connectionHandle, e);
            }
        }
    }

    protected void closeAll() {
        for (ConnectionHandle connectionHandle : this.connectionMap.values()) {
            try {
                connectionHandle.close();
            } catch (IOException e) {
                RabbitManager.logger.error("ConnectionHandle : {}", connectionHandle, e);
            }
        }

        RabbitManager.logger.info("RabbitMQ All Connection Closed.");
        this.connectionMap.clear();
    }
}
