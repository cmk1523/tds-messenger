package com.techdevsolutions.messenger.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.common.beans.geo.GeoLocation;
import com.techdevsolutions.messenger.beans.auditable.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;

import java.util.Date;
import java.util.Map;

public class MessageEvent extends Event<Message> {
    public static final String CATEGORY = "message";
    public static final String DATASET = "messages";

    public MessageEvent() {
    }

    public Map<String, Object> toElasticsearchMap(Event item) {
        Map<String, Object> map = Event.ToElasticsearchMap(item);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        Map<String, Object> dataMap = objectMapper.convertValue(item.getData(), Map.class);
        Message message = (Message) item.getData();

        if (message.getLocation().isPresent()) {
            GeoLocation geoLocation = message.getLocation().get();
            dataMap.put("location", geoLocation.getLatitude() + "," + geoLocation.getLongitude());
        }

        CollectionUtils.filter(dataMap.values(), PredicateUtils.notNullPredicate());
        map.put("event.data", dataMap);
        return map;
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
