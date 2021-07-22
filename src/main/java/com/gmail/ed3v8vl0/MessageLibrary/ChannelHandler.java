package com.gmail.ed3v8vl0.MessageLibrary;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.LongStringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChannelHandler.class);
    private final Map<Type, IMessageHandler> callbackMap = new HashMap<>();
    private final String routingKey;
    private final String channelName;
    private final Channel channel;
    private final Gson gson = new Gson();

    public ChannelHandler(String routingKey, String channelName, Connection connection) throws IOException {
        this(routingKey, channelName, connection, cancelCallback -> {});
    }

    public ChannelHandler(String routingKey, String channelName, Connection connection, CancelCallback cancelCallback) throws IOException {
        this.routingKey = routingKey;
        this.channelName = channelName;
        this.channel = connection.createChannel();
        this.channel.exchangeDeclare(channelName, BuiltinExchangeType.FANOUT);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, this.channelName, "");
        channel.basicConsume(queueName, true, "", true, true, null, (consumerTag, message) -> {
            BasicProperties properties = message.getProperties();
            Map<String, Object> headers = properties.getHeaders();
            Object typeID__ = headers.get("__TypeId__");

            if (typeID__ instanceof LongString) {
                try {
                    Type type = Class.forName(typeID__.toString());

                    if (!this.routingKey.equals(message.getEnvelope().getRoutingKey()))
                        this.callbackMap.get(type).run(this.gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), type));
                } catch (ClassNotFoundException e) {
                    ChannelHandler.logger.error("[{}] Wrong Message Received.", this.routingKey, e);
                }
            }
        }, cancelCallback);
    }

    /**
     * Register a callback of that type.
     * @param type Class Type
     * @param messageHandler Callback Runnable
     */
    public void registerType(Type type, IMessageHandler messageHandler) {
        this.callbackMap.put(type, messageHandler);
    }

    public void sendMessage(Object object) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("__TypeId__", LongStringHelper.asLongString(object.getClass().getName()));

        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().deliveryMode(1).headers(headers).contentEncoding("UTF-8").contentType("application/json").build();
        try {
            this.channel.basicPublish(this.channelName, this.routingKey, properties, this.gson.toJson(object).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            ChannelHandler.logger.error("[{}] Message Publish Error.", this.routingKey, e);
        }
    }

    public boolean isRegistryType(Type type) {
        return this.callbackMap.containsKey(type);
    }

    public String getRoutingKey() { return this.routingKey; }

    public String getChannelName() { return this.channelName; }
}