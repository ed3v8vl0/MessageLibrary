package com.gmail.ed3v8vl0.MessageLibrary;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class ConnectionHandle {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandle.class);
    private final HashMap<String, ChannelHandler> channelMap = new HashMap<>();
    private final Connection connection;
    private final String pluginName;

    public ConnectionHandle(String pluginName, ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        this.connection = connectionFactory.newConnection();
        this.pluginName = pluginName;
    }

    /**
     * Register a channel.
     * @param channelName Channel Name
     */
    public ChannelHandler registerChannel(String channelName) {
        ChannelHandler channelHandler = null;

        try {
            channelHandler = new ChannelHandler(this.pluginName, channelName, this.connection);

            this.channelMap.put(channelName, channelHandler);
            ConnectionHandle.logger.info("[{}] {} Channel Creating Succeed.", this.pluginName, channelName);
        } catch (IOException e) {
            ConnectionHandle.logger.error("[{}] {} Channel Creating Failed.", this.pluginName, channelName, e);
        }

        return channelHandler;
    }

    /**
     * Register a channel.
     * @param channelName Channel Name
     * @param cancelCallback Cancel Callback Runnable
     */
    public ChannelHandler registerChannel(String channelName, CancelCallback cancelCallback) {
        ChannelHandler channelHandler = null;

        try {
            channelHandler = new ChannelHandler(this.pluginName, channelName, this.connection, cancelCallback);

            this.channelMap.put(channelName, channelHandler);
            ConnectionHandle.logger.info("[{}] {} Channel Creating Succeed.", this.pluginName, channelName);
        } catch (IOException e) {
            ConnectionHandle.logger.error("[{}] {} Channel Creating Failed.", this.pluginName, channelName, e);
        }

        return channelHandler;
    }

    /**
     * Send a message to that channel.
     * @param channelName Channel Name
     * @param object Gson Serialized Object
     */
    public void sendToMessage(String channelName, Object object) {
        ChannelHandler channelHandler = this.getChannel(channelName);

        if (channelHandler.isRegistryType((Type) object)) {
            channelHandler.sendMessage(object);
        } else {
            ConnectionHandle.logger.error("[{}] ChannelName: {}, Type: {} Not registered type.", this.pluginName, channelName, object.getClass().getTypeName());
        }
    }

    public ChannelHandler getChannel(String channelName) {
        return this.channelMap.get(channelName);
    }

    public String getPluginName() { return this.pluginName; }

    protected void close() throws IOException { this.connection.close(); }

    @Override
    public String toString() {
        return "ConnectionHandle{" +
                "channelMap=" + channelMap +
                ", connection=" + connection +
                ", pluginName='" + pluginName + '\'' +
                '}';
    }
}