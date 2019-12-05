package com.techdevsolutions.messenger.beans;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.messenger.beans.auditable.Message;

public class MessageCreatedEvent extends MessageEvent {
    public static final String TYPE_CREATED = MessageEvent.CATEGORY + ".created";

    public MessageCreatedEvent() {
    }

    public MessageCreatedEvent(Message item) {
        super(item);
        this.setType(MessageCreatedEvent.TYPE_CREATED);
        this.setAction(EventElasticsearchDAO.ACTION_CREATED);
        this.setCode(EventElasticsearchDAO.CODE_CREATED);
        this.setKind(EventElasticsearchDAO.KIND_CREATE);
    }

    public MessageCreatedEvent(MessageEvent item) {
        super(item.getData());
        this.setType(MessageCreatedEvent.TYPE_CREATED);
        this.setAction(EventElasticsearchDAO.ACTION_CREATED);
        this.setCode(EventElasticsearchDAO.CODE_CREATED);
        this.setKind(EventElasticsearchDAO.KIND_CREATE);
    }
}
