package com.gmail.ed3v8vl0.MessageLibrary;

@FunctionalInterface
public interface IMessageHandler{
    <T> void run(T object);
}