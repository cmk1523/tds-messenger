package com.techdevsolutions.messenger.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.messenger.beans.auditable.Message;

import java.util.Date;

public class MessageEvent extends Event<Message> {
    public static final String CATEGORY = "message";
    public static final String DATASET = "messages";

    public MessageEvent() {
    }

    public MessageEvent(final Message item) {
        // Map applicable fields from the source to the event model
        this.setCreated(new Date());
        this.setCategory(MessageEvent.CATEGORY);
        this.setDataset(MessageEvent.DATASET);
        this.setData(item);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new Jdk8Module());
            this.setOriginal(new ObjectMapper().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessageEvent setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public MessageEvent setAction(String action) {
        super.setAction(action);
        return this;
    }

    @Override
    public MessageEvent setCode(String code) {
        super.setCode(code);
        return this;
    }

    @Override
    public MessageEvent setCreated(Date date) {
        super.setCreated(date);
        return this;
    }

    @Override
    public MessageEvent setKind(String kind) {
        super.setKind(kind);
        return this;
    }

}
