package com.the7winds.verbumSecretum.other;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by the7winds on 21.11.15.
 */
public abstract class Message {

    private final String head;
    protected static final String HEAD_FIELD = "HEAD";
    protected String senderId;

    public Message(String head) {
        this.head = head;
    }

    public String serialize() {
        return baseSerialize();
    }

    public Message deserialize(String str) {
        return this;
    }

    protected JsonObject getBaseJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty(HEAD_FIELD, head);
        return obj;
    }

    protected String baseSerialize() {
        return new Gson().toJson(getBaseJsonObject());
    }

    final public String getHEAD() {
        return head;
    }

    public void setSenderId(String id) {
        senderId = id;
    }
}
