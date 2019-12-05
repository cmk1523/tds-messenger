package com.techdevsolutions.messenger.beans;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.messenger.beans.auditable.Message;

public class MessageUpdatedEvent extends MessageEvent {
    public static final String TYPE_UPDATED = MessageEvent.CATEGORY + ".updated";

    public MessageUpdatedEvent() {
    }

    public MessageUpdatedEvent(Message item) {
        super(item);
        this.setType(MessageUpdatedEvent.TYPE_UPDATED);
        this.setAction(EventElasticsearchDAO.ACTION_UPDATED);
        this.setCode(EventElasticsearchDAO.CODE_UPDATED);
        this.setKind(EventElasticsearchDAO.KIND_UPDATED);
    }

    public MessageUpdatedEvent(MessageEvent item) {
        super(item.getData());
        this.setType(MessageUpdatedEvent.TYPE_UPDATED);
        this.setAction(EventElasticsearchDAO.ACTION_UPDATED);
        this.setCode(EventElasticsearchDAO.CODE_UPDATED);
        this.setKind(EventElasticsearchDAO.KIND_UPDATED);
    }
}
