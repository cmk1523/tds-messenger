package com.techdevsolutions.messenger.dao.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.common.beans.geo.GeoLocation;
import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.common.service.core.Timer;
import com.techdevsolutions.messenger.beans.MessageEvent;
import com.techdevsolutions.messenger.beans.MessageUpdatedEvent;
import com.techdevsolutions.messenger.beans.MessageCreatedEvent;
import com.techdevsolutions.messenger.beans.MessageRemovedEvent;
import com.techdevsolutions.messenger.beans.auditable.Message;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ElasticsearchMessageDao implements DaoCrudInterface<Message> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String INDEX_BASE_NAME = "events-messages";

    private EventElasticsearchDAO dao;

    @Autowired
    public ElasticsearchMessageDao(Environment environment) {
        this.dao = new EventElasticsearchDAO("localhost", ElasticsearchMessageDao.INDEX_BASE_NAME);

        if (environment != null) {
            String elasticsearchHost = environment.getProperty("messagedao.elasticsearch.host");

            if (StringUtils.isNotEmpty(elasticsearchHost)) {
                this.dao = new EventElasticsearchDAO(elasticsearchHost, ElasticsearchMessageDao.INDEX_BASE_NAME);
            }
        }

        this.dao = new EventElasticsearchDAO("localhost", ElasticsearchMessageDao.INDEX_BASE_NAME);
    }

    public static MessageEvent RemoveUnusedFields(final MessageEvent item) throws Exception {
        // Remove fields from the event object that just don't need to be stored
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        MessageEvent copy = objectMapper.readValue(objectMapper.writeValueAsString(item), MessageEvent.class);
        copy.setSeverity(null);
        copy.setSequence(null);
        copy.setStart(null);
        copy.setEnd(null);
        copy.setTimezone(null);
        copy.setRiskScore(null);
        copy.setRisrScoreNormalized(null);
        copy.setProvider(null);
        copy.setModule(null);
        copy.setOutcome(null);
        copy.setHash(null);
        copy.setOriginal(null);
        return copy;
    }

    @Override
    public List<Message> search() {
        return null;
    }

    @Override
    public Message get(final String id) throws Exception {
        Timer timer = new Timer().start();

        try {
            if (StringUtils.isEmpty(id)) {
                throw new IllegalArgumentException("id is null or empty");
            }

            Event event = this.dao.getEventByEventDataIdLazy(id, MessageEvent.CATEGORY, MessageEvent.DATASET);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new Jdk8Module());
            Map<String, Object> itemAsMap = objectMapper.convertValue(event, Map.class);
            Map<String, Object> data = (Map<String, Object>) itemAsMap.get("data");

            if (data.get("location") != null) {
                String location = (String) data.get("location");
                data.put("location", GeoLocation.fromLatLonString(location));
            }

            MessageEvent MessageEvent = objectMapper.convertValue(itemAsMap, MessageEvent.class);

            if (MessageEvent.getAction().equals(EventElasticsearchDAO.ACTION_REMOVED)) {
                throw new Exception(("Item has been removed"));
            }

            Message item = MessageEvent.getData();
            this.logger.info("Got item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
            return item;
        } catch (Exception e) {
            this.logger.info("Failed to get item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
            throw e;
        }
    }

    @Override
    public Message create(final Message item) throws Exception {
        Timer timer = new Timer().start();
        Message itemToFind = null;

        try {
            itemToFind = this.get(item.getId());
        } catch (Exception e) {
            if (e.getMessage().contains("Item has been removed")) {
                throw e;
            }
        }

        if (itemToFind != null) {
            throw new Exception("Item already exists with id: " + item.getId());
        }

        MessageCreatedEvent createdEvent = new MessageCreatedEvent(item);
        MessageEvent event = ElasticsearchMessageDao.RemoveUnusedFields(createdEvent);
        this.dao.create(event);
        Thread.sleep(1L); // wait a second because we dont want to the create time to be same as any other
        this.logger.info("Created item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    @Override
    public void remove(final String id) throws Exception {
        Timer timer = new Timer().start();

        Message item = this.get(id);
        MessageRemovedEvent removedEvent = new MessageRemovedEvent(item);
        MessageEvent event = ElasticsearchMessageDao.RemoveUnusedFields(removedEvent);
        this.dao.create(event);
        this.logger.info("Removed item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
    }

    @Override
    public void delete(final String id) throws Exception {
        // Events are not meant to be deleted
        throw new Exception("Method not implemented: Events are never meant to be deleted. Did you mean to use remove()?");
    }

    @Override
    public Message update(final Message item) throws Exception {
        Timer timer = new Timer().start();

        // You must use .get(...) to ensure the item hasn't been flagged as removed
        this.get(item.getId());
        MessageUpdatedEvent updatedEvent = new MessageUpdatedEvent(item);
        MessageEvent event = ElasticsearchMessageDao.RemoveUnusedFields(updatedEvent);
        this.dao.create(event);
        this.logger.info("Updated item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    public Boolean verifyRemoval(final String id) throws Exception {
        return this.dao.verifyRemoval(id, MessageEvent.CATEGORY, MessageEvent.DATASET);
    }

    @Override
    public void setupIndex() throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(ElasticsearchMessageDao.INDEX_BASE_NAME);
//        request.settings(Settings.builder()
//                .put("index.number_of_shards", 3)
//                .put("index.number_of_replicas", 2)
//        );
        request.mapping(
                "{\n" +
                        "      \"properties\" : {\n" +
                        "        \"event\" : {\n" +
                        "          \"properties\" : {\n" +
                        "            \"data\" : {\n" +
                        "              \"properties\" : {\n" +
                        "                \"location\" : {\n" +
                        "                  \"type\" : \"geo_point\"\n" +
                        "                },\n" +
                        "                \"message\" : {\n" +
                        "                  \"type\" : \"text\"\n" +
                        "                }\n" +
                        "              }\n" +
                        "            }\n" +
                        "          }\n" +
                        "        }\n" +
                        "      }\n" +
                        "}",
                XContentType.JSON);
        CreateIndexResponse createIndexResponse = this.dao.getClient().indices().create(request, RequestOptions.DEFAULT);

    }
}
