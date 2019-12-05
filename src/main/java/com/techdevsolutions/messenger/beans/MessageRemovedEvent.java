package com.techdevsolutions.messenger.beans;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.messenger.beans.auditable.Message;

public class MessageRemovedEvent extends MessageEvent {
    public static final String TYPE_REMOVED = MessageEvent.CATEGORY + ".removed";

    public MessageRemovedEvent() {
    }

    public MessageRemovedEvent(Message item) {
        super(item);
        this.setType(MessageRemovedEvent.TYPE_REMOVED);
        this.setAction(EventElasticsearchDAO.ACTION_REMOVED);
        this.setCode(EventElasticsearchDAO.CODE_REMOVED);
        this.setKind(EventElasticsearchDAO.KIND_REMOVED);
    }

    public MessageRemovedEvent(MessageEvent item) {
        super(item.getData());
        this.setType(MessageRemovedEvent.TYPE_REMOVED);
        this.setAction(EventElasticsearchDAO.ACTION_REMOVED);
        this.setCode(EventElasticsearchDAO.CODE_REMOVED);
        this.setKind(EventElasticsearchDAO.KIND_REMOVED);
    }
}
