package com.techdevsolutions.messenger.dao.elasticsearch;

import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.common.beans.geo.GeoLocation;
import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.messenger.beans.MessageEvent;
import com.techdevsolutions.messenger.beans.auditable.Message;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

@Ignore
public class ElasticsearchMessageDaoIntegrationTestV2 {

    private ElasticsearchMessageDao dao = new ElasticsearchMessageDao(null);
    private EventElasticsearchDAO eventElasticsearchDAO =
            new EventElasticsearchDAO("localhost", ElasticsearchMessageDao.INDEX_BASE_NAME);

    @Test
    public void create() throws Exception {
        this.dao.setupIndex();

        Message item1 = new Message();
        item1.setId("test-567");
        item1.setCreated(123L);
        item1.setFrom("123");
        item1.setTo("456");
        item1.setMessage("hey. are you there?");
        item1.setLocation(Optional.of(new GeoLocation(12.34, 23.45)));

        Message item2 = new Message();
        item2.setId("test-678");
        item2.setCreated(123456L);
        item2.setFrom("456");
        item2.setTo("123");
        item2.setMessage("hey! yes i am.");
        item2.setLocation(Optional.of(new GeoLocation(56.78, 67.89)));

        try {
            List<Event> events = this.eventElasticsearchDAO.getEventsByEventDataId(item1.getId(),
                    MessageEvent.CATEGORY, MessageEvent.DATASET);
            events.forEach((i) -> {
                try {
                    this.eventElasticsearchDAO.delete(i.getId());
                } catch (Exception ignored) {

                }
            });
            Thread.sleep(1000L);

            events = this.eventElasticsearchDAO.getEventsByEventDataId(item2.getId(),
                    MessageEvent.CATEGORY, MessageEvent.DATASET);
            events.forEach((i) -> {
                try {
                    this.eventElasticsearchDAO.delete(i.getId());
                } catch (Exception ignored) {

                }
            });
            Thread.sleep(1000L);
        } catch (Exception ignored) {

        }

        Message created = this.dao.create(item1);
        Message get = this.dao.get(created.getId());
        Assert.assertTrue(get.equals(created));

        created = this.dao.create(item2);
        get = this.dao.get(created.getId());
        Assert.assertTrue(get.equals(created));
    }
}