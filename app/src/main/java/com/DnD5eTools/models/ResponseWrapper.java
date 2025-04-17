package com.DnD5eTools.models;

import java.util.List;

public class ResponseWrapper<T> {
    private List<ResponseMessage> messages;
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<ResponseMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ResponseMessage> messages) {
        this.messages = messages;
    }
}
